package com.example.coinspirit2.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinspirit2.data.remote.dto.TokenResponse
import com.example.coinspirit2.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val tokens: TokenResponse? = null,
    val error: String? = null
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun register(email: String, password: String) {
        _state.value = AuthState(isLoading = true)
        viewModelScope.launch {
            repo.register(email, password)
                .onSuccess { _state.value = AuthState(tokens = it) }
                .onFailure { _state.value = AuthState(error = it.message) }
        }
    }

    fun login(email: String, password: String) {
        _state.value = AuthState(isLoading = true)
        viewModelScope.launch {
            repo.login(email, password)
                .onSuccess { _state.value = AuthState(tokens = it) }
                .onFailure { _state.value = AuthState(error = it.message) }
        }
    }
}
