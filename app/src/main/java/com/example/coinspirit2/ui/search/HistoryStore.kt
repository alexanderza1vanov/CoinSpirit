package com.example.coinspirit2.ui.search

/** Простое хранилище истории поисковых запросов. */
interface HistoryStore {
    fun load(): List<String>
    fun push(symbol: String)
    fun remove(symbol: String)
}