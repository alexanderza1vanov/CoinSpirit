package com.example.coinspirit2.core

sealed class AppResult<out T> {
    data class Ok<T>(val value: T): AppResult<T>()
    data class Err(val message: String, val cause: Throwable? = null): AppResult<Nothing>()
}

inline fun <T> runRes(block: () -> T): AppResult<T> = try {
    AppResult.Ok(block())
} catch (t: Throwable) {
    AppResult.Err(t.message ?: t::class.java.simpleName, t)
}
