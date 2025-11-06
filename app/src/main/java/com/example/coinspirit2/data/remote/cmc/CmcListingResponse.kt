package com.example.coinspirit2.data.remote.cmc

import kotlinx.serialization.Serializable

@Serializable
data class CmcListingResponse(val data: List<CmcAsset>)

@Serializable
data class CmcAsset(
    val name: String,
    val symbol: String,
    val quote: Map<String, CmcQuote>
)

@Serializable
data class CmcQuote(val price: Double)