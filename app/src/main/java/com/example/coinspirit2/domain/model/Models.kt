package com.example.coinspirit2.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.Instant
import kotlinx.serialization.SerialName

@Serializable data class AuthRequest(val email: String, val password: String)
@Serializable data class TokenPair(val accessToken: String, val refreshToken: String, val expiresIn: Long)

@Serializable
data class MeDTO(
    val id: Long,
    val email: String,
    @SerialName("name") val name: String? = null   // <- теперь необязательное
)

@Serializable data class AddPortfolioItemRequest(
    val symbol: String,
    val name: String? = null,
    @Contextual val quantity: BigDecimal,
    @Contextual val purchasePrice: BigDecimal,
    @Contextual val at: Instant? = null,
    val note: String? = null
)

@Serializable data class PositionDTO(
    val symbol: String,
    val name: String? = null,
    @Contextual val quantity: BigDecimal,
    @Contextual val avgPrice: BigDecimal,
    @Contextual val invested: BigDecimal,
    @Contextual val marketPrice: BigDecimal? = null,
    @Contextual val pnl: BigDecimal? = null
)

@Serializable enum class TxType { BUY, SELL }

@Serializable data class TxDTO(
    val id: Int,
    val symbol: String,
    val type: TxType,
    @Contextual val price: BigDecimal,
    @Contextual val quantity: BigDecimal,
    @Contextual val at: Instant,
    val note: String? = null
)

@Serializable data class CreateTxRequest(
    val symbol: String,
    val type: TxType,
    @Contextual val price: BigDecimal,
    @Contextual val quantity: BigDecimal,
    @Contextual val at: Instant? = null,
    val note: String? = null
)

@Serializable data class MarketQuote(val symbol: String, @Contextual val price: BigDecimal)
