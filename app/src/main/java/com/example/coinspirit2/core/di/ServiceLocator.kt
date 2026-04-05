package com.example.coinspirit2.core.di

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.coinspirit2.data.local.TokenStore
import com.example.coinspirit2.data.remote.ApiService
import com.example.coinspirit2.data.repository.PortfolioRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.encodedPath
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.math.BigDecimal
import java.time.Instant

// --- (опционально) сериализаторы, если на бэке BigDecimal/Instant идут как строки/ISO ---
object BigDecimalAsStringSerializer : KSerializer<BigDecimal> {
    override val descriptor = PrimitiveSerialDescriptor("BigDecimalAsString", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: BigDecimal) =
        encoder.encodeString(value.toPlainString())
    override fun deserialize(decoder: Decoder): BigDecimal =
        decoder.decodeString().toBigDecimal()
}
object InstantAsIsoSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("InstantAsIso", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) =
        encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant =
        Instant.parse(decoder.decodeString())
}

object ServiceLocator {
    private const val TAG = "CoinSpirit.Net"
    private const val API_BASE = "http://10.0.2.2:8080"

    @SuppressLint("StaticFieldLeak")
    lateinit var tokenStore: TokenStore
        private set

    lateinit var client: HttpClient
        private set

    lateinit var api: ApiService
        private set

    lateinit var repo: PortfolioRepository
        private set

    private val serializers = SerializersModule {
        contextual(BigDecimalAsStringSerializer)
        contextual(InstantAsIsoSerializer)
    }

    fun init(app: Context) {
        tokenStore = TokenStore(app)
        buildClientAndApi()

        // «Посеять» токены из prefs в память до первого запроса (если нужно).
        // (Здесь ничего дополнительно делать не надо, т.к. loadTokens() читает из TokenStore.)
        Log.i(TAG, "ServiceLocator initialized. BASE=$API_BASE")
    }

    /** Полная сборка HttpClient + ApiService + Repo. */
    @Synchronized
    fun buildClientAndApi() {
        client = HttpClient(OkHttp) {
            expectSuccess = false

            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d(TAG, message)
                    }
                }
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 8_000
                socketTimeoutMillis = 15_000
            }

            install(ResponseObserver) {
                onResponse { resp ->
                    val url = runCatching { resp.call.request.url.toString() }.getOrNull() ?: "<unknown>"
                    if (!resp.status.isSuccess()) {
                        val body = runCatching { resp.bodyAsText() }.getOrNull()
                        if (body != null) Log.w(TAG, "HTTP ${resp.status.value} $url -> $body")
                        else Log.w(TAG, "HTTP ${resp.status.value} $url (no/failed body)")
                    } else {
                        Log.d(TAG, "HTTP ${resp.status.value} $url")
                    }
                }
            }

            // Общие заголовки
            install(DefaultRequest) {
                header(HttpHeaders.Accept, ContentType.Application.Json)
                header(HttpHeaders.AcceptCharset, "UTF-8")
            }

            // JSON
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                        serializersModule = serializers
                    }
                )
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val a = tokenStore.access()
                        val r = tokenStore.refresh()
                        if (!a.isNullOrBlank() && !r.isNullOrBlank()) BearerTokens(a, r) else null
                    }
                    refreshTokens {
                        val old = oldTokens ?: return@refreshTokens null
                        val refreshed = runCatching {
                            this@ServiceLocator.api.refresh(old.refreshToken.toString())
                        }.getOrNull() ?: return@refreshTokens null

                        tokenStore.save(refreshed.accessToken, refreshed.refreshToken)
                        BearerTokens(refreshed.accessToken, refreshed.refreshToken)
                    }
                    // На /auth* не шлём Authorization
                    sendWithoutRequest { req ->
                        val urlStr = req.url.toString()
                        val schemeIdx = urlStr.indexOf("://")
                        val hostStart = if (schemeIdx >= 0) schemeIdx + 3 else 0
                        val pathStart = urlStr.indexOf('/', hostStart)
                        val path = if (pathStart >= 0) urlStr.substring(pathStart) else "/"
                        !path.startsWith("/auth")
                    }
                }
            }
        }

        api = ApiService(client, API_BASE)
        repo = PortfolioRepository(api, tokenStore)
    }

    /** Сбросить кэш токена в плагине: пересоздаём клиент/Api/Repo после логина/логаута. */
    @Synchronized
    fun recreateClient() {
        runCatching { client.close() }
        buildClientAndApi()
    }
}
