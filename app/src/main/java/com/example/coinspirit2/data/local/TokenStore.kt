package com.example.coinspirit2.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("auth_tokens")

object TokenStore {
    private val KEY_ACCESS = stringPreferencesKey("access")
    private val KEY_REFRESH = stringPreferencesKey("refresh")

    suspend fun save(ctx: Context, access: String, refresh: String) {
        ctx.dataStore.edit { it[KEY_ACCESS] = access; it[KEY_REFRESH] = refresh }
    }
    suspend fun access(ctx: Context): String? =
        ctx.dataStore.data.map { it[KEY_ACCESS] }.first()
    suspend fun refresh(ctx: Context): String? =
        ctx.dataStore.data.map { it[KEY_REFRESH] }.first()

    suspend fun clear(ctx: Context) {
        ctx.dataStore.edit { it.clear() }
    }
}
