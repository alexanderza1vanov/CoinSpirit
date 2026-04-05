package com.example.coinspirit2.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coinspirit2.R
import com.example.coinspirit2.data.remote.PositionDTO
import com.example.coinspirit2.databinding.FragmentHomeBinding
import com.example.coinspirit2.ui.adapters.PortfolioAdapter
import com.example.coinspirit2.util.launchWhenStarted
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val vm: HomeViewModel by viewModels()
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private lateinit var adapter: PortfolioAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentHomeBinding.bind(view)

        adapter = PortfolioAdapter { pos ->
            val args = Bundle().apply { putString("symbol", pos.symbol) }
            findNavController().navigate(R.id.action_home_to_transactions, args)
        }

        b.mainRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        b.mainRecyclerview.adapter = adapter

        b.settingsImage.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }
        b.addImg.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_search)
        }

        launchWhenStarted {
            vm.items.collect { list ->
                adapter.submitList(list)
                updateSummary(list)
            }
        }
        vm.load()
    }



    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }

    private fun updateSummary(items: List<PositionDTO>) {
        val totalValue = items.fold(BigDecimal.ZERO) { acc, p ->
            acc + p.quantity.bd() * p.marketPrice.bd()
        }
        val totalInvested = items.fold(BigDecimal.ZERO) { acc, p -> acc + p.invested.bd() }
        val totalPnl = items.fold(BigDecimal.ZERO) { acc, p -> acc + p.pnl.bd() }

        val roiPercent = if (totalInvested.signum() != 0) {
            totalPnl.divide(totalInvested, 6, RoundingMode.HALF_UP) * BigDecimal("100")
        } else BigDecimal.ZERO

        b.valuePortfolioTv.text    = totalValue.prettyUSD()
        b.profitPortfolioTv.text   = totalPnl.prettySignedUSD()
        b.investedPortfolioTv.text = totalInvested.prettyUSD()
        b.roiPortfolioTv.text      = roiPercent.prettySignedPercent()

        val pnlColor = if (totalPnl.signum() >= 0) R.color.green_ok else R.color.red_bad
        val roiColor = if (roiPercent.signum() >= 0) R.color.green_ok else R.color.red_bad
        b.profitPortfolioTv.setTextColor(ContextCompat.getColor(requireContext(), pnlColor))
        b.roiPortfolioTv.setTextColor(ContextCompat.getColor(requireContext(), roiColor))
    }

    // helpers
    private fun String?.bd(): BigDecimal = try {
        if (this.isNullOrBlank()) BigDecimal.ZERO else BigDecimal(this)
    } catch (_: Throwable) { BigDecimal.ZERO }

    private fun BigDecimal.prettyUSD(): String {
        val nf = NumberFormat.getCurrencyInstance(Locale.US).apply {
            maximumFractionDigits = 2; minimumFractionDigits = 2
        }
        return nf.format(this.setScale(2, RoundingMode.HALF_UP))
    }
    private fun BigDecimal.prettySignedUSD(): String {
        val sign = if (this.signum() >= 0) "+" else ""
        val nf = NumberFormat.getCurrencyInstance(Locale.US).apply {
            maximumFractionDigits = 2; minimumFractionDigits = 2
        }
        return "$sign${nf.format(this.abs().setScale(2, RoundingMode.HALF_UP))}"
    }
    private fun BigDecimal.prettySignedPercent(): String {
        val sign = if (this.signum() >= 0) "+" else ""
        return "$sign${this.abs().setScale(2, RoundingMode.HALF_UP)}%"
    }
}
