package com.example.coinspirit2.di

import android.content.Context
import com.example.coinspirit2.data.local.TokenStorage
import com.example.coinspirit2.data.remote.KtorClientProvider
import com.example.coinspirit2.data.remote.api.AuthApi
import com.example.coinspirit2.data.remote.api.PortfolioApi
import com.example.coinspirit2.data.repo.AuthRepository
import com.example.coinspirit2.data.repo.PortfolioRepository

class AppGraph(ctx: Context) {
    private val tokenStorage = TokenStorage(ctx)
    private val clientProvider = KtorClientProvider(tokenStorage)
    private val client = clientProvider.client

    // APIs
    private val authApi = AuthApi(client)
    private val portfolioApi = PortfolioApi(client)

    // Repos
    val authRepo = AuthRepository(authApi, tokenStorage)
    val portfolioRepo = PortfolioRepository(portfolioApi)
}
