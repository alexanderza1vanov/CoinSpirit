package com.example.coinspirit2.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PortfolioItemDto(
    val symbol: String,
    val name: String,
    val quantity: String,
    val avgPrice: String,
    val invested: String
)

@Serializable
data class AssetCreateRequest(
    val symbol: String,
    val name: String
)

@Serializable
data class AssetDto(
    val id: Int,
    val symbol: String,
    val name: String
)

@Serializable
data class TxCreateRequest(
    val assetId: Int,
    val type: String,     // BUY | SELL
    val price: String,
    val quantity: String,
    val at: String,
    val note: String? = null
)

@Serializable
data class TxDto(
    val id: Int,
    val assetId: Int,
    val type: String,
    val price: String,
    val quantity: String,
    val at: String,
    val note: String? = null
)
