package com.example.coinspirit2.ui.activitys
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.coinspirit2.R
import com.example.coinspirit2.data.remote.ApiClient
import com.example.coinspirit2.data.remote.AuthRepository
import com.example.coinspirit2.data.remote.PortfolioApi
import com.example.coinspirit2.data.remote.PortfolioRepository
import com.example.coinspirit2.utils.TokenManager

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)

        val hasToken = !TokenManager.getToken(this).isNullOrEmpty()
        graph.setStartDestination(if (hasToken) R.id.homeFragment else R.id.loginFragment)
        navController.graph = graph
    }
    class AppDeps(ctx: Context) {
        private val baseUrl = "http://10.0.2.2:8080" // эмулятор; поменяй при нужде
        private val client = ApiClient(ctx, baseUrl)
        private val api = PortfolioApi(client)

        val authRepository = AuthRepository(api, ctx)
        val portfolioRepository = PortfolioRepository(api)
    }

}
