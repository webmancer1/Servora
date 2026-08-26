package com.example.servora.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servora.data.settings.MonitoringSettings
import com.example.servora.data.settings.SettingsRepository
import com.example.servora.data.settings.Thresholds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<MonitoringSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonitoringSettings())

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun setRefreshInterval(intervalMs: Long) {
        viewModelScope.launch { repository.setRefreshInterval(intervalMs) }
    }

    fun setMockModeEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setMockModeEnabled(enabled) }
    }

    fun updateThresholds(cpuWarn: Float, cpuCrit: Float, memWarn: Float, memCrit: Float, diskWarn: Float, diskCrit: Float) {
        viewModelScope.launch {
            repository.setThresholds(
                Thresholds(
                    cpuWarning = cpuWarn,
                    cpuCritical = cpuCrit,
                    memoryWarning = memWarn,
                    memoryCritical = memCrit,
                    diskWarning = diskWarn,
                    diskCritical = diskCrit
                )
            )
        }
    }
}
