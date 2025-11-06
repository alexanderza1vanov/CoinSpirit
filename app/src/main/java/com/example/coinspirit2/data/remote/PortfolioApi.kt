package com.example.coinspirit2.data.remote

import com.example.coinspirit2.data.local.TokenStore

class PortfolioApi(private val c: ApiClient) {
    suspend fun register(email: String, pass: String): TokenPair =
        c.post("/auth/register", RegisterRequest(email, pass))

    suspend fun login(email: String, pass: String): TokenPair =
        c.post("/auth/login", LoginRequest(email, pass))

    suspend fun positions(): List<PositionDto> =
        c.get("/portfolio/positions")

    suspend fun createAsset(symbol: String, name: String) =
        c.post("/assets", AssetCreateRequest(symbol, name))

    suspend fun createTx(req: CreateTxRequest) =
        c.post("/transactions", req)
}

class AuthRepository(private val api: PortfolioApi, private val ctx: android.content.Context) {
    suspend fun register(email: String, pass: String) {
        val t = api.register(email, pass)
        TokenStore.save(ctx, t.accessToken, t.refreshToken)
    }
    suspend fun login(email: String, pass: String) {
        val t = api.login(email, pass)
        TokenStore.save(ctx, t.accessToken, t.refreshToken)
    }
    suspend fun logout() = TokenStore.clear(ctx)
}

class PortfolioRepository(private val api: PortfolioApi) {
    suspend fun positions() = api.positions()
    suspend fun createAsset(symbol: String, name: String) = api.createAsset(symbol, name)
    suspend fun createTx(req: CreateTxRequest) = api.createTx(req)
}
