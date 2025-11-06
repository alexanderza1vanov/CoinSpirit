package com.example.coinspirit2.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinspirit2.data.remote.PortfolioRepository
import com.example.coinspirit2.data.remote.PositionDto
import kotlinx.coroutines.launch

class PortfolioViewModel(private val repo: PortfolioRepository): ViewModel() {
    val positions = MutableLiveData<List<PositionDto>>()
    val error = MutableLiveData<String?>()

    fun load() = viewModelScope.launch {
        runCatching { repo.positions() }
            .onSuccess { positions.postValue(it) }
            .onFailure { error.postValue(it.message) }
    }
}
