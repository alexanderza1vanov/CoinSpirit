package com.example.coinspirit2.ui.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.coinspirit2.ui.interfaces.DataTransfer
import com.example.coinspirit2.R

class CurrencyDetailFragment : DialogFragment() {

    private var name: String? = null
    private var symbol: String? = null
    private var price: Double = 0.0
    private lateinit var dataTransferInterface: DataTransfer

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        return dialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            dataTransferInterface = context as DataTransfer
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement DataTransfer")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(ARG_NAME)
            symbol = it.getString(ARG_SYMBOL)
            price = it.getDouble(ARG_PRICE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_currency_detail, container, false)

        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvSymbol: TextView = view.findViewById(R.id.tvSymbol)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val etQuantity: EditText = view.findViewById(R.id.etQuantity)
        val etPurchasePrice: EditText = view.findViewById(R.id.etPurchasePrice)
        val btnAddTransaction: Button = view.findViewById(R.id.btnAddTransaction)

        tvName.text = name
        tvSymbol.text = symbol
        tvPrice.text = String.format("$ %.2f", price)

        btnAddTransaction.setOnClickListener {
            val quantity = etQuantity.text.toString()
            val purchasePrice = etPurchasePrice.text.toString()

            if (quantity.isNotEmpty() && purchasePrice.isNotEmpty()) {
                dataTransferInterface.onAddTransaction(name!!, symbol!!, price.toString(), quantity, purchasePrice)
                dismiss()
            } else {
                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_SYMBOL = "symbol"
        private const val ARG_PRICE = "price"

        @JvmStatic
        fun newInstance(name: String, symbol: String, price: Double) =
            CurrencyDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAME, name)
                    putString(ARG_SYMBOL, symbol)
                    putDouble(ARG_PRICE, price)
                }
            }
    }
}
