package com.example.coinspirit2.ui.tx

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coinspirit2.R
import com.example.coinspirit2.core.di.ServiceLocator
import com.example.coinspirit2.data.remote.TxDTO
import com.example.coinspirit2.databinding.FragmentTransactionsBinding
import com.example.coinspirit2.util.launchWhenStarted
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class TransactionsFragment : Fragment(R.layout.fragment_transactions) {

    private var _b: FragmentTransactionsBinding? = null
    private val b get() = _b!!
    private val vm: TransactionsViewModel by viewModels()
    private lateinit var adapter: TxAdapter

    private var livePriceJob: Job? = null
    private var symbol: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentTransactionsBinding.bind(view)

        // back
        b.btnBack.setOnClickListener {
            val nav = findNavController()
            if (!nav.popBackStack()) nav.navigate(R.id.homeFragment)
        }

        // args
        symbol = requireArguments().getString("symbol").orEmpty()
        b.tvSymbolTitle.text = symbol

        // list
        adapter = TxAdapter(
            onEdit = { tx -> navigateToEdit(tx) },
            onDelete = { tx ->
                vm.delete(tx.id)
                vm.loadForSymbol(symbol)
            }
        )
        b.rvTx.layoutManager = LinearLayoutManager(requireContext())
        b.rvTx.adapter = adapter

        // binds
        launchWhenStarted { vm.tx.collect { list -> adapter.submitList(list) } }  // <-- ВАЖНО
        launchWhenStarted {
            vm.header.collect { hdr ->
                val nf = NumberFormat.getCurrencyInstance(Locale.US)
                b.tvTotalValue.text = nf.format(hdr.totalValue)
                val yieldPct = try {
                    "%.2f%%".format(Locale.US, hdr.yieldPct.toDouble())
                } catch (_: Throwable) { "0.00%" }
                b.tvInvestedAndYield.text =
                    getString(R.string.invested_and_yield_fmt, nf.format(hdr.invested), yieldPct)

                val color = if (hdr.totalValue >= hdr.invested) R.color.green_ok else R.color.red_bad
                b.tvTotalValue.setTextColor(ContextCompat.getColor(requireContext(), color))
            }
        }

        // initial load
        vm.loadForSymbol(symbol)

        // live price polling
        startLivePricePolling()
    }

    private fun startLivePricePolling() {
        val priceView = runCatching { b.tvLivePrice }.getOrNull() ?: return

        livePriceJob?.cancel()
        livePriceJob = viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    val text = try {
                        val quotes = ServiceLocator.repo.latest(listOf(symbol))
                        val raw = quotes.firstOrNull()?.price
                        val bd = raw?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        NumberFormat.getCurrencyInstance(Locale.US).format(bd)
                    } catch (_: Throwable) {
                        "--"
                    }
                    priceView.text = text
                    delay(30_000L)
                }
            }
        }
    }

    private fun navigateToEdit(tx: TxDTO) {
        val args = Bundle().apply {
            putString("symbol", tx.symbol)
            putInt("editTxId", tx.id)
        }
        findNavController().navigate(R.id.currencyDetailFragment, args)
    }
    override fun onDestroyView() {
        livePriceJob?.cancel()
        livePriceJob = null
        _b = null
        super.onDestroyView()
    }
}