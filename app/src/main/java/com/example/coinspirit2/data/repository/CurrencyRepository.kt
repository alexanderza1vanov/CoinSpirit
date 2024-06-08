package com.example.coinspirit2.data.repository

import android.content.Context
import android.view.View
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.coinspirit2.data.model.CurrencyRVModel
import com.example.coinspirit2.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CurrencyRepository(private val context: Context) {

    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)

    fun getCurrencyData(onSuccess: (ArrayList<CurrencyRVModel>) -> Unit, onError: (String) -> Unit) {
        val url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest"
        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                try {
                    val dataArray: JSONArray = response.getJSONArray("data")
                    val currencyList = ArrayList<CurrencyRVModel>()
                    for (i in 0 until dataArray.length()) {
                        val dataObj = dataArray.getJSONObject(i)
                        val name = dataObj.getString("name")
                        val symbol = dataObj.getString("symbol")
                        val quote = dataObj.getJSONObject("quote")
                        val USD = quote.getJSONObject("USD")
                        val price = USD.getDouble("price")
                        currencyList.add(CurrencyRVModel(name, symbol, price))
                    }
                    onSuccess(currencyList)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    onError("Fail to extract json data...")
                }
            },
            Response.ErrorListener { error ->
                onError("Fail to get the data...")
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["X-CMC_PRO_API_KEY"] = "9dda162c-a631-4d4e-8d1b-78b5f1c0b67e"
                return headers
            }
        }
        requestQueue.add(jsonObjectRequest)
    }
}
