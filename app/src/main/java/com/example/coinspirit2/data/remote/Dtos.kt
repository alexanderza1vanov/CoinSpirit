package com.example.coinspirit2.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual

// ---------- AUTH ----------
@Serializable
data class TokenPair(
    val accessToken: String,
    val refreshToken: String
    // если сервер возвращает expiresIn — добавишь сюда при необходимости:
    // val expiresIn: Long? = null
)

@Serializable
data class AuthRegisterRequest(val email: String, val password: String)

@Serializable
data class AuthLoginRequest(val email: String, val password: String)

@Serializable
data class MeDTO(val id: Int, val email: String, val name: String?)

// ---------- MARKET ----------
@Serializable
data class MarketQuote(
    val symbol: String,
    // в UI ты выводишь price как текст (без расчётов) — оставляем строкой
    val price: String
)

@Serializable
data class AssetItem(
    val id: Int,
    val symbol: String,
    val name: String
)

// ---------- PORTFOLIO ----------
@Serializable
data class PositionDTO(
    val symbol: String,
    val name: String? = null,
    // ниже — строки, потому что Home/Adapters используют String.bd()
    val quantity: String,
    val avgPrice: String,
    val invested: String,
    val marketPrice: String? = null,
    val pnl: String? = null
)

// ---------- TRANSACTIONS ----------
@Serializable
data class CreateTxRequest(
    val symbol: String,
    val type: String,             // "BUY" | "SELL"
    @Contextual val price: String,
    @Contextual val quantity: String,
    val at: String? = null,       // ISO-8601 строкой
    val note: String? = null
)

@Serializable
data class TxDTO(
    val id: Int,
    val symbol: String,
    val type: String,             // "BUY" | "SELL"
    // строки — они парсятся через .bd() в адаптере/VM
    val price: String,
    val quantity: String,
    val at: String,
    val note: String? = null
)
