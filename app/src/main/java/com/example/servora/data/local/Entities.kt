package com.example.servora.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.servora.data.model.AlertSeverity

@Entity(tableName = "servers")
data class ServerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val ipAddress: String,
    val location: String,
    val type: String,
    val baseUrl: String? = null,
    val apiKey: String? = null,
    val isMock: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "metric_snapshots",
    indices = [Index(value = ["serverId", "timestamp"])]
)
data class MetricSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val serverId: String,
    val timestamp: Long,
    val cpuUsage: Float,
    val memoryUsage: Float,
    val diskUsage: Float,
    val networkIn: Float,
    val networkOut: Float,
    val responseTime: Int
)

@Entity(tableName = "alerts", indices = [Index(value = ["serverId"])])
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dedupeKey: String,
    val serverId: String,
    val serverName: String,
    val severity: AlertSeverity,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

@Entity(tableName = "remote_actions")
data class RemoteActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val serverId: String,
    val serverName: String,
    val action: String,
    val target: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)
