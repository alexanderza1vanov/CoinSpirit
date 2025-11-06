package com.example.coinspirit2.data.remote.ktor

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object KtorClients {

    /** Бэкенд Ktor (PostgreSQL) */
    const val BASE = "http://10.0.2.2:8080"

    /** CoinMarketCap REST */
    private const val CMC_BASE = "https://pro-api.coinmarketcap.com"

    val backend: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; explicitNulls = false })
            }
            install(Logging) { level = LogLevel.BODY }
        }
    }

    val cmc: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; explicitNulls = false })
            }
            install(Logging) { level = LogLevel.BODY }
            // Базовый URL зададим в запросах абсолютным путём, чтобы гибко собирать эндпоинты
        }
    }

    fun backendUrl(path: String) = "$BASE/$path".replace("//", "/").replace(":/", "://")
    fun cmcUrl(path: String) = "$CMC_BASE/$path".replace("//", "/").replace(":/", "://")
}
