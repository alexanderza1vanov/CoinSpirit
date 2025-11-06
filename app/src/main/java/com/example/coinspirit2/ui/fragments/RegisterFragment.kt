package com.example.coinspirit2.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.coinspirit2.R
import com.example.coinspirit2.databinding.FragmentRegisterBinding
import com.example.coinspirit2.ui.auth.AuthViewModel
import com.example.coinspirit2.utils.TokenManager
import kotlinx.coroutines.flow.collectLatest

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val vm: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentRegisterBinding.bind(view)

        binding.backBtn.setOnClickListener { findNavController().popBackStack() }

        binding.signUpBtn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.register(email, password)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { s ->
                s.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
                s.tokens?.let {
                    TokenManager.saveToken(requireContext(), it.accessToken)
                    findNavController().navigate(R.id.action_register_to_home)
                }
            }
        }
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
