package com.example.coinspirit2.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.coinspirit2.R
import com.example.coinspirit2.databinding.FragmentRegisterBinding
import com.example.coinspirit2.util.launchWhenStarted

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private val vm: AuthViewModel by viewModels()
    private var _b: FragmentRegisterBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentRegisterBinding.bind(view)

        b.signUpBtn.setOnClickListener {
            val name = b.usernameEt.text?.toString()?.trim().orEmpty()        // ← ИМЯ
            val email = b.emailEt.text?.toString()?.trim().orEmpty()
            val pass = b.passwordEt.text?.toString().orEmpty()

            if (email.isBlank() || pass.length < 6) {
                Toast.makeText(requireContext(), getString(R.string.fill_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            vm.register(email, pass, name) {
                findNavController().navigate(R.id.action_register_to_home)
            }
        }

        b.backBtn.setOnClickListener { findNavController().popBackStack() }

        launchWhenStarted { vm.busy.collect { b.signUpBtn.isEnabled = !it } }
        launchWhenStarted {
            vm.error.collect { msg ->
                msg?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
            }
        }
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}