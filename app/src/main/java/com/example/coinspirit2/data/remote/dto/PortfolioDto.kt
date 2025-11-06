package com.example.coinspirit2.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PortfolioItemDto(
    val id: Long,
    val name: String,
    val symbol: String,
    val quantity: Double,
    val purchasePrice: Double,
    val currentPrice: Double? = null
)

@Serializable
data class AddPortfolioItemRequest(
    val name: String,
    val symbol: String,
    val quantity: Double,
    val purchasePrice: Double
)
