package com.example.coinspirit2.data.model

data class PortfolioRVModel(
    val id: Long? = null,          // <— добавили для операций DELETE/UPDATE
    val name: String = "",
    val symbol: String = "",
    var price: String = "",
    val quantity: String = "",
    val purchasePrice: String = ""
)
