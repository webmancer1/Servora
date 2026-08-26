package com.example.servora.domain

import com.example.servora.data.model.AlertSeverity
import com.example.servora.data.model.Server
import com.example.servora.data.model.ServerMetrics
import com.example.servora.data.model.ServerStatus
import com.example.servora.data.settings.Thresholds
import javax.inject.Inject
import javax.inject.Singleton

data class AlertCandidate(
    val dedupeKey: String,
    val severity: AlertSeverity,
    val message: String
)

/**
 * Derives a server's status from its metrics and configured thresholds, and
 * produces the alert candidates that should be raised for a status change.
 * Pure Kotlin so threshold behaviour is unit-testable.
 */
@Singleton
class StatusEvaluator @Inject constructor() {

    fun evaluate(metrics: ServerMetrics, thresholds: Thresholds): ServerStatus {
        val cpu = metrics.cpuUsage
        val memory = metrics.memoryUsage
        val disk = metrics.diskUsage
        return when {
            cpu >= thresholds.cpuCritical ||
                memory >= thresholds.memoryCritical ||
                disk >= thresholds.diskCritical -> ServerStatus.CRITICAL

            cpu >= thresholds.cpuWarning ||
                memory >= thresholds.memoryWarning ||
                disk >= thresholds.diskWarning -> ServerStatus.WARNING

            else -> ServerStatus.ONLINE
        }
    }

    fun alertCandidates(
        serverId: String,
        serverName: String,
        metrics: ServerMetrics,
        previousStatus: ServerStatus?,
        thresholds: Thresholds
    ): List<AlertCandidate> {
        val status = evaluate(metrics, thresholds)
        val candidates = mutableListOf<AlertCandidate>()

        if (status == ServerStatus.CRITICAL || status == ServerStatus.WARNING) {
            val severity = if (status == ServerStatus.CRITICAL) AlertSeverity.CRITICAL else AlertSeverity.WARNING

            if (metrics.cpuUsage >= thresholds.cpuCritical) {
                candidates += AlertCandidate(
                    dedupeKey = "$serverId:cpu:critical",
                    severity = AlertSeverity.CRITICAL,
                    message = "CPU usage at ${metrics.cpuUsage.toInt()}% (critical)"
                )
            } else if (metrics.cpuUsage >= thresholds.cpuWarning) {
                candidates += AlertCandidate(
                    dedupeKey = "$serverId:cpu:warning",
                    severity = AlertSeverity.WARNING,
                    message = "CPU usage at ${metrics.cpuUsage.toInt()}% (above ${thresholds.cpuWarning.toInt()}%)"
                )
            }

            if (metrics.memoryUsage >= thresholds.memoryCritical) {
                candidates += AlertCandidate(
                    dedupeKey = "$serverId:memory:critical",
                    severity = AlertSeverity.CRITICAL,
                    message = "Memory usage at ${metrics.memoryUsage.toInt()}% (critical)"
                )
            } else if (metrics.memoryUsage >= thresholds.memoryWarning) {
                candidates += AlertCandidate(
                    dedupeKey = "$serverId:memory:warning",
                    severity = AlertSeverity.WARNING,
                    message = "Memory usage at ${metrics.memoryUsage.toInt()}% (above ${thresholds.memoryWarning.toInt()}%)"
                )
            }

            if (metrics.diskUsage >= thresholds.diskCritical) {
                candidates += AlertCandidate(
                    dedupeKey = "$serverId:disk:critical",
                    severity = AlertSeverity.CRITICAL,
                    message = "Disk usage at ${metrics.diskUsage.toInt()}% (critical)"
                )
            } else if (metrics.diskUsage >= thresholds.diskWarning) {
                candidates += AlertCandidate(
                    dedupeKey = "$serverId:disk:warning",
                    severity = AlertSeverity.WARNING,
                    message = "Disk usage at ${metrics.diskUsage.toInt()}% (above ${thresholds.diskWarning.toInt()}%)"
                )
            }

            // Only announce a breach on the transition into a degraded state to avoid spam.
            if (previousStatus == ServerStatus.ONLINE && candidates.isEmpty()) {
                candidates += AlertCandidate(
                    dedupeKey = "$serverId:status:$status",
                    severity = severity,
                    message = "$serverName entered ${status.name.lowercase()} state"
                )
            }
        } else if (previousStatus != null && previousStatus != ServerStatus.ONLINE) {
            candidates += AlertCandidate(
                dedupeKey = "$serverId:recovered",
                severity = AlertSeverity.INFO,
                message = "$serverName recovered — all metrics back within thresholds"
            )
        }

        return candidates
    }

    fun offlineCandidate(serverId: String, serverName: String, baseUrl: String): AlertCandidate =
        AlertCandidate(
            dedupeKey = "$serverId:offline",
            severity = AlertSeverity.CRITICAL,
            message = "Unreachable at $baseUrl — check the agent or network"
        )

    fun summaryTotals(servers: List<Server>): Map<ServerStatus, Int> =
        ServerStatus.entries.associateWith { status -> servers.count { it.status == status } }
}
