package com.example.coinspirit2.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinspirit2.data.remote.dto.PortfolioItemDto
import com.example.coinspirit2.data.repo.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant

sealed interface PortfolioState {
    data object Loading: PortfolioState
    data class Ready(val items: List<PortfolioItemDto>): PortfolioState
    data class Error(val message: String): PortfolioState
}

class PortfolioViewModel(
    private val repo: PortfolioRepository
): ViewModel() {

    private val _state = MutableStateFlow<PortfolioState>(PortfolioState.Loading)
    val state: StateFlow<PortfolioState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = PortfolioState.Loading
            runCatching { repo.portfolio() }
                .onSuccess { _state.value = PortfolioState.Ready(it) }
                .onFailure { _state.value = PortfolioState.Error(it.message ?: "Ошибка") }
        }
    }

    fun createAssetAndBuy(symbol: String, name: String, price: String, qty: String, note: String?) {
        viewModelScope.launch {
            val at = Instant.now().toString() // ISO-8601 UTC
            runCatching {
                val asset = repo.ensureAsset(symbol, name)
                repo.addBuy(asset.id, price, qty, at, note)
            }.onSuccess {
                load()
            }.onFailure {
                _state.value = PortfolioState.Error(it.message ?: "Ошибка")
            }
        }
    }
}
