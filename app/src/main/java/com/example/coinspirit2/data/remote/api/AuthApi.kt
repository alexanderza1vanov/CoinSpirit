package com.example.coinspirit2.data.remote.api

import com.example.coinspirit2.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*

class AuthApi(private val client: HttpClient) {
    suspend fun register(email: String, password: String): TokenPair =
        client.post("/auth/register") {
            setBody(RegisterRequest(email, password))
        }.body()

    suspend fun login(email: String, password: String): TokenPair =
        client.post("/auth/login") {
            setBody(LoginRequest(email, password))
        }.body()

    suspend fun refresh(refreshToken: String): TokenPair =
        client.post("/auth/refresh") {
            setBody(mapOf("refreshToken" to refreshToken))
        }.body()
}
