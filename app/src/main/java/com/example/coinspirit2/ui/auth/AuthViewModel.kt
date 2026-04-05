package com.example.coinspirit2.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinspirit2.core.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {
    private val repo = ServiceLocator.repo
    private val tokenStore = ServiceLocator.tokenStore
    private val TAG = "CoinSpirit.AuthVM"

    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _authorized = MutableStateFlow(false)
    val authorized = _authorized.asStateFlow()

    fun register(email: String, pass: String, name: String?, onOk: () -> Unit) = viewModelScope.launch {
        _busy.value = true; _error.value = null
        Log.d(TAG, "register() -> $email")
        try {
            val t = withContext(Dispatchers.IO) { repo.register(email, pass, name) }
            withContext(Dispatchers.IO) { tokenStore.save(t.accessToken, t.refreshToken) }
            _authorized.value = t.accessToken.isNotBlank()
            onOk()
        } catch (e: Throwable) {
            _authorized.value = false
            _error.value = e.message ?: e.toString()
        } finally { _busy.value = false }
    }

    fun login(email: String, pass: String, onOk: () -> Unit) = viewModelScope.launch {
        _busy.value = true; _error.value = null
        try {
            val t = withContext(Dispatchers.IO) { repo.login(email, pass) }
            withContext(Dispatchers.IO) { tokenStore.save(t.accessToken, t.refreshToken) }
            _authorized.value = t.accessToken.isNotBlank()
            onOk()
        } catch (e: Throwable) {
            _authorized.value = false
            _error.value = e.message ?: e.toString()
        } finally { _busy.value = false }
    }

    fun logout(onOk: () -> Unit) = viewModelScope.launch {
        withContext(Dispatchers.IO) { tokenStore.clear() }
        _authorized.value = false
        onOk()
    }
}