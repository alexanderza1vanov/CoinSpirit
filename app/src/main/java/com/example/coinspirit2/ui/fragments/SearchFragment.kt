package com.example.coinspirit2.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coinspirit2.ui.adapters.CurrencyRVAdapter
import com.example.coinspirit2.data.model.CurrencyRVModel
import com.example.coinspirit2.R
import com.example.coinspirit2.data.repository.CurrencyRepository
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment : DialogFragment(), CurrencyRVAdapter.OnItemClickListener {

    private lateinit var searchEt: EditText
    private lateinit var currenciesRV: RecyclerView
    private lateinit var loadingPB: ProgressBar
    private lateinit var currencyRVModelArrayList: ArrayList<CurrencyRVModel>
    private lateinit var currencyRVAdapter: CurrencyRVAdapter
    private lateinit var currencyRepository: CurrencyRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchEt = view.findViewById(R.id.EdtSearch)
        currenciesRV = view.findViewById(R.id.RVCurrencies)
        loadingPB = view.findViewById(R.id.PBLoading)
        currencyRVModelArrayList = ArrayList()
        currencyRVAdapter = context?.let { CurrencyRVAdapter(currencyRVModelArrayList, it, this) }!!
        currenciesRV.layoutManager = LinearLayoutManager(context)
        currenciesRV.adapter = currencyRVAdapter
        currencyRepository = CurrencyRepository(requireContext())

        getCurrencyData()

        searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterCurrencies(s.toString())
            }
        })

        return view
    }

    private fun filterCurrencies(currency: String) {
        val filteredList = ArrayList<CurrencyRVModel>()
        for (item in currencyRVModelArrayList) {
            if (item.name.toLowerCase(Locale.ROOT).contains(currency.toLowerCase(Locale.ROOT))) {
                filteredList.add(item)
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(context, "Криптовалюта по вашему запросу не найдена", Toast.LENGTH_SHORT).show()
        } else {
            currencyRVAdapter.filterList(filteredList)
        }
    }

    private fun getCurrencyData() {
        loadingPB.visibility = View.VISIBLE
        currencyRepository.getCurrencyData(
            onSuccess = { currencyList ->
                loadingPB.visibility = View.GONE
                currencyRVModelArrayList.addAll(currencyList)
                currencyRVAdapter.notifyDataSetChanged()
            },
            onError = { errorMessage ->
                loadingPB.visibility = View.GONE
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onItemClick(currency: CurrencyRVModel) {
        val dialogFragment = CurrencyDetailFragment.newInstance(currency.name, currency.symbol, currency.price)
        dialogFragment.show(parentFragmentManager, "currency_detail_dialog")
    }

    private fun showCurrencyDetailFragment(name: String, symbol: String, price: Double) {
        val fragment = CurrencyDetailFragment.newInstance(name, symbol, price)
        fragment.show(parentFragmentManager, "currency_detail")
    }
}
