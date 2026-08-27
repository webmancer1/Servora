package com.example.servora.data.repository

import com.example.servora.data.local.AlertDao
import com.example.servora.data.local.AlertEntity
import com.example.servora.data.local.MetricDao
import com.example.servora.data.local.MetricSnapshotEntity
import com.example.servora.data.local.RemoteActionDao
import com.example.servora.data.local.RemoteActionEntity
import com.example.servora.data.local.ServerDao
import com.example.servora.data.local.ServerEntity
import com.example.servora.data.model.AlertItem
import com.example.servora.data.model.AlertSeverity
import com.example.servora.data.model.ProcessInfo
import com.example.servora.data.model.Server
import com.example.servora.data.model.ServerStatus
import com.example.servora.data.remote.ApiFactory
import com.example.servora.data.remote.RemoteActionRequest
import com.example.servora.data.settings.MonitoringSettings
import com.example.servora.data.settings.SettingsRepository
import com.example.servora.di.ApplicationScope
import com.example.servora.domain.StatusEvaluator
import com.example.servora.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central monitoring brain. Polls every configured server on the configured
 * interval — live agents over HTTP when a server has a baseUrl, generated
 * data otherwise — evaluates status against the user's thresholds, persists
 * every reading as a Room snapshot, raises deduplicated alerts, and posts
 * notifications for new critical/warning events.
 */
@Singleton
class ServerRepository @Inject constructor(
    private val serverDao: ServerDao,
    private val metricDao: MetricDao,
    private val alertDao: AlertDao,
    private val actionDao: RemoteActionDao,
    private val settingsRepository: SettingsRepository,
    private val statusEvaluator: StatusEvaluator,
    private val apiFactory: ApiFactory,
    private val notificationHelper: NotificationHelper,
    @param:ApplicationScope private val scope: CoroutineScope
) {

    private val liveServers = MutableStateFlow<List<Server>>(emptyList())
    val servers: Flow<List<Server>> = liveServers.asStateFlow()

    private val previousStatuses = mutableMapOf<String, ServerStatus>()
    private val mockGenerator = MockMetricsGenerator()
    private val latestSettings = MutableStateFlow(MonitoringSettings())

    init {
        scope.launch {
            seedIfEmpty()
        }
        scope.launch {
            settingsRepository.settings.collect { latestSettings.value = it }
        }
        scope.launch {
            pollingLoop()
        }
    }

    private suspend fun pollingLoop() {
        while (scope.isActive) {
            runCatching { refreshOnce() }
            delay(latestSettings.value.refreshIntervalMs)
        }
    }

    fun observeServer(id: String): Flow<Server?> = liveServers.map { list -> list.find { it.id == id } }

    suspend fun currentServers(): List<Server> {
        if (liveServers.value.isEmpty()) refreshOnce()
        return liveServers.value
    }

    /** One polling cycle: fetch, evaluate, persist, alert. Also used by the widget worker. */
    suspend fun refreshOnce() {
        val settings = latestSettings.value
        val entities = serverDao.getServers()
        if (entities.isEmpty()) {
            liveServers.value = emptyList()
            return
        }
        val now = System.currentTimeMillis()

        val updated = entities.map { entity ->
            val server = entity.toServer()
            if (isMockServer(entity, settings)) {
                val metrics = mockGenerator.nextMetrics(server)
                server.copy(
                    status = statusEvaluator.evaluate(metrics, settings.thresholds),
                    metrics = metrics
                )
            } else {
                try {
                    val metrics = apiFactory.create(entity.baseUrl!!, entity.apiKey).getMetrics()
                    server.copy(
                        status = statusEvaluator.evaluate(metrics, settings.thresholds),
                        metrics = metrics
                    )
                } catch (e: Exception) {
                    server.copy(status = ServerStatus.OFFLINE)
                }
            }
        }

        liveServers.value = updated
        metricDao.insertSnapshots(updated.map { it.toSnapshot(now) })
        raiseAlerts(updated, entities, settings, now)
    }

    private suspend fun raiseAlerts(
        servers: List<Server>,
        entities: List<ServerEntity>,
        settings: MonitoringSettings,
        now: Long
    ) {
        for (server in servers) {
            val entity = entities.firstOrNull { it.id == server.id } ?: continue
            val previous = previousStatuses[server.id]

            val candidates = when {
                server.status == ServerStatus.OFFLINE && !isMockServer(entity, settings) ->
                    listOf(statusEvaluator.offlineCandidate(server.id, server.name, entity.baseUrl ?: ""))

                else -> statusEvaluator.alertCandidates(
                    serverId = server.id,
                    serverName = server.name,
                    metrics = server.metrics,
                    previousStatus = previous,
                    thresholds = settings.thresholds
                )
            }

            for (candidate in candidates) {
                if (alertDao.countRecentWithKey(candidate.dedupeKey, now - DEDUPE_WINDOW_MS) > 0) continue
                alertDao.insertAlert(
                    AlertEntity(
                        dedupeKey = candidate.dedupeKey,
                        serverId = server.id,
                        serverName = server.name,
                        severity = candidate.severity,
                        message = candidate.message,
                        timestamp = now
                    )
                )
                if (settings.notificationsEnabled && candidate.severity != AlertSeverity.INFO) {
                    notificationHelper.showAlert(
                        notificationId = candidate.dedupeKey.hashCode(),
                        serverId = server.id,
                        serverName = server.name,
                        severity = candidate.severity,
                        message = candidate.message,
                        soundEnabled = settings.soundEnabled
                    )
                }
            }

            previousStatuses[server.id] = server.status
        }
    }

    private fun isMockServer(entity: ServerEntity, settings: MonitoringSettings): Boolean =
        settings.mockModeEnabled || entity.isMock || entity.baseUrl.isNullOrBlank()

    // ---- Server management ----

    fun observeServerEntities(): Flow<List<ServerEntity>> = serverDao.observeServers()

    suspend fun addServer(
        name: String,
        ipAddress: String,
        location: String,
        type: String,
        baseUrl: String?,
        apiKey: String?
    ): Result<Unit> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("Name cannot be empty"))
        if (ipAddress.isBlank()) return Result.failure(IllegalArgumentException("IP address cannot be empty"))
        if (!baseUrl.isNullOrBlank() && !baseUrl.startsWith("http")) {
            return Result.failure(IllegalArgumentException("Base URL must start with http:// or https://"))
        }
        serverDao.upsertServer(
            ServerEntity(
                id = "srv-${UUID.randomUUID().toString().take(8)}",
                name = name.trim(),
                ipAddress = ipAddress.trim(),
                location = location.trim().ifBlank { "Unknown" },
                type = type.trim().ifBlank { "Generic" },
                baseUrl = baseUrl?.trim()?.takeIf { it.isNotBlank() },
                apiKey = apiKey?.trim()?.takeIf { it.isNotBlank() },
                isMock = baseUrl.isNullOrBlank()
            )
        )
        return Result.success(Unit)
    }

    suspend fun deleteServer(serverId: String) {
        serverDao.getServer(serverId)?.let { serverDao.deleteServer(it) }
        serverDao.deleteSnapshotsFor(serverId)
        serverDao.deleteAlertsFor(serverId)
        serverDao.deleteActionsFor(serverId)
        mockGenerator.clear(serverId)
        previousStatuses.remove(serverId)
        liveServers.value = liveServers.value.filterNot { it.id == serverId }
    }

    // ---- Alerts ----

    fun observeAlerts(limit: Int = 200): Flow<List<AlertItem>> =
        alertDao.observeAlerts(limit).map { list -> list.map { it.toAlertItem() } }

    fun observeAlertsFor(serverId: String, limit: Int = 50): Flow<List<AlertItem>> =
        alertDao.observeAlertsFor(serverId, limit).map { list -> list.map { it.toAlertItem() } }

    fun observeUnreadCount(): Flow<Int> = alertDao.observeUnreadCount()

    suspend fun markAlertRead(alertId: Long) = alertDao.markRead(alertId)

    suspend fun markAllAlertsRead() = alertDao.markAllRead()

    // ---- History ----

    fun observeHistory(serverId: String, since: Long): Flow<List<MetricSnapshotEntity>> =
        metricDao.observeHistory(serverId, since)

    // ---- Processes & remote actions ----

    suspend fun getProcesses(serverId: String): List<ProcessInfo> {
        val settings = latestSettings.value
        val entity = serverDao.getServer(serverId) ?: return MockMetricsGenerator.demoProcesses()
        if (isMockServer(entity, settings)) return MockMetricsGenerator.demoProcesses()
        return try {
            apiFactory.create(entity.baseUrl!!, entity.apiKey).getProcesses()
        } catch (e: Exception) {
            MockMetricsGenerator.demoProcesses()
        }
    }

    suspend fun performRemoteAction(serverId: String, action: String, target: String): Result<Unit> {
        val settings = latestSettings.value
        val entity = serverDao.getServer(serverId)
            ?: return Result.failure(IllegalArgumentException("Unknown server"))
        val serverName = liveServers.value.find { it.id == serverId }?.name ?: serverId

        val actionId = actionDao.insertAction(
            RemoteActionEntity(
                serverId = serverId,
                serverName = serverName,
                action = action,
                target = target,
                status = ACTION_REQUESTED
            )
        )

        return if (isMockServer(entity, settings)) {
            actionDao.updateStatus(actionId, ACTION_SUCCESS)
            Result.success(Unit)
        } else {
            try {
                apiFactory.create(entity.baseUrl!!, entity.apiKey)
                    .performAction(RemoteActionRequest(action = action, target = target))
                actionDao.updateStatus(actionId, ACTION_SUCCESS)
                Result.success(Unit)
            } catch (e: Exception) {
                actionDao.updateStatus(actionId, ACTION_FAILED)
                Result.failure(e)
            }
        }
    }

    fun observeActionsFor(serverId: String, limit: Int = 20): Flow<List<RemoteActionEntity>> =
        actionDao.observeActionsFor(serverId, limit)

    /** Housekeeping for the periodic worker: drop data older than the longest chart range. */
    suspend fun pruneOldData() {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 3_600_000L
        metricDao.deleteOlderThan(now - 8 * dayMs)
        alertDao.deleteOlderThan(now - 30 * dayMs)
        actionDao.deleteOlderThan(now - 30 * dayMs)
    }

    // ---- Seeding ----

    private suspend fun seedIfEmpty() {
        if (serverDao.count() > 0) return
        DEMO_SERVERS.forEach { serverDao.upsertServer(it) }
        val now = System.currentTimeMillis()
        DEMO_ALERTS(now).forEach { alertDao.insertAlert(it) }
    }

    companion object {
        const val DEDUPE_WINDOW_MS = 15 * 60_000L
        const val ACTION_REQUESTED = "REQUESTED"
        const val ACTION_SUCCESS = "SUCCESS"
        const val ACTION_FAILED = "FAILED"

        val DEMO_SERVERS = listOf(
            ServerEntity("srv-001", "Web Server", "192.168.1.10", "US-East", "NGINX"),
            ServerEntity("srv-002", "Database", "192.168.1.20", "US-East", "PostgreSQL"),
            ServerEntity("srv-003", "API Gateway", "192.168.1.30", "EU-West", "Kong"),
            ServerEntity("srv-004", "Cache Server", "192.168.1.40", "US-West", "Redis"),
            ServerEntity("srv-005", "File Storage", "192.168.1.50", "AP-South", "MinIO")
        )

        fun DEMO_ALERTS(now: Long) = listOf(
            AlertEntity(0, "seed-1", "srv-002", "Database", AlertSeverity.CRITICAL, "CPU usage exceeded 90%", now - 120_000),
            AlertEntity(0, "seed-2", "srv-005", "File Storage", AlertSeverity.WARNING, "Disk usage above 75%", now - 300_000, isRead = true),
            AlertEntity(0, "seed-3", "srv-001", "Web Server", AlertSeverity.INFO, "SSL certificate renews in 7 days", now - 600_000, isRead = true),
            AlertEntity(0, "seed-4", "srv-003", "API Gateway", AlertSeverity.WARNING, "Response time spike detected", now - 900_000),
            AlertEntity(0, "seed-5", "srv-004", "Cache Server", AlertSeverity.CRITICAL, "Memory usage critical", now - 1_800_000, isRead = true),
            AlertEntity(0, "seed-6", "srv-002", "Database", AlertSeverity.INFO, "Backup completed successfully", now - 3_600_000, isRead = true)
        )
    }
}

fun ServerEntity.toServer() = Server(
    id = id,
    name = name,
    ipAddress = ipAddress,
    location = location,
    type = type
)

fun Server.toSnapshot(timestamp: Long) = MetricSnapshotEntity(
    serverId = id,
    timestamp = timestamp,
    cpuUsage = metrics.cpuUsage,
    memoryUsage = metrics.memoryUsage,
    diskUsage = metrics.diskUsage,
    networkIn = metrics.networkIn,
    networkOut = metrics.networkOut,
    responseTime = metrics.responseTime
)

fun AlertEntity.toAlertItem() = AlertItem(
    id = "db-$id",
    serverId = serverId,
    serverName = serverName,
    severity = severity,
    message = message,
    timestamp = timestamp,
    dbId = id,
    isRead = isRead
)
