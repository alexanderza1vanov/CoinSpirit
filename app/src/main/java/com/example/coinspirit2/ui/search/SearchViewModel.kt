package com.example.coinspirit2.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinspirit2.core.di.ServiceLocator
import com.example.coinspirit2.data.remote.AssetItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class SearchViewModel : ViewModel() {
    private val api = ServiceLocator.api

    private val _results = MutableStateFlow<List<QuoteUi>>(emptyList())
    val results = _results.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    /** Умный поиск: "btc, eth" -> exact; иначе – свободный */
    suspend fun searchSmart(text: String) {
        val q = text.trim()
        if (q.isEmpty()) {
            _results.value = emptyList()
            return
        }
        val tokens = q.split(',', ';', ' ')
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() }

        if (tokens.size >= 2) loadExact(tokens) else searchFree(q)
    }

    /** Свободный поиск по одному запросу */
    private suspend fun searchFree(query: String) {
        _loading.value = true
        try {
            val raw: List<AssetItem> = api.marketSearch(query)
            val list = raw.map { QuoteUi(it.symbol, it.name) }
                .distinctBy { it.symbol.uppercase() }            // УБИРАЕМ ДУБЛИ
            _results.value = list
        } finally {
            _loading.value = false
        }
    }

    /** Точный поиск по списку тикеров */
    fun loadExact(symbols: List<String>) = viewModelScope.launch {
        _loading.value = true
        try {
            // backend может возвращать только часть — нормализуем + убираем дубли
            val q = symbols.joinToString(",")
            val raw: List<AssetItem> = api.marketSearch(q)
            val list = raw.map { QuoteUi(it.symbol, it.name) }
                .distinctBy { it.symbol.uppercase() }
                .sortedBy { symbols.indexOf(it.symbol.uppercase()) }
            _results.value = list
        } finally {
            _loading.value = false
        }
    }
}