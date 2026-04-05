package com.example.coinspirit2.core.auth

import com.example.coinspirit2.data.local.TokenStore
import com.example.coinspirit2.data.remote.ApiService
import kotlin.math.max

/**
 * Храним "в памяти" и в SharedPreferences пару токенов.
 * Здесь же делаем refresh при необходимости.
 */
data class TokenBundle(
    val access: String,
    val refresh: String,
    val accessExpMillis: Long // ожидаемая дата истечения access (ms)
)

class AuthRepository(
    private val api: ApiService,
    private val store: TokenStore
) {
    @Volatile private var inMemory: TokenBundle? = null

    /** Сохранить токены после логина/регистрации. exp ~ 1 час по умолчанию, если сервер не отдаёт TTL. */
    fun setFromLogin(access: String, refresh: String, expiresInSec: Long = 3600L) {
        val exp = System.currentTimeMillis() + max(1, expiresInSec) * 1000
        val b = TokenBundle(access, refresh, exp)
        inMemory = b
        store.save(access, refresh)
    }

    /** Текущие токены (если есть на диске), плюс поддерживаем exp в памяти. */
    fun current(): TokenBundle? {
        val a = store.access()
        val r = store.refresh()
        return if (!a.isNullOrBlank() && !r.isNullOrBlank()) {
            val expGuess = inMemory?.accessExpMillis ?: (System.currentTimeMillis() + 3600_000)
            TokenBundle(a, r, expGuess).also { inMemory = it }
        } else null
    }

    /** Полный выход из аккаунта. */
    fun logout() {
        inMemory = null
        store.clear()
    }

    /** Вернёт валидный access; при просрочке попробует рефрешнуть. */
    suspend fun ensureFreshAccess(): String? {
        val cur = current() ?: return null
        val now = System.currentTimeMillis()
        return if (now < cur.accessExpMillis - 30_000) cur.access
        else refresh(cur.refresh)?.access
    }

    /** Обновление токенов через API /auth/refresh. */
    suspend fun refresh(refresh: String): TokenBundle? {
        val r = runCatching { api.refresh(refresh) }.getOrNull() ?: return null
        val exp = System.currentTimeMillis() + 3600_000 // TTL неизвестен — принимаем 1 час
        val b = TokenBundle(r.accessToken, r.refreshToken, exp)
        inMemory = b
        store.save(r.accessToken, r.refreshToken)
        return b
    }
}
