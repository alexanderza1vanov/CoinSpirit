package com.example.coinspirit2.ui.search

import android.content.Context

class LocalHistoryStore(context: Context) : HistoryStore {
    private val prefs = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    private val KEY = "items"

    override fun load(): List<String> =
        prefs.getString(KEY, "")?.split('|')?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

    override fun push(symbol: String) {
        val s = symbol.trim().uppercase()
        if (s.isEmpty()) return
        val list = load().toMutableList()
        list.remove(s)
        list.add(0, s)
        prefs.edit().putString(KEY, list.take(20).joinToString("|")).apply()
    }

    override fun remove(symbol: String) {
        val s = symbol.trim().uppercase()
        val list = load().toMutableList()
        list.remove(s)
        prefs.edit().putString(KEY, list.joinToString("|")).apply()
    }
}