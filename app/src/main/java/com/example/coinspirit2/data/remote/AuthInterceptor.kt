package com.example.coinspirit2.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Не добавляет токен сам по себе. Мы передаём заголовок в вызове getMe().
 * Этот интерсептор пригодится, если захотите автоматом подставлять Bearer во все запросы.
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider()
        val req = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(req)
    }
}
