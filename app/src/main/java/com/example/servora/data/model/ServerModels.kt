package com.example.servora.data.model

import kotlinx.serialization.Serializable

enum class ServerStatus {
    ONLINE, WARNING, CRITICAL, OFFLINE
}

enum class AlertSeverity {
    INFO, WARNING, CRITICAL
}

@Serializable
data class Server(
    val id: String,
    val name: String,
    val ipAddress: String,
    val location: String,
    val type: String,
    val status: ServerStatus = ServerStatus.ONLINE,
    val metrics: ServerMetrics = ServerMetrics()
)

@Serializable
data class ServerMetrics(
    val cpuUsage: Float = 0f,
    val memoryUsage: Float = 0f,
    val memoryTotal: Float = 32f,
    val memoryUsed: Float = 0f,
    val diskUsage: Float = 0f,
    val diskTotal: Float = 500f,
    val diskUsed: Float = 0f,
    val networkIn: Float = 0f,
    val networkOut: Float = 0f,
    val uptime: Long = 0L,
    val responseTime: Int = 0,
    val requestsPerSecond: Int = 0,
    val activeConnections: Int = 0,
    val cpuHistory: List<Float> = emptyList(),
    val networkInHistory: List<Float> = emptyList(),
    val networkOutHistory: List<Float> = emptyList()
)

@Serializable
data class AlertItem(
    val id: String,
    val serverId: String,
    val serverName: String,
    val severity: AlertSeverity,
    val message: String,
    val timestamp: Long
)

@Serializable
data class ProcessInfo(
    val name: String,
    val pid: Int,
    val cpuUsage: Float,
    val memoryUsage: Float
)

data class DashboardSummary(
    val totalServers: Int,
    val onlineCount: Int,
    val warningCount: Int,
    val criticalCount: Int,
    val offlineCount: Int,
    val averageCpu: Float,
    val averageMemory: Float
)
