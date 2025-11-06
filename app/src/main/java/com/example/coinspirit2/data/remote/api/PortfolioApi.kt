package com.example.coinspirit2.data.remote.api

import com.example.coinspirit2.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*

class PortfolioApi(private val client: HttpClient) {

    suspend fun getPortfolio(): List<PortfolioItemDto> =
        client.get("/portfolio").body()

    suspend fun createAsset(symbol: String, name: String): AssetDto =
        client.post("/assets") {
            setBody(AssetCreateRequest(symbol, name))
        }.body()

    suspend fun getAsset(id: Int): AssetDto =
        client.get("/assets/$id").body()

    suspend fun addTransaction(req: TxCreateRequest): TxDto =
        client.post("/transactions") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

    suspend fun deleteTransaction(id: Int) {
        client.delete("/transactions/$id")
    }
}
