package com.example.servora.ui.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servora.data.local.ServerEntity
import com.example.servora.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddServerUiState(
    val isVisible: Boolean = false,
    val name: String = "",
    val ipAddress: String = "",
    val location: String = "",
    val type: String = "",
    val baseUrl: String = "",
    val apiKey: String = "",
    val error: String? = null
)

data class ManageServersUiState(
    val servers: List<ServerEntity> = emptyList(),
    val addState: AddServerUiState = AddServerUiState(),
    val serverPendingDelete: ServerEntity? = null
)

@HiltViewModel
class ManageServersViewModel @Inject constructor(
    private val repository: ServerRepository
) : ViewModel() {

    private val addState = MutableStateFlow(AddServerUiState())
    private val pendingDelete = MutableStateFlow<ServerEntity?>(null)

    val uiState: StateFlow<ManageServersUiState> =
        kotlinx.coroutines.flow.combine(
            repository.observeServerEntities(),
            addState,
            pendingDelete
        ) { servers, add, delete ->
            ManageServersUiState(servers = servers, addState = add, serverPendingDelete = delete)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManageServersUiState())

    fun showAddDialog() {
        addState.value = AddServerUiState(isVisible = true)
    }

    fun dismissAddDialog() {
        addState.value = AddServerUiState()
    }

    fun updateAddForm(update: (AddServerUiState) -> AddServerUiState) {
        addState.value = update(addState.value)
    }

    fun saveServer() {
        val form = addState.value
        viewModelScope.launch {
            repository.addServer(form.name, form.ipAddress, form.location, form.type, form.baseUrl, form.apiKey)
                .onSuccess { addState.value = AddServerUiState() }
                .onFailure { e ->
                    addState.value = form.copy(error = e.message ?: "Could not save server")
                }
        }
    }

    fun requestDelete(server: ServerEntity) {
        pendingDelete.value = server
    }

    fun dismissDelete() {
        pendingDelete.value = null
    }

    fun confirmDelete() {
        val server = pendingDelete.value ?: return
        viewModelScope.launch {
            repository.deleteServer(server.id)
            pendingDelete.value = null
        }
    }
}
