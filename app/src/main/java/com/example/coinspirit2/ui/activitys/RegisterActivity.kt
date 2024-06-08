package com.example.coinspirit2.ui.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coinspirit2.ui.activitys.MainActivity
import com.example.coinspirit2.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.backBtn.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
        }

        binding.signUpBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()
            val username = binding.usernameEt.text.toString()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(applicationContext, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this@RegisterActivity) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.let {
                                val userId = it.uid
                                val userRef: DocumentReference = db.collection("Users").document(userId)
                                userRef.set(User(email, username))
                                    .addOnSuccessListener {
                                        Log.d("RegisterActivity", "User data saved successfully")
                                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("RegisterActivity", "Failed to save user data", e)
                                        Toast.makeText(applicationContext, "Failed to save user data. Please try again.", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Log.e("RegisterActivity", "User registration failed", task.exception)
                            Toast.makeText(applicationContext, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    data class User(
        var email: String = "",
        var username: String = ""
    )
}
