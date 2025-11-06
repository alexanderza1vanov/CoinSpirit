package com.example.coinspirit2.data.remote.model

data class RegisterRequest(
    val login: String,
    val password: String,
    val email: String
)
