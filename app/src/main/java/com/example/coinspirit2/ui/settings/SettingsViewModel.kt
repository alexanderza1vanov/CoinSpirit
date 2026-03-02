package com.example.coinspirit2.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinspirit2.core.di.ServiceLocator
import com.example.coinspirit2.data.remote.MeDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MeUi(
    val displayName: String,
    val email: String
)

class SettingsViewModel : ViewModel() {
    private val api = ServiceLocator.api
    private val tokenStore = ServiceLocator.tokenStore

    private val _user = MutableStateFlow<MeUi?>(null)
    val user: StateFlow<MeUi?> = _user.asStateFlow()

    private val _done = MutableStateFlow(false)
    val done: StateFlow<Boolean> = _done.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Загрузка профиля текущего пользователя */
    fun loadMe() = viewModelScope.launch {
        runCatching { api.me() }
            .onSuccess { dto: MeDTO ->
                val shownName = (dto.name?.takeIf { it.isNotBlank() }
                    ?: dto.email.substringBefore('@')).ifBlank { dto.email }
                _user.value = MeUi(displayName = shownName, email = dto.email)
            }
            .onFailure { e -> _error.value = e.message }
    }

    /** Выход из аккаунта */
    fun logout() = viewModelScope.launch {
        runCatching {
            withContext(Dispatchers.IO) { tokenStore.clear() }
        }.onSuccess {
            _done.value = true
        }.onFailure { e -> _error.value = e.message }
    }
}