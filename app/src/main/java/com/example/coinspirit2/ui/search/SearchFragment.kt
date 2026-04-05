package com.example.coinspirit2.ui.search

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coinspirit2.R
import com.example.coinspirit2.databinding.FragmentSearchBinding
import com.example.coinspirit2.util.launchWhenStarted
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _b: FragmentSearchBinding? = null
    private val b get() = _b!!
    private val vm: SearchViewModel by viewModels()

    private lateinit var adapter: SearchAdapter
    private var searchJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentSearchBinding.bind(view)

        // Назад
        b.btnBack.setOnClickListener {
            val nav = findNavController()
            if (!nav.popBackStack()) nav.navigate(R.id.homeFragment)
        }

        // Поиск по нажатию на иконку
        b.btnSearch.setOnClickListener { triggerSearchNow() }

        adapter = SearchAdapter { item ->
            // ВСЕГДА идём на экран ввода сделки
            val args = Bundle().apply {
                putString("symbol", item.symbol)
                putInt("editTxId", -1)
            }
            findNavController().navigate(R.id.currencyDetailFragment, args)
        }

        b.rvResults.layoutManager = LinearLayoutManager(requireContext())
        b.rvResults.adapter = adapter

        // дебаунс ввода (300 мс) + режим exact для "BTC,ETH"
        b.etSearch.doAfterTextChanged { e ->
            val text = e?.toString().orEmpty()
            searchJob?.cancel()
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(300)
                vm.searchSmart(text)
            }
        }

        // подписки
        launchWhenStarted { vm.results.collect { adapter.submit(it) } }
        launchWhenStarted {
            vm.loading.collect { isLoading ->
                b.pb.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun triggerSearchNow() {
        val text = b.etSearch.text?.toString().orEmpty()
        searchJob?.cancel()
        viewLifecycleOwner.lifecycleScope.launch { vm.searchSmart(text) }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}