package com.example.servora.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servora.data.repository.AccountRepository
import com.example.servora.data.repository.UserSettings
import com.example.servora.data.repository.AuthRepository
import com.example.servora.data.repository.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val userSettings: StateFlow<UserSettings?> = accountRepository.getUserSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        
    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _updateStatus = MutableStateFlow<Result<Unit>?>(null)
    val updateStatus: StateFlow<Result<Unit>?> = _updateStatus.asStateFlow()

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            val result = accountRepository.updateDisplayName(newName)
            _updateStatus.value = result
        }
    }

    fun toggleTwoFactorAuth(isEnabled: Boolean) {
        viewModelScope.launch {
            val result = accountRepository.updateSetting("twoFactorAuth", isEnabled)
            _updateStatus.value = result
        }
    }

    fun updateDataRegion(region: String) {
        viewModelScope.launch {
            val result = accountRepository.updateSetting("dataRegion", region)
            _updateStatus.value = result
        }
    }

    fun clearStatus() {
        _updateStatus.value = null
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
