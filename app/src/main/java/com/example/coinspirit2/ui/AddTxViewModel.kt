package com.example.coinspirit2.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinspirit2.data.remote.CreateTxRequest
import com.example.coinspirit2.data.remote.PortfolioRepository
import kotlinx.coroutines.launch

class AddTxViewModel(private val repo: PortfolioRepository): ViewModel() {
    val done = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()

    fun addBuy(assetId: Int, qty: String, price: String, note: String?) = viewModelScope.launch {
        val nowUtc = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
            .toInstant().toString() // 2025-11-06T14:14:30Z

        val req = CreateTxRequest(
            assetId = assetId,
            type = "BUY",
            price = price,
            quantity = qty,
            at = nowUtc,
            note = note
        )
        runCatching { repo.createTx(req) }
            .onSuccess { done.postValue(true) }
            .onFailure { error.postValue(it.message) }
    }
}
