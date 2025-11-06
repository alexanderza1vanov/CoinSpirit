package com.example.coinspirit2.data.remote
import android.content.Context
import androidx.browser.trusted.TokenStore
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class ApiClient(
    ctx: Context,
    private val baseUrl: String
) {
    // Bearer + refresh
    private val bearer = BearerTokensProvider(ctx, baseUrl)

    val http = HttpClient(OkHttp) {
        expectSuccess = false
        install(Logging) { level = LogLevel.INFO }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; explicitNulls = false })
        }
        install(Auth) {
            bearer {
                loadTokens { bearer.load() }                // достаём access/refresh из DataStore
                refreshTokens { old -> bearer.refresh(old) } // дергаем /auth/refresh при 401
            }
        }
        defaultRequest {
            url { protocol = URLProtocol.HTTP } // если https — поменяй
        }
    }

    suspend fun <T> get(path: String): T =
        http.get("$baseUrl$path").body()

    suspend fun <Req, Res> post(path: String, body: Req): Res =
        http.post("$baseUrl$path") { contentType(ContentType.Application.Json); setBody(body) }.body()
}

private class BearerTokensProvider(
    private val ctx: Context,
    private val baseUrl: String
) {
    suspend fun load(): BearerTokens? {
        val access = TokenStore.access(ctx)
        val refresh = TokenStore.refresh(ctx)
        return if (access != null && refresh != null) BearerTokens(access, refresh) else null
    }

    suspend fun refresh(old: BearerTokens): BearerTokens {
        // твой /auth/refresh принимает refreshToken в body (мы так делали в PowerShell)
        // если у тебя на сервере ожидается Authorization: Bearer <refresh>, поменяй запрос.
        val client = HttpClient(OkHttp) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val resp: TokenPair = client.post("$baseUrl/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("refreshToken" to old.refreshToken))
        }.body()

        TokenStore.save(ctx, resp.accessToken, resp.refreshToken)
        client.close()
        return BearerTokens(resp.accessToken, resp.refreshToken)
    }
}
