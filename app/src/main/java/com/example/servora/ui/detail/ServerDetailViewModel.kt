package com.example.servora.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servora.data.model.AlertItem
import com.example.servora.data.model.ProcessInfo
import com.example.servora.data.model.Server
import com.example.servora.data.remote.RemoteAction
import com.example.servora.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val server: Server? = null,
    val processes: List<ProcessInfo> = emptyList(),
    val alerts: List<AlertItem> = emptyList(),
    val actionMessage: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ServerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ServerRepository
) : ViewModel() {

    val serverId: String = savedStateHandle.get<String>("serverId") ?: ""

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        observeServer()
        observeAlerts()
        loadProcesses()
    }

    private fun observeServer() {
        viewModelScope.launch {
            repository.observeServer(serverId).collect { server ->
                _uiState.value = _uiState.value.copy(
                    server = server,
                    isLoading = false
                )
            }
        }
    }

    private fun observeAlerts() {
        viewModelScope.launch {
            repository.observeAlertsFor(serverId).collect { alerts ->
                _uiState.value = _uiState.value.copy(alerts = alerts)
            }
        }
    }

    fun loadProcesses() {
        viewModelScope.launch {
            val list = repository.getProcesses(serverId)
            _uiState.value = _uiState.value.copy(processes = list)
        }
    }

    fun rebootServer() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionMessage = "Sending reboot command...")
            repository.performRemoteAction(serverId, RemoteAction.REBOOT, "")
                .onSuccess {
                    _uiState.value = _uiState.value.copy(actionMessage = "Reboot command sent successfully")
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(actionMessage = e.message ?: "Failed to reboot server")
                }
        }
    }

    fun killProcess(pid: Int, processName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionMessage = "Sending kill signal to PID $pid ($processName)...")
            repository.performRemoteAction(serverId, RemoteAction.KILL_PROCESS, pid.toString())
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        actionMessage = "Process $processName (PID $pid) terminated",
                        processes = _uiState.value.processes.filterNot { it.pid == pid }
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(actionMessage = e.message ?: "Failed to terminate process")
                }
        }
    }

    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null)
    }
}

