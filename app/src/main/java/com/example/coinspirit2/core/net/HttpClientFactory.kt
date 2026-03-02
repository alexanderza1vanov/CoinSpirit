package com.example.coinspirit2.core.net

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    fun create(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    android.util.Log.d("CoinSpirit.Net", message)
                }
            }
        }
        install(ResponseObserver) {
            onResponse { resp ->
                val url = runCatching { resp.call.request.url.toString() }.getOrNull() ?: "<unknown>"
                if (!resp.status.isSuccess()) {
                    runCatching { resp.bodyAsText() }
                        .onSuccess { body ->
                            android.util.Log.w("CoinSpirit.Net", "HTTP ${resp.status.value} $url -> $body")
                        }
                        .onFailure { e ->
                            android.util.Log.w("CoinSpirit.Net", "HTTP ${resp.status.value} $url (no/failed body)", e)
                        }
                } else {
                    android.util.Log.d("CoinSpirit.Net", "HTTP ${resp.status.value} $url")
                }
            }
        }
    }
}