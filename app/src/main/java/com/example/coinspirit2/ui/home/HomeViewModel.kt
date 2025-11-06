package com.example.coinspirit2.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinspirit2.data.model.CurrencyRVModel
import com.example.coinspirit2.data.model.PortfolioRVModel
import com.example.coinspirit2.data.remote.dto.UserResponse
import com.example.coinspirit2.data.repository.AuthRepository
import com.example.coinspirit2.data.repository.CurrencyRepository
import com.example.coinspirit2.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeState(
    val me: UserResponse? = null,
    val currencies: List<CurrencyRVModel> = emptyList(),
    val portfolio: List<PortfolioRVModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val marketRepo: CurrencyRepository = CurrencyRepository(),
    private val portfolioRepo: PortfolioRepository = PortfolioRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    fun loadMe(accessToken: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            authRepo.me(accessToken)
                .onSuccess { _state.value = _state.value.copy(me = it, isLoading = false) }
                .onFailure { _state.value = _state.value.copy(error = it.message, isLoading = false) }
        }
    }

    fun loadPortfolio(accessToken: String) {
        viewModelScope.launch {
            portfolioRepo.list(accessToken)
                .onSuccess { _state.value = _state.value.copy(portfolio = it) }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun addTransaction(
        accessToken: String,
        name: String,
        symbol: String,
        quantity: Double,
        purchasePrice: Double
    ) {
        viewModelScope.launch {
            portfolioRepo.add(accessToken, name, symbol, quantity, purchasePrice)
                .onSuccess {
                    _state.value = _state.value.copy(portfolio = _state.value.portfolio + it)
                }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun deleteTransaction(accessToken: String, id: Long) {
        viewModelScope.launch {
            portfolioRepo.delete(accessToken, id)
                .onSuccess {
                    _state.value =
                        _state.value.copy(portfolio = _state.value.portfolio.filterNot { it.id == id })
                }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun loadMarket() {
        viewModelScope.launch {
            marketRepo.getCurrencyData()
                .onSuccess { _state.value = _state.value.copy(currencies = it) }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }
}
