package com.example.coinspirit2.data.repo

import android.content.Context
import com.example.coinspirit2.data.remote.api.PortfolioApi
import com.example.coinspirit2.data.remote.dto.TokenPair

class AuthRepository(
    private val api: PortfolioApi,
    private val storage: Context
) {
    suspend fun register(email: String, password: String): TokenPair {
        val r = api.register(email, password)
        storage.saveTokens(r.accessToken, r.refreshToken)
        return r
    }

    suspend fun login(email: String, password: String): TokenPair {
        val r = api.login(email, password)
        storage.saveTokens(r.accessToken, r.refreshToken)
        return r
    }

    suspend fun logout() =
        storage.clear()
}
