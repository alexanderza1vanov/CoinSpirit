package com.example.coinspirit2

import android.app.Application
import com.example.coinspirit2.core.di.ServiceLocator

class CoinSpiritApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
