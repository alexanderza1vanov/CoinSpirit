package com.example.coinspirit2.data.remote

import android.util.Log
import com.example.coinspirit2.BuildConfig
import com.example.coinspirit2.data.local.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class KtorClientProvider(
    private val tokenStorage: TokenStorage
) {
    val client: HttpClient by lazy {
        HttpClient(OkHttp) {
            expectSuccess = false

            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.HEADERS
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                )
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val access = tokenStorage.readAccess()
                        val refresh = tokenStorage.readRefresh()
                        if (access != null && refresh != null)
                            BearerTokens(access, refresh)
                        else null
                    }
                    sendWithoutRequest { req ->
                        // не слать авторизацию на /auth/*
                        !req.url.encodedPath.startsWith("/auth")
                    }
                    refreshTokens {
                        val refresh = tokenStorage.readRefresh() ?: return@refreshTokens null
                        try {
                            val newTokens = client.post("${BuildConfig.API_BASE}/auth/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody(mapOf("refreshToken" to refresh))
                            }.body<AuthTokens>()
                            tokenStorage.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                            BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                        } catch (t: Throwable) {
                            Log.e("KtorAuth", "refresh failed", t)
                            tokenStorage.clear()
                            null
                        }
                    }
                }
            }

            defaultRequest {
                url { takeFrom(BuildConfig.API_BASE) }
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
        }
    }

    @Serializable
    data class AuthTokens(
        val accessToken: String,
        val refreshToken: String
    )
}
