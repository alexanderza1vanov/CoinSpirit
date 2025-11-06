package com.example.coinspirit2

import android.app.Application
import com.example.coinspirit2.di.AppGraph

class App : Application() {
    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
    }
}
