package com.example.coinspirit2.ui.currency

import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.coinspirit2.R
import com.example.coinspirit2.data.remote.CreateTxRequest
import com.example.coinspirit2.databinding.FragmentCurrencyDetailBinding
import com.example.coinspirit2.util.launchWhenStarted

class CurrencyDetailFragment : Fragment(R.layout.fragment_currency_detail) {

    private var _b: FragmentCurrencyDetailBinding? = null
    private val b get() = _b!!
    private val vm: CurrencyDetailViewModel by viewModels()

    private var editTxId: Int = -1
    private var symbol: String = ""

    private companion object {
        const val MAX_NOTE = 300
        const val ARG_SYMBOL = "symbol"
        const val ARG_EDIT_ID = "editTxId"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentCurrencyDetailBinding.bind(view)

        // args
        symbol = requireArguments().getString(ARG_SYMBOL).orEmpty()
        editTxId = requireArguments().getInt(ARG_EDIT_ID, -1)

        // header
        b.tvSymbol.text = symbol
        b.tvName.text = symbol

        // limit note length (safety: UI + runtime)
        b.edtNote.filters = arrayOf(InputFilter.LengthFilter(MAX_NOTE))

        // mode
        if (editTxId >= 0) {
            vm.loadTransaction(editTxId)
            b.btnPrimary.text = getStringSafe(R.string.save, "Сохранить")
        } else {
            b.btnPrimary.text = getStringSafe(R.string.add, "Добавить")
        }

        // collect vm
        launchWhenStarted {
            vm.txToEdit.collect { tx ->
                if (tx != null) {
                    // Prefill
                    b.edtPrice.setText(tx.price)
                    b.edtQty.setText(tx.quantity)
                    b.edtNote.setText(tx.note ?: "")

                    if (symbol.isBlank()) {
                        symbol = tx.symbol
                        b.tvSymbol.text = symbol
                        b.tvName.text = symbol
                    }

                    // Set BUY/SELL by sign of qty
                    runCatching {
                        val q = tx.quantity.replace(',', '.').toDouble()
                        b.rbBuy.isChecked = q >= 0
                        b.rbSell.isChecked = q < 0
                    }
                }
            }
        }
        launchWhenStarted {
            vm.loading.collect { loading ->
                b.pb.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }

        // actions
        b.btnPrimary.setOnClickListener {
            val priceStr = b.edtPrice.text?.toString()?.trim().orEmpty()
            val qtyStr   = b.edtQty.text?.toString()?.trim().orEmpty()
            val noteStr  = b.edtNote.text?.toString()
                ?.take(MAX_NOTE)
                ?.trim()
                ?.takeIf { it.isNotBlank() }

            if (priceStr.isBlank() || qtyStr.isBlank()) {
                toast(getStringSafe(R.string.fill_required, "Заполните цену и количество"))
                return@setOnClickListener
            }

            // SELL => negative qty; BUY => positive (no leading '-')
            val isSell = b.rbSell.isChecked
            val finalQty = if (isSell) {
                if (qtyStr.startsWith("-")) qtyStr else "-$qtyStr"
            } else {
                qtyStr.removePrefix("-")
            }

            val req = CreateTxRequest(
                symbol   = symbol,
                type     = if (isSell) "SELL" else "BUY",
                price    = priceStr,
                quantity = finalQty,
                note     = noteStr
            )

            if (editTxId >= 0) {
                vm.updateTransaction(editTxId, req) {
                    toast(getStringSafe(R.string.saved, "Сохранено"))
                    findNavController().popBackStack()
                }
            } else {
                vm.createTransaction(req) {
                    toast(getStringSafe(R.string.added, "Добавлено"))
                    findNavController().popBackStack()
                }
            }
        }

        b.btnBack.setOnClickListener {
            val nav = findNavController()
            if (!nav.popBackStack()) nav.navigateUp()
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    private fun getStringSafe(resId: Int, fallback: String): String =
        runCatching { getString(resId) }.getOrElse { fallback }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}
