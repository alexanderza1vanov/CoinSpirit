package com.example.coinspirit2.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class TokenPair(val accessToken: String, val refreshToken: String)
