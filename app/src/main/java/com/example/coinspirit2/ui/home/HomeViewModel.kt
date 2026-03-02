package com.example.coinspirit2.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinspirit2.core.di.ServiceLocator
import com.example.coinspirit2.data.remote.PositionDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repo = ServiceLocator.repo

    private val _items = MutableStateFlow<List<PositionDTO>>(emptyList())
    val items: StateFlow<List<PositionDTO>> = _items.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun load() = viewModelScope.launch {
        _loading.value = true
        _error.value = null
        try {
            _items.value = repo.portfolio()
        } catch (t: Throwable) {
            _items.value = emptyList()
            _error.value = t.message
        } finally {
            _loading.value = false
        }
    }
}
