package com.example.servora.data.repository

import com.example.servora.data.model.AlertItem
import com.example.servora.data.model.AlertSeverity
import com.example.servora.data.model.ProcessInfo
import com.example.servora.data.model.Server
import com.example.servora.data.model.ServerMetrics
import com.example.servora.data.model.ServerStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class ServerRepository @Inject constructor() {

    private val baseServers = listOf(
        Server(
            id = "srv-001",
            name = "Web Server",
            ipAddress = "192.168.1.10",
            location = "US-East",
            type = "NGINX"
        ),
        Server(
            id = "srv-002",
            name = "Database",
            ipAddress = "192.168.1.20",
            location = "US-East",
            type = "PostgreSQL"
        ),
        Server(
            id = "srv-003",
            name = "API Gateway",
            ipAddress = "192.168.1.30",
            location = "EU-West",
            type = "Kong"
        ),
        Server(
            id = "srv-004",
            name = "Cache Server",
            ipAddress = "192.168.1.40",
            location = "US-West",
            type = "Redis"
        ),
        Server(
            id = "srv-005",
            name = "File Storage",
            ipAddress = "192.168.1.50",
            location = "AP-South",
            type = "MinIO"
        )
    )

    private val cpuHistories = mutableMapOf<String, MutableList<Float>>()
    private val netInHistories = mutableMapOf<String, MutableList<Float>>()
    private val netOutHistories = mutableMapOf<String, MutableList<Float>>()

    init {
        baseServers.forEach { server ->
            cpuHistories[server.id] = MutableList(20) { Random.nextFloat() * 60f + 20f }
            netInHistories[server.id] = MutableList(20) { Random.nextFloat() * 500f + 100f }
            netOutHistories[server.id] = MutableList(20) { Random.nextFloat() * 300f + 50f }
        }
    }

    fun getServersFlow(intervalMs: Long = 3000L): Flow<List<Server>> = flow {
        while (true) {
            val servers = baseServers.map { server ->
                val cpuBase = when (server.id) {
                    "srv-001" -> 45f
                    "srv-002" -> 72f
                    "srv-003" -> 35f
                    "srv-004" -> 25f
                    "srv-005" -> 55f
                    else -> 50f
                }
                val cpu = (cpuBase + Random.nextFloat() * 20f - 10f).coerceIn(5f, 98f)
                val memory = when (server.id) {
                    "srv-002" -> (75f + Random.nextFloat() * 10f).coerceIn(5f, 95f)
                    "srv-004" -> (60f + Random.nextFloat() * 15f).coerceIn(5f, 95f)
                    else -> (45f + Random.nextFloat() * 20f).coerceIn(5f, 95f)
                }
                val disk = when (server.id) {
                    "srv-005" -> (70f + Random.nextFloat() * 5f).coerceIn(5f, 95f)
                    "srv-002" -> (55f + Random.nextFloat() * 5f).coerceIn(5f, 95f)
                    else -> (30f + Random.nextFloat() * 10f).coerceIn(5f, 95f)
                }
                val networkIn = Random.nextFloat() * 800f + 50f
                val networkOut = Random.nextFloat() * 400f + 30f
                val responseTime = (Random.nextInt(200) + 10)

                val status = when {
                    server.id == "srv-005" && Random.nextFloat() < 0.1f -> ServerStatus.OFFLINE
                    cpu > 85f || memory > 90f -> ServerStatus.CRITICAL
                    cpu > 70f || memory > 75f -> ServerStatus.WARNING
                    else -> ServerStatus.ONLINE
                }

                val cpuHist = cpuHistories.getOrPut(server.id) { mutableListOf() }
                cpuHist.add(cpu)
                if (cpuHist.size > 20) cpuHist.removeAt(0)

                val netInHist = netInHistories.getOrPut(server.id) { mutableListOf() }
                netInHist.add(networkIn)
                if (netInHist.size > 20) netInHist.removeAt(0)

                val netOutHist = netOutHistories.getOrPut(server.id) { mutableListOf() }
                netOutHist.add(networkOut)
                if (netOutHist.size > 20) netOutHist.removeAt(0)

                val memTotal = when (server.id) {
                    "srv-002" -> 64f
                    "srv-001" -> 32f
                    else -> 16f
                }

                server.copy(
                    status = status,
                    metrics = ServerMetrics(
                        cpuUsage = cpu,
                        memoryUsage = memory,
                        memoryTotal = memTotal,
                        memoryUsed = memTotal * memory / 100f,
                        diskUsage = disk,
                        diskTotal = if (server.id == "srv-005") 2000f else 500f,
                        diskUsed = (if (server.id == "srv-005") 2000f else 500f) * disk / 100f,
                        networkIn = networkIn,
                        networkOut = networkOut,
                        uptime = when (server.id) {
                            "srv-001" -> 864000L
                            "srv-002" -> 2592000L
                            "srv-003" -> 432000L
                            "srv-004" -> 172800L
                            "srv-005" -> 86400L
                            else -> 100000L
                        },
                        responseTime = responseTime,
                        requestsPerSecond = Random.nextInt(500) + 50,
                        activeConnections = Random.nextInt(200) + 10,
                        cpuHistory = cpuHist.toList(),
                        networkInHistory = netInHist.toList(),
                        networkOutHistory = netOutHist.toList()
                    )
                )
            }
            emit(servers)
            delay(intervalMs)
        }
    }

    fun getAlerts(): List<AlertItem> {
        val now = System.currentTimeMillis()
        return listOf(
            AlertItem("a1", "srv-002", "Database", AlertSeverity.CRITICAL, "CPU usage exceeded 90%", now - 120_000),
            AlertItem("a2", "srv-005", "File Storage", AlertSeverity.WARNING, "Disk usage above 75%", now - 300_000),
            AlertItem("a3", "srv-001", "Web Server", AlertSeverity.INFO, "SSL certificate renews in 7 days", now - 600_000),
            AlertItem("a4", "srv-003", "API Gateway", AlertSeverity.WARNING, "Response time spike detected", now - 900_000),
            AlertItem("a5", "srv-004", "Cache Server", AlertSeverity.CRITICAL, "Memory usage critical", now - 1_800_000),
            AlertItem("a6", "srv-002", "Database", AlertSeverity.INFO, "Backup completed successfully", now - 3_600_000)
        )
    }

    fun getProcesses(serverId: String): List<ProcessInfo> {
        return listOf(
            ProcessInfo("nginx", 1024, Random.nextFloat() * 15f + 5f, Random.nextFloat() * 10f + 2f),
            ProcessInfo("postgres", 2048, Random.nextFloat() * 25f + 10f, Random.nextFloat() * 20f + 10f),
            ProcessInfo("node", 3072, Random.nextFloat() * 12f + 3f, Random.nextFloat() * 8f + 4f),
            ProcessInfo("redis-server", 4096, Random.nextFloat() * 8f + 2f, Random.nextFloat() * 30f + 5f),
            ProcessInfo("python3", 5120, Random.nextFloat() * 10f + 1f, Random.nextFloat() * 6f + 1f)
        )
    }
}
