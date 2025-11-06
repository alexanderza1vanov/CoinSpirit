package com.example.coinspirit2.data.repository

import com.example.coinspirit2.data.model.PortfolioRVModel
import com.example.coinspirit2.data.remote.dto.AddPortfolioItemRequest
import com.example.coinspirit2.data.remote.dto.PortfolioItemDto
import com.example.coinspirit2.data.remote.ktor.KtorClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*

class PortfolioRepository(
    private val client: io.ktor.client.HttpClient = KtorClientProvider.client
) {
    suspend fun list(accessToken: String): Result<List<PortfolioRVModel>> = runCatching {
        val resp = client.get("/portfolio") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        if (!resp.status.isSuccess()) error(resp.bodyAsText())
        val items: List<PortfolioItemDto> = resp.body()
        items.map {
            PortfolioRVModel(
                id = it.id,
                name = it.name,
                symbol = it.symbol,
                price = (it.currentPrice ?: 0.0).toString(),
                quantity = it.quantity.toString(),
                purchasePrice = it.purchasePrice.toString()
            )
        }
    }

    suspend fun add(
        accessToken: String,
        name: String,
        symbol: String,
        quantity: Double,
        purchasePrice: Double
    ): Result<PortfolioRVModel> = runCatching {
        val resp = client.post("/portfolio") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(AddPortfolioItemRequest(name, symbol, quantity, purchasePrice))
        }
        if (!resp.status.isSuccess()) error(resp.bodyAsText())
        val dto: PortfolioItemDto = resp.body()
        PortfolioRVModel(
            id = dto.id,
            name = dto.name,
            symbol = dto.symbol,
            price = (dto.currentPrice ?: 0.0).toString(),
            quantity = dto.quantity.toString(),
            purchasePrice = dto.purchasePrice.toString()
        )
    }

    suspend fun delete(accessToken: String, id: Long): Result<Unit> = runCatching {
        val resp = client.delete("/portfolio/$id") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        if (!resp.status.isSuccess()) error(resp.bodyAsText()) else Unit
    }
}
