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
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coinspirit2.R
import com.example.coinspirit2.data.model.CurrencyRVModel
import com.example.coinspirit2.ui.adapters.CurrencyRVAdapter
import com.example.coinspirit2.ui.adapters.HistoryRVAdapter
import com.example.coinspirit2.ui.home.HomeViewModel
import kotlinx.coroutines.flow.collectLatest

class SearchFragment : DialogFragment(), CurrencyRVAdapter.OnItemClickListener {

    // Берём одну и ту же VM, что и у HomeFragment
    private val vm: HomeViewModel by activityViewModels()

    private lateinit var searchEt: EditText
    private lateinit var btnClear: ImageButton
    private lateinit var rv: RecyclerView
    private lateinit var loadingPB: ProgressBar

    private val allData = ArrayList<CurrencyRVModel>()           // полные данные из VM
    private val shownData = ArrayList<CurrencyRVModel>()         // то, что сейчас показывает адаптер
    private lateinit var currencyAdapter: CurrencyRVAdapter

    // История
    private val PREFS_NAME = "search_history_prefs"
    private val HISTORY_KEY = "search_history"
    private val MAX_HISTORY_SIZE = 10
    private var historyList: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEt   = view.findViewById(R.id.EdtSearch)
        btnClear   = view.findViewById(R.id.btnClear)
        rv         = view.findViewById(R.id.RVCurrencies)
        loadingPB  = view.findViewById(R.id.PBLoading)

        currencyAdapter = CurrencyRVAdapter(shownData, requireContext(), this)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = currencyAdapter

        historyList = loadHistory()
        if (historyList.isNotEmpty()) showHistory() else showCurrencies()

        // подписка на VM
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { s ->
                loadingPB.visibility = if (s.isLoading && allData.isEmpty()) View.VISIBLE else View.GONE
                s.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }

                // обновим данные для поиска
                allData.clear()
                allData.addAll(s.currencies)

                // если что-то введено – отфильтруем; иначе можно показать историю либо весь список
                val q = searchEt.text?.toString().orEmpty()
                if (q.isNotBlank()) {
                    applyFilter(q)
                } else if (historyList.isNotEmpty()) {
                    showHistory()
                } else {
                    showCurrencies()
                }
            }
        }

        searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString().orEmpty()
                btnClear.visibility = if (q.isBlank()) View.GONE else View.VISIBLE
                if (q.isBlank()) {
                    if (historyList.isNotEmpty()) showHistory() else showCurrencies()
                } else {
                    applyFilter(q)
                }
            }
        })

        btnClear.setOnClickListener {
            searchEt.setText("")
            hideKeyboard()
        }
    }

    // === фильтрация ===
    private fun applyFilter(query: String) {
        val filtered = allData.filter {
            it.name.contains(query, ignoreCase = true) || it.symbol.contains(query, ignoreCase = true)
        }
        if (filtered.isEmpty()) {
            Toast.makeText(requireContext(), "Ничего не найдено", Toast.LENGTH_SHORT).show()
        }
        shownData.clear()
        shownData.addAll(filtered)
        rv.adapter = currencyAdapter
        currencyAdapter.notifyDataSetChanged()
        rv.visibility = View.VISIBLE
    }

    private fun showCurrencies() {
        shownData.clear()
        shownData.addAll(allData)
        rv.adapter = currencyAdapter
        currencyAdapter.notifyDataSetChanged()
        rv.visibility = View.VISIBLE
    }

    // === история ===
    private fun showHistory() {
        val adapter = HistoryRVAdapter(historyList) { query ->
            searchEt.setText(query)
            searchEt.setSelection(query.length)
            applyFilter(query)
        }
        rv.adapter = adapter
        rv.visibility = View.VISIBLE

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                removeFromHistory(pos)
                adapter.notifyItemRemoved(pos)
                if (historyList.isEmpty()) showCurrencies()
            }
        })
        touchHelper.attachToRecyclerView(rv)
    }

    private fun saveToHistory(q: String) {
        if (q.isBlank()) return
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(HISTORY_KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
        set.remove(q)
        set.add(q)
        while (set.size > MAX_HISTORY_SIZE) {
            // удаляем самый старый (первый в итерации)
            set.remove(set.first())
        }
        prefs.edit().putStringSet(HISTORY_KEY, set).apply()
        historyList = set.toMutableList()
    }

    private fun removeFromHistory(position: Int) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(HISTORY_KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
        val item = historyList.getOrNull(position) ?: return
        historyList.removeAt(position)
        set.remove(item)
        prefs.edit().putStringSet(HISTORY_KEY, set).apply()
    }

    private fun loadHistory(): MutableList<String> {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(HISTORY_KEY, emptySet())?.toMutableList() ?: mutableListOf()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    // === клик по монете ===
    override fun onItemClick(currency: CurrencyRVModel) {
        saveToHistory(currency.name)
        val dialog = CurrencyDetailFragment.newInstance(
            currency.name,
            currency.symbol,
            currency.price
        )
        dialog.show(parentFragmentManager, "currency_detail_dialog")
    }
}
