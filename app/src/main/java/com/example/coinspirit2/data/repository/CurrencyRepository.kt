package com.example.coinspirit2.data.repository

import com.example.coinspirit2.data.model.CurrencyRVModel
import com.example.coinspirit2.data.remote.dto.MarketCoinDto
import com.example.coinspirit2.data.remote.ktor.KtorClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.get

class CurrencyRepository(
    private val client: io.ktor.client.HttpClient = KtorClientProvider.client
) {
    // Ktor‑сервер должен отдавать актуальные котировки с CMC: GET /market/latest
    suspend fun getCurrencyData(): Result<List<CurrencyRVModel>> = runCatching {
        val list: List<MarketCoinDto> = client.get("/market/latest").body()
        list.map { CurrencyRVModel(it.name, it.symbol, it.price) }
    }
}
