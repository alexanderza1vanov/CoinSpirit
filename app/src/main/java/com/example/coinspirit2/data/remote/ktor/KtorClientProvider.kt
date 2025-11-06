package com.example.coinspirit2.data.remote.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorClientProvider {

    // эмулятор: 10.0.2.2, устройство: подставь IP ПК
    private const val BASE = "http://10.0.2.2:8080"

    val client: HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            )
        }
        install(Logging) {
            level = LogLevel.BODY
        }
        defaultRequest {
            url(BASE)
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }
    }
}
