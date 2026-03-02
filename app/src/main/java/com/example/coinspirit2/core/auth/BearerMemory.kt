package com.example.coinspirit2.core.auth

import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Потокобезопасное in-memory хранилище токенов (без интерфейсов Ktor — совместимо с 2.x).
 */
object BearerMemory {
    private val lock = Mutex()
    private var current: BearerTokens? = null

    suspend fun load(): BearerTokens? = lock.withLock { current }

    suspend fun set(access: String, refresh: String) = lock.withLock {
        current = BearerTokens(access, refresh)
    }

    suspend fun clear() = lock.withLock { current = null }
}
