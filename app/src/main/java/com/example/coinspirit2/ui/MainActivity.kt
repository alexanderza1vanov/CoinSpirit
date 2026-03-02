package com.example.coinspirit2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.coinspirit2.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

//app/
//└── src/main/
//├── java/com/example/coinspirit2/
//│   ├── App.kt
//│   ├── core/
//│   │   ├── ResultExt.kt
//│   │   ├── TimeExt.kt
//│   │   └── di/ServiceLocator.kt
//│   ├── data/
//│   │   ├── local/TokenStore.kt
//│   │   ├── remote/ApiService.kt
//│   │   └── repository/PortfolioRepository.kt
//│   ├── domain/
//│   │   ├── model/Models.kt
//│   │   └── usecase/UseCases.kt
//│   ├── ui/
//│   │   ├── MainActivity.kt
//│   │   ├── adapters/
//│   │   │   ├── PortfolioAdapter.kt
//│   │   │   └── HistoryAdapter.kt
//│   │   ├── auth/
//│   │   │   ├── LoginFragment.kt
//│   │   │   ├── RegisterFragment.kt
//│   │   │   └── AuthViewModel.kt
//│   │   ├── home/
//│   │   │   ├── HomeFragment.kt
//│   │   │   └── HomeViewModel.kt
//│   │   ├── search/
//│   │   │   ├── SearchFragment.kt
//│   │   │   └── SearchViewModel.kt
//│   │   ├── currency/
//│   │   │   ├── CurrencyDetailFragment.kt
//│   │   │   └── CurrencyDetailViewModel.kt
//│   │   └── settings/
//│   │       ├── SettingsFragment.kt
//│   │       └── SettingsViewModel.kt
//│   └── util/ViewBindingExt.kt
//└── res/layout/

//import android.content.Context
//import android.content.SharedPreferences
//import androidx.core.content.edit
//
//object ViewBindingExt {
//    private const val PREF_NAME = "auth_pref"
//    private const val TOKEN_KEY = "auth_token"
//
//    fun saveToken(context: Context, token: String) {
//        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        prefs.edit() { putString(TOKEN_KEY, token) }
//    }
//
//    fun getToken(context: Context): String? {
//        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        return prefs.getString(TOKEN_KEY, null)
//    }
//
//    fun clearToken(context: Context) {
//        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        prefs.edit() { remove(TOKEN_KEY) }
//    }
//}