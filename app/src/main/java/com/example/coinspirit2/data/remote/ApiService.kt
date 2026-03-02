package com.example.coinspirit2.data.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class ApiService(
    private val client: HttpClient,
    private val base: String
) {
    // ---------- AUTH ----------
    suspend fun register(email: String, password: String, name: String?): TokenPair {
        val req = AuthRegisterRequest(email, password)
        return client.post("$base/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()
    }

    suspend fun login(email: String, password: String): TokenPair {
        val req = AuthLoginRequest(email, password)
        return client.post("$base/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()
    }

    suspend fun refresh(refreshToken: String): TokenPair {
        return client.post("$base/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("refreshToken" to refreshToken))
        }.body()
    }

    suspend fun me(): MeDTO = client.get("$base/me").body()

    // ---------- PORTFOLIO ----------
    suspend fun portfolio(): List<PositionDTO> =
        client.get("$base/portfolio").body()

    suspend fun addToPortfolio(
        symbol: String,
        purchasePrice: String,
        quantity: String,
        atIso: String? = null,
        note: String? = null
    ): Int {
        val payload = mapOf(
            "symbol" to symbol,
            "purchasePrice" to purchasePrice,
            "quantity" to quantity,
            "at" to atIso,
            "note" to note
        )
        val resp = client.post("$base/portfolio") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.body<Map<String, Int>>()
        return resp["transactionId"] ?: -1
    }

    // ---------- TRANSACTIONS ----------
    suspend fun transactions(): List<TxDTO> =
        client.get("$base/transactions").body()

    suspend fun transactionsBySymbol(symbol: String): List<TxDTO> =
        client.get("$base/transactions/by-symbol") {
            url { parameters.append("symbol", symbol) }
        }.body()

    suspend fun createTransaction(req: CreateTxRequest): Int {
        val resp = client.post("$base/transactions") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body<Map<String, Int>>()
        return resp["id"] ?: -1
    }

    suspend fun deleteTransaction(id: Int) {
        client.delete("$base/transactions/$id")
    }

    /** Вернёт null при 404/не-2xx вместо падения. */
    suspend fun transactionById(id: Int): TxDTO? {
        val url = "$base/transactions/$id"
        val resp: HttpResponse = client.get(url)
        return when {
            resp.status.value == 404 -> null
            resp.status.isSuccess()  -> resp.body()
            else -> {
                runCatching { Log.w("CoinSpirit.Net", "HTTP ${resp.status.value} $url -> ${resp.bodyAsText()}") }
                null
            }
        }
    }

    /** Возвращает true, если обновление прошло успешно (2xx). */
    suspend fun updateTransaction(id: Int, req: CreateTxRequest): Boolean {
        val url = "$base/transactions/$id"
        val resp: HttpResponse = client.put(url) {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        if (!resp.status.isSuccess()) {
            runCatching { Log.w("CoinSpirit.Net", "HTTP ${resp.status.value} $url -> ${resp.bodyAsText()}") }
        }
        return resp.status.isSuccess()
    }

    // ---------- MARKET ----------
    suspend fun marketLatest(symbols: List<String>): List<MarketQuote> =
        client.get("$base/market/latest") {
            url {
                if (symbols.isNotEmpty()) {
                    parameters.append("symbols", symbols.joinToString(","))
                }
            }
        }.body()

    suspend fun marketSearch(query: String): List<AssetItem> =
        client.get("$base/market/search") {
            url { parameters.append("q", query) }
        }.body()
}
