package com.example.coinspirit2.ui.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinspirit2.core.di.ServiceLocator
import com.example.coinspirit2.data.remote.CreateTxRequest
import com.example.coinspirit2.data.remote.TxDTO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class CurrencyDetailViewModel : ViewModel() {

    private val repo = ServiceLocator.repo

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _txToEdit = MutableStateFlow<TxDTO?>(null)
    val txToEdit: StateFlow<TxDTO?> = _txToEdit.asStateFlow()

    // текущий символ для экрана, отсюда строим авто-обновление цены
    private val symbol = MutableStateFlow<String?>(null)

    /** «сырая» цена токена (0 если нет данных/ошибка) */
    private val livePriceValue: StateFlow<BigDecimal> =
        symbol.filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { sym -> pricePolling(sym) } // отменится при смене/уходе
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = BigDecimal.ZERO
            )

    /** Готовая строка для UI (например, "$87,000.00") */
    val livePriceStr: StateFlow<String> =
        livePriceValue.map { px ->
            if (px.signum() == 0) "—"
            else NumberFormat.getCurrencyInstance(Locale.US).format(px)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "—"
        )

    /** Экран сообщает VM, какой символ сейчас редактируем/смотрим */
    fun setSymbol(sym: String) {
        symbol.value = sym
    }

    fun loadTransaction(id: Int) = viewModelScope.launch {
        _loading.value = true
        try {
            val tx = repo.getTransaction(id) // nullable или non-null — у тебя уже реализовано
            _txToEdit.value = tx
            // если вдруг символ ещё не известен (зашли через редактирование) — установим его
            if (tx != null && symbol.value.isNullOrBlank()) setSymbol(tx.symbol)
        } finally {
            _loading.value = false
        }
    }

    fun createTransaction(req: CreateTxRequest, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        _loading.value = true
        try {
            val id = repo.createTransaction(req)
            onDone(id > 0)
        } finally {
            _loading.value = false
        }
    }

    fun updateTransaction(id: Int, req: CreateTxRequest, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        _loading.value = true
        try {
            val ok = repo.updateTransaction(id, req)
            onDone(ok)
        } finally {
            _loading.value = false
        }
    }

    /** Поллинг цены: мгновенный запрос + повтор каждые 30 секунд. */
    private fun pricePolling(sym: String): Flow<BigDecimal> = flow {
        while (true) {
            val px = runCatching {
                repo.latest(listOf(sym)).firstOrNull()?.price?.toBigDecimal()
            }.getOrNull() ?: BigDecimal.ZERO
            emit(px)
            delay(30_000L)
        }
    }
}
