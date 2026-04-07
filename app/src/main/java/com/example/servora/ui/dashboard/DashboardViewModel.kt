package com.example.servora.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servora.data.model.AlertItem
import com.example.servora.data.model.DashboardSummary
import com.example.servora.data.model.Server
import com.example.servora.data.model.ServerStatus
import com.example.servora.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val servers: List<Server> = emptyList(),
    val alerts: List<AlertItem> = emptyList(),
    val summary: DashboardSummary = DashboardSummary(0, 0, 0, 0, 0, 0f, 0f),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ServerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeServers()
        loadAlerts()
    }

    private fun observeServers() {
        viewModelScope.launch {
            repository.getServersFlow().collect { servers ->
                val summary = DashboardSummary(
                    totalServers = servers.size,
                    onlineCount = servers.count { it.status == ServerStatus.ONLINE },
                    warningCount = servers.count { it.status == ServerStatus.WARNING },
                    criticalCount = servers.count { it.status == ServerStatus.CRITICAL },
                    offlineCount = servers.count { it.status == ServerStatus.OFFLINE },
                    averageCpu = servers.map { it.metrics.cpuUsage }.average().toFloat(),
                    averageMemory = servers.map { it.metrics.memoryUsage }.average().toFloat()
                )
                _uiState.value = _uiState.value.copy(
                    servers = servers,
                    summary = summary,
                    isLoading = false
                )
            }
        }
    }

    private fun loadAlerts() {
        _uiState.value = _uiState.value.copy(
            alerts = repository.getAlerts()
        )
    }
}
