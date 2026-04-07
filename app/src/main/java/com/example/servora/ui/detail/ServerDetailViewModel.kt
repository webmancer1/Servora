package com.example.servora.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servora.data.model.AlertItem
import com.example.servora.data.model.ProcessInfo
import com.example.servora.data.model.Server
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
    val isLoading: Boolean = true
)

@HiltViewModel
class ServerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ServerRepository
) : ViewModel() {

    private val serverId: String = savedStateHandle.get<String>("serverId") ?: ""

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        observeServer()
        loadProcesses()
        loadAlerts()
    }

    private fun observeServer() {
        viewModelScope.launch {
            repository.getServersFlow().collect { servers ->
                val server = servers.find { it.id == serverId }
                _uiState.value = _uiState.value.copy(
                    server = server,
                    isLoading = false
                )
            }
        }
    }

    private fun loadProcesses() {
        _uiState.value = _uiState.value.copy(
            processes = repository.getProcesses(serverId)
        )
    }

    private fun loadAlerts() {
        val allAlerts = repository.getAlerts()
        _uiState.value = _uiState.value.copy(
            alerts = allAlerts.filter { it.serverId == serverId }
        )
    }
}
