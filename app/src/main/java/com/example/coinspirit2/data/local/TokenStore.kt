package com.example.coinspirit2.data.local

import android.content.Context
import android.content.SharedPreferences

class TokenStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun access(): String? = prefs.getString(KEY_ACCESS, null)
    fun refresh(): String? = prefs.getString(KEY_REFRESH, null)

    @Synchronized
    fun save(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .apply()
    }

    @Synchronized
    fun updateAccess(accessToken: String) {
        prefs.edit().putString(KEY_ACCESS, accessToken).apply()
    }

    @Synchronized
    fun clear() {
        prefs.edit()
            .remove(KEY_ACCESS)
            .remove(KEY_REFRESH)
            .apply()
    }

    fun hasTokens(): Boolean = !access().isNullOrBlank() && !refresh().isNullOrBlank()

    private companion object {
        const val PREFS_NAME = "auth_tokens"
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
    }
}
