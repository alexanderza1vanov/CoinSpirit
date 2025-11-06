package com.example.coinspirit2.data.repository

import com.example.coinspirit2.data.remote.dto.*
import com.example.coinspirit2.data.remote.ktor.KtorClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*

class AuthRepository(
    private val client: io.ktor.client.HttpClient = KtorClientProvider.client
) {

    suspend fun register(email: String, password: String): Result<TokenResponse> = runCatching {
        val resp = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, password))
        }
        if (resp.status.isSuccess()) resp.body() else error(resp.bodyAsText())
    }

    suspend fun login(email: String, password: String): Result<TokenResponse> = runCatching {
        val resp = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }
        if (resp.status.isSuccess()) resp.body() else error(resp.bodyAsText())
    }

    suspend fun me(accessToken: String): Result<UserResponse> = runCatching {
        val resp = client.get("/me") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        if (resp.status.isSuccess()) resp.body() else error(resp.bodyAsText())
    }
}
