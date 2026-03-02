package com.example.coinspirit2.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.coinspirit2.R
import com.example.coinspirit2.databinding.FragmentLoginBinding
import com.example.coinspirit2.util.launchWhenStarted

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val vm: AuthViewModel by viewModels()
    private var _b: FragmentLoginBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentLoginBinding.bind(view)

        b.loginBtn.setOnClickListener {
            b.loginBtn.isEnabled = false
            vm.login(
                b.emailEt.text.toString().trim(),
                b.passwordEt.text.toString()
            ) {
                findNavController().navigate(R.id.action_login_to_home)
            }
        }

        b.registerTv.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        launchWhenStarted { vm.busy.collect { busy ->
            b.loginBtn.isEnabled = !busy
        } }

        // Показываем текст ошибки (сетевой/HTTP/парсинг и т.п.)
        launchWhenStarted { vm.error.collect { err ->
            if (!err.isNullOrBlank()) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show()
            }
        } }
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}
