package com.example.coinspirit2.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinspirit2.data.remote.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repo: AuthRepository): ViewModel() {
    private val _state = MutableLiveData<Result<Unit>>()
    val state: LiveData<Result<Unit>> = _state

    fun login(email: String, pass: String) = viewModelScope.launch {
        runCatching { repo.login(email, pass) }
            .onSuccess { _state.postValue(Result.success(Unit)) }
            .onFailure { _state.postValue(Result.failure(it)) }
    }

    fun register(email: String, pass: String) = viewModelScope.launch {
        runCatching { repo.register(email, pass) }
            .onSuccess { _state.postValue(Result.success(Unit)) }
            .onFailure { _state.postValue(Result.failure(it)) }
    }
}
