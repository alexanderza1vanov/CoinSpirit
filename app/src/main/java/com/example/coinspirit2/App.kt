package com.example.coinspirit2

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.coinspirit2.core.di.ServiceLocator

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // DI
        ServiceLocator.init(this)

        // Тема из настроек
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val dark = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}