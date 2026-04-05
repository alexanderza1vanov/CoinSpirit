package com.example.coinspirit2.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.coinspirit2.R
import com.example.coinspirit2.databinding.FragmentSettingsBinding
import com.example.coinspirit2.util.launchWhenStarted

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private val vm: SettingsViewModel by viewModels()
    private var _b: FragmentSettingsBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentSettingsBinding.bind(view)


        b.btnBack.setOnClickListener {
            val nav = findNavController()
            if (!nav.popBackStack()) nav.navigate(R.id.homeFragment)
        }

        // Выход
        b.btnLogout.apply {
            setOnClickListener { vm.logout() }
        }


        launchWhenStarted {
            vm.user.collect { u ->
                if (u != null) {
                    b.tvUsername.text = u.displayName
                    b.tvEmail.text = u.email
                }
            }
        }

        // После выхода — на экран логина + очистить бэкстек
        launchWhenStarted {
            vm.done.collect { ok ->
                if (ok) {
                    findNavController().navigate(
                        R.id.loginFragment,
                        null,
                        navOptions {
                            // Если у тебя есть nav_graph_main / стартовый граф — скорректируй ID
                            popUpTo(R.id.nav_graph) { inclusive = true }
                        }
                    )
                }
            }
        }

        // Переключатель темы
        val dark = prefs().getBoolean(KEY_DARK, false)
        (b.switchTheme as SwitchCompat).isChecked = dark
        applyNightMode(dark)
        b.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            prefs().edit().putBoolean(KEY_DARK, isChecked).apply()
            applyNightMode(isChecked)
        }

        // Грузим профиль
        vm.loadMe()
    }

    private fun applyNightMode(dark: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun prefs() =
        requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }

    private companion object { const val KEY_DARK = "dark_mode" }
}
