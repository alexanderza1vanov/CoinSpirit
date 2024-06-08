package com.example.coinspirit2

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.coinspirit2.data.model.CurrencyRVModel
import com.example.coinspirit2.data.repository.CurrencyRepository
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurrencyRepositoryTest {

    private lateinit var repository: CurrencyRepository

    @Before
    fun setup() {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        repository = CurrencyRepository(context)
    }

    @Test
    fun testGetCurrencyData() {
        repository.getCurrencyData({ currencyList ->
            // Убедитесь, что список валют не пустой
            assert(currencyList.isNotEmpty())

            // Проверяем некоторые свойства первой валюты
            val currency = currencyList[0]
            assertEquals("Bitcoin", currency.name)
            assertEquals("BTC", currency.symbol)
        }, { error ->
            assert(false) { "Test failed with error: $error" }
        })
    }
}
