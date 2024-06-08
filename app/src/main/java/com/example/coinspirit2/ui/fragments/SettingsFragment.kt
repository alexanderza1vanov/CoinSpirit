package com.example.coinspirit2.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.coinspirit2.R
import com.example.coinspirit2.ui.activitys.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SettingsFragment : DialogFragment() {


    private lateinit var tvEmail: TextView
    private lateinit var btnLogout: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)


        tvEmail = view.findViewById(R.id.tvEmail)
        btnLogout = view.findViewById(R.id.btnLogout)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        // Set username and email

        tvEmail.text = currentUser.email ?: "No email"

        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
            dismiss()
            navigateToLogin()
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setTitle("Settings")
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        activity?.finish()
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}
