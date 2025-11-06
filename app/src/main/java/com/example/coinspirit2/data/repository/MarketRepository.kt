package com.example.coinspirit2.data.repository

import com.example.coinspirit2.data.model.CurrencyRVModel
import com.example.coinspirit2.data.remote.cmc.CmcListingResponse
import com.example.coinspirit2.data.remote.ktor.KtorClients
import io.ktor.client.call.*
import io.ktor.client.request.*

class MarketRepository(
    private val apiKey: String // положи ключ в BuildConfig или local.properties
) {
    private val client = KtorClients.cmc

    suspend fun latest(): Result<ArrayList<CurrencyRVModel>> = runCatching {
        val url = KtorClients.cmcUrl("v1/cryptocurrency/listings/latest")
        val resp: CmcListingResponse = client.get(url) {
            header("X-CMC_PRO_API_KEY", apiKey)
        }.body()

        val list = ArrayList<CurrencyRVModel>()
        resp.data.forEach { a ->
            val price = a.quote["USD"]?.price ?: return@forEach
            list.add(CurrencyRVModel(a.name, a.symbol, price))
        }
        list
    }
}
