package com.example.coinspirit2.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MarketCoinDto(
    val name: String,
    val symbol: String,
    val price: Double
)
