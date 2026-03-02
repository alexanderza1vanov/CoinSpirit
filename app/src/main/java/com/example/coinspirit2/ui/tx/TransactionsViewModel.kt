package com.example.coinspirit2.ui.tx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinspirit2.core.di.ServiceLocator
import com.example.coinspirit2.data.remote.TxDTO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
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
            .flatMapLatest { sym -> pricePolling(sym) } // flow<BigDecimal>
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

    data class Header(
        val invested: BigDecimal,   // cost basis (себестоимость текущей позиции)
        val totalValue: BigDecimal  // market value (текущая рыночная стоимость)
    ) {
        val yieldPct: BigDecimal
            get() = if (invested.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO
            else (totalValue - invested) * BigDecimal(100) / invested
    }

    // заголовок портфеля = функция от (tx, livePrice)
    val header: StateFlow<Header> =
        combine(_tx, livePriceValue) { rows, px ->

            var costBasis = BigDecimal.ZERO      // то, что показываем как "Инвестировано"
            var qty = BigDecimal.ZERO            // текущее количество монет

            // ВАЖНО: правильный average-cost:
            // BUY -> costBasis += price * qtyBuy; qty += qtyBuy
            // SELL -> avgCost = costBasis/qty; costBasis -= avgCost*qtySell; qty -= qtySell
            rows.forEach { r ->
                val price = r.price.bd()
                val qAbs = r.quantity.bd().abs() // нормализуем на случай отрицательных qty

                if (price.signum() == 0 || qAbs.signum() == 0) return@forEach

                val isBuy = r.type.equals("BUY", true)

                if (isBuy) {
                    costBasis = costBasis + (price * qAbs)
                    qty = qty + qAbs
                } else {
                    // продажа
                    if (qty.signum() == 0) {
                        // нечего продавать по average cost (данные "в минус") — просто пропустим
                        return@forEach
                    }

                    // не даём продать больше, чем есть (защита от отрицательных остатков)
                    val sellQty = qAbs.min(qty)

                    val avgCost = if (qty.signum() == 0) BigDecimal.ZERO
                    else costBasis.divide(qty, 18, RoundingMode.HALF_UP)

                    costBasis = costBasis - (avgCost * sellQty)
                    qty = qty - sellQty
                }
            }

            val total = px * qty
            Header(invested = costBasis.max(BigDecimal.ZERO), totalValue = total.max(BigDecimal.ZERO))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Header(BigDecimal.ZERO, BigDecimal.ZERO)
        )
    /** Публичный метод: загрузить данные для символа */
    fun loadForSymbol(sym: String) = viewModelScope.launch {
        symbol.value = sym
        _tx.value = repo.transactionsBySymbol(sym)
    }

    /** Удаление транзакции с ре-лоадом списка для текущего символа. */
    fun delete(id: Int) = viewModelScope.launch {
        repo.deleteTransaction(id)
        symbol.value?.let { sym ->
            _tx.value = repo.transactionsBySymbol(sym)
        }
    }

    /** Поллинг цены: немедленный запрос + повтор каждые 30с. */
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

private fun BigDecimal.prettyCurrency(): String {
    val nf = NumberFormat.getCurrencyInstance(Locale.US)
    return nf.format(this)
}

private fun BigDecimal.abs(): BigDecimal = this.abs()
private fun BigDecimal.min(o: BigDecimal): BigDecimal = if (this <= o) this else o
private fun BigDecimal.max(o: BigDecimal): BigDecimal = if (this >= o) this else o

private operator fun BigDecimal.plus(o: BigDecimal) = this.add(o)
private operator fun BigDecimal.minus(o: BigDecimal) = this.subtract(o)
private operator fun BigDecimal.times(o: BigDecimal) = this.multiply(o)