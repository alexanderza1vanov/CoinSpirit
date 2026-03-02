package com.example.coinspirit2.data.repository

import com.example.coinspirit2.data.local.TokenStore
import com.example.coinspirit2.data.remote.ApiService
import com.example.coinspirit2.data.remote.AssetItem
import com.example.coinspirit2.data.remote.CreateTxRequest
import com.example.coinspirit2.data.remote.MarketQuote
import com.example.coinspirit2.data.remote.TokenPair
import com.example.coinspirit2.data.remote.TxDTO
import com.example.coinspirit2.data.remote.PositionDTO

class PortfolioRepository(
    private val api: ApiService,
    private val tokenStore: TokenStore
) {
    // --------- AUTH ----------
    suspend fun register(email: String, password: String, name: String?): TokenPair =
        api.register(email, password, name)

    suspend fun login(email: String, password: String): TokenPair =
        api.login(email, password)

    // --------- PORTFOLIO ----------
    suspend fun portfolio(): List<PositionDTO> = api.portfolio()

    // --------- MARKET (с кэшем) ----------
    private val priceCache = mutableMapOf<String, Pair<Long, String>>() // symbol -> (ts, price)
    private val PRICE_TTL = 15_000L

    suspend fun latest(symbols: List<String>): List<MarketQuote> {
        if (symbols.isEmpty()) return emptyList()
        val now = System.currentTimeMillis()

        val need = symbols.filterNot { s ->
            priceCache[s]?.let { (ts, _) -> now - ts < PRICE_TTL } == true
        }

        if (need.isNotEmpty()) {
            val fresh = api.marketLatest(need) // List<MarketQuote> (price: String)
            fresh.forEach { q -> priceCache[q.symbol] = now to q.price }
        }

        return symbols.mapNotNull { s ->
            priceCache[s]?.second?.let { p -> MarketQuote(s, p) }
        }
    }

    suspend fun search(q: String): List<AssetItem> = api.marketSearch(q)

    // --------- TRANSACTIONS ----------
    suspend fun transactionsBySymbol(symbol: String): List<TxDTO> =
        api.transactionsBySymbol(symbol)

    suspend fun deleteTransaction(id: Int) = api.deleteTransaction(id)

    suspend fun createTransaction(req: CreateTxRequest): Int = api.createTransaction(req)

    suspend fun getTransaction(id: Int): TxDTO? = api.transactionById(id)

    suspend fun updateTransaction(id: Int, req: CreateTxRequest) =
        api.updateTransaction(id, req)
}