package com.example.coinspirit2.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinspirit2.data.repo.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AuthState {
    data object Idle: AuthState
    data object Loading: AuthState
    data class Error(val message: String): AuthState
    data object Success: AuthState
}

class AuthViewModel(
    private val repo: AuthRepository
): ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            runCatching { repo.login(email, pass) }
                .onSuccess { _state.value = AuthState.Success }
                .onFailure { _state.value = AuthState.Error(it.message ?: "Ошибка") }
        }
    }

    fun register(email: String, pass: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            runCatching { repo.register(email, pass) }
                .onSuccess { _state.value = AuthState.Success }
                .onFailure { _state.value = AuthState.Error(it.message ?: "Ошибка") }
        }
    }
}
