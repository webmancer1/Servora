package com.example.servora.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servora.data.model.AlertItem
import com.example.servora.data.model.DashboardSummary
import com.example.servora.data.model.Server
import com.example.servora.data.model.ServerStatus
import com.example.servora.data.repository.ServerRepository
import com.example.servora.data.settings.GroupingMode
import com.example.servora.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val servers: List<Server> = emptyList(),
    val alerts: List<AlertItem> = emptyList(),
    val summary: DashboardSummary = DashboardSummary(0, 0, 0, 0, 0, 0f, 0f),
    val searchQuery: String = "",
    val statusFilter: ServerStatus? = null,
    val groupingMode: GroupingMode = GroupingMode.NONE,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ServerRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val statusFilter = MutableStateFlow<ServerStatus?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.servers,
        repository.observeAlerts(limit = 6),
        settingsRepository.settings,
        searchQuery,
        statusFilter
    ) { servers, alerts, settings, query, filter ->
        val visible = servers.filter { server ->
            val matchesQuery = query.isBlank() ||
                server.name.contains(query, ignoreCase = true) ||
                server.ipAddress.contains(query, ignoreCase = true) ||
                server.type.contains(query, ignoreCase = true)
            val matchesFilter = filter == null || server.status == filter
            matchesQuery && matchesFilter
        }
        DashboardUiState(
            servers = visible,
            alerts = alerts,
            summary = summaryOf(servers),
            searchQuery = query,
            statusFilter = filter,
            groupingMode = settings.groupingMode,
            isLoading = servers.isEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setStatusFilter(status: ServerStatus?) {
        statusFilter.value = status
    }

    private fun summaryOf(servers: List<Server>): DashboardSummary {
        if (servers.isEmpty()) return DashboardSummary(0, 0, 0, 0, 0, 0f, 0f)
        return DashboardSummary(
            totalServers = servers.size,
            onlineCount = servers.count { it.status == ServerStatus.ONLINE },
            warningCount = servers.count { it.status == ServerStatus.WARNING },
            criticalCount = servers.count { it.status == ServerStatus.CRITICAL },
            offlineCount = servers.count { it.status == ServerStatus.OFFLINE },
            averageCpu = servers.map { it.metrics.cpuUsage }.average().toFloat(),
            averageMemory = servers.map { it.metrics.memoryUsage }.average().toFloat()
        )
    }
}
