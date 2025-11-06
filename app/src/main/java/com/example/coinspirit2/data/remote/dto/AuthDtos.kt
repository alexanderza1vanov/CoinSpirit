package com.example.coinspirit2.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class TokenResponse(val accessToken: String, val refreshToken: String)

@Serializable
data class UserResponse(val id: Long, val email: String, val createdAt: String)


@Serializable
data class SettingsDTO(val theme: String,val currency: String)
// "LIGHT" | "DARK" | "SYSTEM"  "USD" и т.п.

