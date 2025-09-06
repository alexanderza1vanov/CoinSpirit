package com.example.coinspirit2.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import com.example.coinspirit2.R
import com.example.coinspirit2.ui.activitys.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@SuppressLint("UseSwitchCompatOrMaterialCode")
class SettingsFragment : DialogFragment() {
    private lateinit var ivProfile: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var switchTheme: Switch
    private lateinit var btnLogout: Button
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        ivProfile = view.findViewById(R.id.ivProfile)
        tvUsername = view.findViewById(R.id.tvUsername)
        tvEmail = view.findViewById(R.id.tvEmail)
        switchTheme = view.findViewById(R.id.switchTheme)
        btnLogout = view.findViewById(R.id.btnLogout)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        // Заполняем поля
        val displayName = currentUser?.displayName ?: "User"
        val email = currentUser?.email ?: "No email"
        tvUsername.text = displayName
        tvEmail.text = email

        // Настройка тумблера темы
        switchTheme.isChecked = isDarkModeEnabled(requireContext())
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            saveThemePreference(requireContext(), isChecked)
            requireActivity().recreate() // Пересоздаём активность для применения новой темы
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
            dismiss()
            navigateToLogin()
        }

        return view
    }

    private fun navigateToLogin() {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        activity?.finish()
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