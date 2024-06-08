package com.example.coinspirit2.ui.interfaces

interface DataTransfer {
    fun onAddTransaction(name: String, symbol: String, price: String, quantity: String, purchasePrice: String)
}
