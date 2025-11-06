package com.example.coinspirit2.data.remote

@kotlinx.serialization.Serializable
data class RegisterRequest(val email: String, val password: String)

@kotlinx.serialization.Serializable
data class LoginRequest(val email: String, val password: String)

@kotlinx.serialization.Serializable
data class TokenPair(val accessToken: String, val refreshToken: String)

@kotlinx.serialization.Serializable
data class AssetCreateRequest(val symbol: String, val name: String)

@kotlinx.serialization.Serializable
data class CreateTxRequest(
    val assetId: Int,
    val type: String,      // "BUY" / "SELL"
    val price: String,     // <-- строки!
    val quantity: String,  // <-- строки!
    val at: String,        // ISO-8601 UTC, напр. 2025-11-06T14:14:30Z
    val note: String? = null
)

@kotlinx.serialization.Serializable
data class PositionDto(
    val symbol: String,
    val name: String,
    val quantity: String,
    val avgPrice: String,
    val invested: String
)
