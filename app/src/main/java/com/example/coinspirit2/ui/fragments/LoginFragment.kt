package com.example.coinspirit2.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.coinspirit2.R
import com.example.coinspirit2.databinding.FragmentLoginBinding
import com.example.coinspirit2.ui.auth.AuthViewModel
import com.example.coinspirit2.utils.TokenManager
import kotlinx.coroutines.flow.collectLatest

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val vm: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentLoginBinding.bind(view)

        binding.registerTv.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Введите e‑mail и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.login(email, password)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { s ->
                s.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
                s.tokens?.let {
                    TokenManager.saveToken(requireContext(), it.accessToken)
                    findNavController().navigate(R.id.action_login_to_home)
                }
            }
        }
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
