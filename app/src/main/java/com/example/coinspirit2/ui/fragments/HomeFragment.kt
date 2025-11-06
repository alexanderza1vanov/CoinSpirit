package com.example.coinspirit2.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coinspirit2.R
import com.example.coinspirit2.data.model.PortfolioRVModel
import com.example.coinspirit2.ui.adapters.PortfolioRVAdapter
import com.example.coinspirit2.ui.home.HomeViewModel
import com.example.coinspirit2.ui.interfaces.DataTransfer
import com.example.coinspirit2.utils.TokenManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class HomeFragment : Fragment(R.layout.fragment_home), DataTransfer {

    private val vm: HomeViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var portfolioRVAdapter: PortfolioRVAdapter
    private lateinit var portfolioList: MutableList<PortfolioRVModel>
    private lateinit var totalValueTextView: TextView
    private lateinit var profitTextView: TextView
    private val df2 = DecimalFormat("#.##")

    private var updateJob: Job? = null
    private val updateInterval = 5_000L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.main_recyclerview)
        totalValueTextView = view.findViewById(R.id.value_portfolio_tv)
        profitTextView = view.findViewById(R.id.profit_portfolio_tv)

        portfolioList = ArrayList()
        portfolioRVAdapter = PortfolioRVAdapter(requireContext(), portfolioList)
        recyclerView.apply {
            adapter = portfolioRVAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        view.findViewById<ImageView>(R.id.add_img).setOnClickListener { showSearchDialog() }
        view.findViewById<ImageView>(R.id.settings_image).setOnClickListener { showSettingsDialog() }

        val token = TokenManager.getToken(requireContext())
        if (token == null) {
            Toast.makeText(requireContext(), "Авторизуйтесь заново", Toast.LENGTH_SHORT).show()
            return
        }

        // /me + портфель + котировки
        vm.loadMe(token)
        vm.loadPortfolio(token)
        vm.loadMarket()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collect { s ->
                s.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }

                // обновляем список портфеля
                if (portfolioList != s.portfolio) {
                    portfolioList.clear()
                    portfolioList.addAll(s.portfolio)
                    portfolioRVAdapter.notifyDataSetChanged()
                    updateTotals()
                }
            }
        }

        attachSwipeToDelete(token)
        startUpdatingPrices()
    }

    private fun attachSwipeToDelete(token: String) {
        val cb = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                val pos = vh.adapterPosition
                val item = portfolioList.getOrNull(pos)
                if (item?.id != null) {
                    vm.deleteTransaction(token, item.id)
                    portfolioList.removeAt(pos)
                    portfolioRVAdapter.notifyItemRemoved(pos)
                    updateTotals()
                } else {
                    portfolioRVAdapter.notifyItemChanged(pos)
                    Toast.makeText(requireContext(), "Не удалось удалить запись", Toast.LENGTH_SHORT).show()
                }
            }
        }
        ItemTouchHelper(cb).attachToRecyclerView(recyclerView)
    }

    private fun showSearchDialog() = SearchFragment().show(parentFragmentManager, "search_dialog")
    private fun showSettingsDialog() = SettingsFragment.newInstance().show(parentFragmentManager, "settings_dialog")

    private fun startUpdatingPrices() {
        updateJob?.cancel()
        updateJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                vm.loadMarket()
                delay(updateInterval)
            }
        }
    }

    override fun onDestroyView() { updateJob?.cancel(); super.onDestroyView() }

    // ==== расчёт сумм ====
    private fun updateTotals() { updatePortfolioValue(); updatePortfolioProfit() }

    private fun updatePortfolioValue() {
        var total = 0.0
        for (i in portfolioList) {
            val price = i.price.toDoubleOrNull() ?: 0.0
            val qty = i.quantity.toDoubleOrNull() ?: 0.0
            total += price * qty
        }
        totalValueTextView.text = "$${df2.format(total)}"
    }

    private fun updatePortfolioProfit() {
        var total = 0.0
        for (i in portfolioList) {
            val cur = i.price.toDoubleOrNull() ?: 0.0
            val buy = i.purchasePrice.toDoubleOrNull() ?: 0.0
            val qty = i.quantity.toDoubleOrNull() ?: 0.0
            total += (cur - buy) * qty
        }
        profitTextView.text = "$${df2.format(total)}"
    }

    // из диалога добавления
    override fun onAddTransaction(name: String, symbol: String, price: String, quantity: String, purchasePrice: String) {
        val token = TokenManager.getToken(requireContext()) ?: return
        val q = quantity.toDoubleOrNull() ?: 0.0
        val p = purchasePrice.toDoubleOrNull() ?: 0.0

        vm.addTransaction(token, name, symbol, q, p)
        // локально «пессимистично» обновим список
        portfolioList.add(
            PortfolioRVModel(
                name = name, symbol = symbol, price = price,
                quantity = quantity, purchasePrice = purchasePrice
            )
        )
        portfolioRVAdapter.notifyDataSetChanged()
        updateTotals()
    }
}
