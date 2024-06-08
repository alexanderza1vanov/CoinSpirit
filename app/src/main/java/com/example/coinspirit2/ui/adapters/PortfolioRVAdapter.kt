package com.example.coinspirit2.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coinspirit2.R
import com.example.coinspirit2.data.model.PortfolioRVModel
import java.text.DecimalFormat

class PortfolioRVAdapter(
    private val context: Context,
    private val portfolioItems: MutableList<PortfolioRVModel>
) : RecyclerView.Adapter<PortfolioRVAdapter.PortfolioViewHolder>() {

    private val df2 = DecimalFormat("#.##")



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortfolioViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_portfolio_rv, parent, false)
        return PortfolioViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PortfolioViewHolder, position: Int) {
        val item = portfolioItems[position]
        holder.tvName.text = item.name
        holder.tvSymbol.text = item.symbol

        // Преобразование строки в Double перед форматированием
        val price = item.price.toDoubleOrNull() ?: 0.0
        holder.tvPrice.text = "$${df2.format(price)}"

        holder.tvQuantity.text = item.quantity

        // Преобразование строки в Double перед форматированием
        val purchasePrice = item.purchasePrice.toDoubleOrNull() ?: 0.0
        holder.tvAvgPrice.text = "$${df2.format(purchasePrice)}"
        holder.tvQuantity.text = item.quantity
    }

    override fun getItemCount(): Int {
        return portfolioItems.size
    }

    class PortfolioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.TVNameCurrency)
        val tvSymbol: TextView = itemView.findViewById(R.id.TVSymbolCurrency)
        val tvPrice: TextView = itemView.findViewById(R.id.TVCurrencyPrice)
        val tvQuantity: TextView = itemView.findViewById(R.id.TVQuantity)
        val tvAvgPrice: TextView = itemView.findViewById(R.id.TVAvgPrice)
    }
}
