package com.example.coinspirit2.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coinspirit2.R
import com.example.coinspirit2.data.model.CurrencyRVModel
import com.example.coinspirit2.data.repository.CurrencyRepository
import com.example.coinspirit2.ui.adapters.CurrencyRVAdapter
import com.example.coinspirit2.ui.adapters.HistoryRVAdapter
import java.util.*

class SearchFragment : DialogFragment(), CurrencyRVAdapter.OnItemClickListener {

    private lateinit var searchEt: EditText
    private lateinit var btnClear: ImageButton
    private lateinit var currenciesRV: RecyclerView
    private lateinit var loadingPB: ProgressBar
    private lateinit var currencyRVModelArrayList: ArrayList<CurrencyRVModel>
    private lateinit var currencyRVAdapter: CurrencyRVAdapter
    private lateinit var currencyRepository: CurrencyRepository

    private val PREFS_NAME = "search_history_prefs"
    private val HISTORY_KEY = "search_history"
    private val MAX_HISTORY_SIZE = 10
    private lateinit var historyList: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchEt = view.findViewById(R.id.EdtSearch)
        btnClear = view.findViewById(R.id.btnClear)
        currenciesRV = view.findViewById(R.id.RVCurrencies)
        loadingPB = view.findViewById(R.id.PBLoading)

        currencyRVModelArrayList = ArrayList()
        currencyRVAdapter = CurrencyRVAdapter(currencyRVModelArrayList, requireContext(), this)
        currenciesRV.layoutManager = LinearLayoutManager(context)
        currenciesRV.adapter = currencyRVAdapter

        currencyRepository = CurrencyRepository(requireContext())
        getCurrencyData()

        historyList = loadHistory()
        setupHistoryRecyclerView()

        searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    btnClear.visibility = View.VISIBLE
                    filterCurrencies(s.toString())
                    showCurrencies()
                } else {
                    btnClear.visibility = View.GONE
                    showHistory()
                }
            }
        })

        btnClear.setOnClickListener {
            searchEt.setText("")
            hideKeyboard()
        }

        return view
    }

    private fun filterCurrencies(query: String) {
        val filteredList = currencyRVModelArrayList.filter {
            it.name.contains(query, ignoreCase = true) || it.symbol.contains(query, ignoreCase = true)
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(context, "Криптовалюта по вашему запросу не найдена", Toast.LENGTH_SHORT).show()
        }
        currencyRVAdapter.filterList(ArrayList(filteredList))
    }

    private fun getCurrencyData() {
        loadingPB.visibility = View.VISIBLE
        currencyRepository.getCurrencyData(
            onSuccess = { currencyList ->
                loadingPB.visibility = View.GONE
                currencyRVModelArrayList.clear()
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
        saveToHistory(currency.name)
        val dialogFragment = CurrencyDetailFragment.newInstance(currency.name, currency.symbol, currency.price)
        dialogFragment.show(parentFragmentManager, "currency_detail_dialog")
    }

    private fun saveToHistory(query: String) {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historySet = prefs.getStringSet(HISTORY_KEY, emptySet())?.toMutableSet() ?: mutableSetOf()

        historySet.remove(query)
        historySet.add(query)

        if (historySet.size > MAX_HISTORY_SIZE) {
            historySet.remove(historySet.first())
        }

        prefs.edit().putStringSet(HISTORY_KEY, historySet).apply()
        historyList = historySet.toMutableList()
        setupHistoryRecyclerView()
    }

    private fun loadHistory(): MutableList<String> {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(HISTORY_KEY, emptySet())?.toMutableList() ?: mutableListOf()
    }

    private fun setupHistoryRecyclerView() {
        if (historyList.isEmpty()) {
            currenciesRV.visibility = View.GONE
            return
        }

        val adapter = HistoryRVAdapter(historyList) { query ->
            searchEt.setText(query)
            showCurrencies()
        }
        currenciesRV.adapter = adapter
        currenciesRV.visibility = View.VISIBLE

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                removeFromHistory(position)
                adapter.notifyItemRemoved(position)
            }
        })
        itemTouchHelper.attachToRecyclerView(currenciesRV)
    }

    private fun removeFromHistory(position: Int) {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historySet = prefs.getStringSet(HISTORY_KEY, emptySet())?.toMutableSet() ?: mutableSetOf()

        val itemToRemove = historyList[position]
        historyList.removeAt(position)
        historySet.remove(itemToRemove)

        prefs.edit().putStringSet(HISTORY_KEY, historySet).apply()
    }

    private fun showCurrencies() {
        currenciesRV.adapter = currencyRVAdapter
        currenciesRV.visibility = View.VISIBLE
    }

    private fun showHistory() {
        if (historyList.isNotEmpty()) {
            setupHistoryRecyclerView()
        } else {
            currenciesRV.visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEt.windowToken, 0)
    }
}