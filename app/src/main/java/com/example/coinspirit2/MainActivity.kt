package com.example.coinspirit2
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.coinspirit2.data.remote.api.PortfolioApi
import com.example.coinspirit2.data.repo.AuthRepository
import com.example.coinspirit2.data.repo.PortfolioRepository
import com.example.coinspirit2.utils.TokenManager


import com.example.coinspirit2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val graph by lazy { (application as App).graph }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
