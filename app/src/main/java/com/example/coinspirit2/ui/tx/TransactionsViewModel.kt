package com.example.coinspirit2.ui.tx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinspirit2.core.di.ServiceLocator
import com.example.coinspirit2.data.remote.TxDTO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class TransactionsViewModel : ViewModel() {
    private val repo = ServiceLocator.repo

    // текущий выбранный тикер
    private val symbol = MutableStateFlow<String?>(null)

    // список транзакций для текущего символа
    private val _tx = MutableStateFlow<List<TxDTO>>(emptyList())
    val tx: StateFlow<List<TxDTO>> = _tx.asStateFlow()

    // «сырая» текущая цена как BigDecimal (0 при ошибке)
    private val livePriceValue: StateFlow<BigDecimal> =
        symbol
            .filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { sym -> pricePolling(sym) }          // flow<BigDecimal>
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = BigDecimal.ZERO
            )

    // красивая строка для UI: "Текущая цена: $..."
    val livePriceStr: StateFlow<String> =
        livePriceValue
            .map { price ->
                if (price.signum() == 0) "Текущая цена: —"
                else "Текущая цена: ${price.prettyCurrency()}"
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = "Текущая цена: —"
            )

    data class Header(val invested: BigDecimal, val totalValue: BigDecimal) {
        val yieldPct: BigDecimal
            get() = if (invested.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO
            else (totalValue - invested) * BigDecimal(100) / invested
    }

    // заголовок портфеля = функция от (tx, livePrice)
    val header: StateFlow<Header> =
        combine(_tx, livePriceValue) { rows, px ->
            // посчитаем вложения и количество
            var invested = BigDecimal.ZERO
            var qty = BigDecimal.ZERO

            rows.forEach { r ->
                val price = r.price.bd()
                val q = r.quantity.bd()
                if (r.type.equals("BUY", true)) {
                    invested += (price * q)
                    qty += q
                } else {
                    invested -= (price * q)
                    qty -= q
                }
            }

            val total = px * qty
            Header(invested = invested, totalValue = total)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Header(BigDecimal.ZERO, BigDecimal.ZERO)
        )

    /** Публичный метод: загрузить данные для символа и запустить «умный» авто-обновлятор. */
    fun loadForSymbol(sym: String) = viewModelScope.launch {
        symbol.value = sym
        _tx.value = repo.transactionsBySymbol(sym)
        // Инициируем начальную цену сразу (не ждём первой итерации 30с)
        // — pricePolling тоже делает мгновенный запрос, так что можно не дёргать тут latest.
    }

    /** Удаление транзакции с ре-лоадом списка для текущего символа. */
    fun delete(id: Int) = viewModelScope.launch {
        repo.deleteTransaction(id)
        symbol.value?.let { sym ->
            _tx.value = repo.transactionsBySymbol(sym)
        }
    }

    /** Поллинг цены: немедленный запрос + повтор каждые 30с. Отменяется flatMapLatest’ом. */
    private fun pricePolling(sym: String): Flow<BigDecimal> = flow {
        while (true) {
            val px = runCatching {
                repo.latest(listOf(sym))
                    .firstOrNull()
                    ?.price
                    ?.bd()
                    ?: BigDecimal.ZERO
            }.getOrElse { BigDecimal.ZERO }

            emit(px)
            delay(30_000L)
        }
    }
}

/* ---------- helpers ---------- */
private fun String?.bd(): BigDecimal = try {
    if (this.isNullOrBlank()) BigDecimal.ZERO else BigDecimal(this)
} catch (_: Throwable) { BigDecimal.ZERO }

private operator fun BigDecimal.plus(o: BigDecimal) = this.add(o)
private operator fun BigDecimal.minus(o: BigDecimal) = this.subtract(o)
private operator fun BigDecimal.times(o: BigDecimal) = this.multiply(o)

private fun BigDecimal.prettyCurrency(): String {
    val nf = NumberFormat.getCurrencyInstance(Locale.US)
    return nf.format(this)
}
