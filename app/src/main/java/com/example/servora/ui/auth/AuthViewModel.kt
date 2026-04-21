package com.example.servora.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isCheckingAuth = MutableStateFlow(true)
    val isCheckingAuth: StateFlow<Boolean> = _isCheckingAuth.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.isLoggedIn.collect {
                _isCheckingAuth.value = false
            }
        }
    }

    fun signUp(name: String, email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signUp(name, email.trim(), password)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success },
                onFailure = { AuthState.Error(it.message ?: "Sign up failed") }
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(email.trim(), password)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success },
                onFailure = { AuthState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signInWithGoogle(idToken)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success },
                onFailure = { AuthState.Error(it.message ?: "Google Sign-In failed") }
            )
        }
    }

    fun setAuthError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
