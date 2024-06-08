package com.example.coinspirit2.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coinspirit2.R
import com.example.coinspirit2.data.model.CurrencyRVModel
import java.text.DecimalFormat

class CurrencyRVAdapter(
    private var currencyRVModelArrayList: ArrayList<CurrencyRVModel>,
    private val context: Context,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<CurrencyRVAdapter.ViewHolder>() {

    private val df2 = DecimalFormat("#.##")

    fun filterList(filteredList: ArrayList<CurrencyRVModel>) {
        currencyRVModelArrayList = filteredList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.currency_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currencyRVModel = currencyRVModelArrayList[position]
        holder.tvName.text = currencyRVModel.name
        holder.tvSymbol.text = currencyRVModel.symbol
        holder.tvCurrencyRate.text = "$ ${df2.format(currencyRVModel.price)}"

        holder.itemView.setOnClickListener { onItemClickListener.onItemClick(currencyRVModel) }
    }

    override fun getItemCount(): Int {
        return currencyRVModelArrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.TVName)
        val tvSymbol: TextView = itemView.findViewById(R.id.TVSymbol)
        val tvCurrencyRate: TextView = itemView.findViewById(R.id.TVCurrencyRate)
    }

    interface OnItemClickListener {
        fun onItemClick(currency: CurrencyRVModel)
    }
}
