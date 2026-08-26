package com.example.servora.data.settings

data class Thresholds(
    val cpuWarning: Float = 70f,
    val cpuCritical: Float = 90f,
    val memoryWarning: Float = 75f,
    val memoryCritical: Float = 90f,
    val diskWarning: Float = 80f,
    val diskCritical: Float = 95f
)

enum class GroupingMode {
    NONE, LOCATION, TYPE
}

data class MonitoringSettings(
    val thresholds: Thresholds = Thresholds(),
    val refreshIntervalMs: Long = 3_000L,
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val mockModeEnabled: Boolean = true,
    val groupingMode: GroupingMode = GroupingMode.NONE
)
