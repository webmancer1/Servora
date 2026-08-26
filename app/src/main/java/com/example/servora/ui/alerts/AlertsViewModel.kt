package com.example.servora.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servora.data.model.AlertItem
import com.example.servora.data.model.AlertSeverity
import com.example.servora.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AlertFilter(val label: String) {
    ALL("All"), UNREAD("Unread"), CRITICAL("Critical"), WARNING("Warning"), INFO("Info")
}

data class AlertsUiState(
    val alerts: List<AlertItem> = emptyList(),
    val filter: AlertFilter = AlertFilter.ALL,
    val unreadCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val repository: ServerRepository
) : ViewModel() {

    private val filter = MutableStateFlow(AlertFilter.ALL)

    val uiState: StateFlow<AlertsUiState> =
        combine(repository.observeAlerts(), repository.observeUnreadCount(), filter) { alerts, unread, selected ->
            AlertsUiState(
                alerts = alerts.filter { alert ->
                    when (selected) {
                        AlertFilter.ALL -> true
                        AlertFilter.UNREAD -> !alert.isRead
                        AlertFilter.CRITICAL -> alert.severity == AlertSeverity.CRITICAL
                        AlertFilter.WARNING -> alert.severity == AlertSeverity.WARNING
                        AlertFilter.INFO -> alert.severity == AlertSeverity.INFO
                    }
                },
                filter = selected,
                unreadCount = unread,
                isLoading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlertsUiState())

    fun setFilter(newFilter: AlertFilter) {
        filter.value = newFilter
    }

    fun markRead(alert: AlertItem) {
        if (alert.dbId > 0L) {
            viewModelScope.launch { repository.markAlertRead(alert.dbId) }
        }
    }

    fun markAllRead() {
        viewModelScope.launch { repository.markAllAlertsRead() }
    }
}
