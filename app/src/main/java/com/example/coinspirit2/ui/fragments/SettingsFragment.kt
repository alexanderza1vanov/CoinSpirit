package com.example.coinspirit2.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.coinspirit2.R
import com.example.coinspirit2.data.repository.AuthRepository
import com.example.coinspirit2.utils.TokenManager
import kotlinx.coroutines.launch

class SettingsFragment : DialogFragment() {

    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchTheme: Switch
    private lateinit var btnLogout: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        tvUsername = view.findViewById(R.id.tvUsername)
        tvEmail    = view.findViewById(R.id.tvEmail)
        switchTheme = view.findViewById(R.id.switchTheme)
        btnLogout   = view.findViewById(R.id.btnLogout)

        // данные пользователя с /me
        val token = TokenManager.getToken(requireContext())
        if (token != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                AuthRepository().me(token)
                    .onSuccess { user ->
                        tvUsername.text = user.email.substringBefore("@")
                        tvEmail.text = user.email
                    }
            }
        }

        // тема
        switchTheme.isChecked = isDarkModeEnabled(requireContext())
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            saveThemePreference(requireContext(), isChecked)
            requireActivity().recreate()
        }

        btnLogout.setOnClickListener {
            TokenManager.clearToken(requireContext())
            Toast.makeText(context, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        return view
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()

        private const val PREFS_NAME = "theme_prefs"
        private const val PREF_DARK_MODE = "is_dark_mode"

        fun isDarkModeEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_DARK_MODE, false)
        }

        fun saveThemePreference(context: Context, isDarkMode: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(PREF_DARK_MODE, isDarkMode).apply()
        }
    }
}
