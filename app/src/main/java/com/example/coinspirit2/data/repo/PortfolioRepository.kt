package com.example.coinspirit2.data.repo

import com.example.coinspirit2.data.remote.api.PortfolioApi
import com.example.coinspirit2.data.remote.dto.PortfolioItemDto
import com.example.coinspirit2.data.remote.dto.TxCreateRequest
import com.example.coinspirit2.data.remote.dto.TxDto

class PortfolioRepository(
    private val api: PortfolioApi
) {
    suspend fun portfolio(): List<PortfolioItemDto> =
        api.getPortfolio()

    suspend fun ensureAsset(symbol: String, name: String) =
        api.createAsset(symbol, name)

    suspend fun addBuy(
        assetId: Int,
        price: String,
        qty: String,
        atIsoUtc: String,
        note: String?
    ): TxDto = api.addTransaction(
        TxCreateRequest(
            assetId = assetId,
            type = "BUY",
            price = price,
            quantity = qty,
            at = atIsoUtc,
            note = note
        )
    )
}
