package com.example.servora.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "servora_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val CPU_WARNING = floatPreferencesKey("cpu_warning")
        val CPU_CRITICAL = floatPreferencesKey("cpu_critical")
        val MEMORY_WARNING = floatPreferencesKey("memory_warning")
        val MEMORY_CRITICAL = floatPreferencesKey("memory_critical")
        val DISK_WARNING = floatPreferencesKey("disk_warning")
        val DISK_CRITICAL = floatPreferencesKey("disk_critical")
        val REFRESH_INTERVAL = longPreferencesKey("refresh_interval_ms")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MOCK_MODE = booleanPreferencesKey("mock_mode")
        val GROUPING_MODE = stringPreferencesKey("grouping_mode")
    }

    val settings: Flow<MonitoringSettings> = context.settingsDataStore.data.map { prefs ->
        MonitoringSettings(
            thresholds = Thresholds(
                cpuWarning = prefs[Keys.CPU_WARNING] ?: 70f,
                cpuCritical = prefs[Keys.CPU_CRITICAL] ?: 90f,
                memoryWarning = prefs[Keys.MEMORY_WARNING] ?: 75f,
                memoryCritical = prefs[Keys.MEMORY_CRITICAL] ?: 90f,
                diskWarning = prefs[Keys.DISK_WARNING] ?: 80f,
                diskCritical = prefs[Keys.DISK_CRITICAL] ?: 95f
            ),
            refreshIntervalMs = prefs[Keys.REFRESH_INTERVAL] ?: 3_000L,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: false,
            mockModeEnabled = prefs[Keys.MOCK_MODE] ?: true,
            groupingMode = runCatching {
                GroupingMode.valueOf(prefs[Keys.GROUPING_MODE] ?: GroupingMode.NONE.name)
            }.getOrDefault(GroupingMode.NONE)
        )
    }

    suspend fun setThresholds(thresholds: Thresholds) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.CPU_WARNING] = thresholds.cpuWarning
            prefs[Keys.CPU_CRITICAL] = thresholds.cpuCritical
            prefs[Keys.MEMORY_WARNING] = thresholds.memoryWarning
            prefs[Keys.MEMORY_CRITICAL] = thresholds.memoryCritical
            prefs[Keys.DISK_WARNING] = thresholds.diskWarning
            prefs[Keys.DISK_CRITICAL] = thresholds.diskCritical
        }
    }

    suspend fun setRefreshInterval(intervalMs: Long) {
        context.settingsDataStore.edit { it[Keys.REFRESH_INTERVAL] = intervalMs.coerceIn(1_000L, 300_000L) }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setMockModeEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.MOCK_MODE] = enabled }
    }

    suspend fun setGroupingMode(mode: GroupingMode) {
        context.settingsDataStore.edit { it[Keys.GROUPING_MODE] = mode.name }
    }
}
