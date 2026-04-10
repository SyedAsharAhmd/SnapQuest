package com.ashar.snapquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        btnSignup.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: ""
                        val userRef = FirebaseDatabase.getInstance()
                            .getReference("users").child(userId)
                        val userData = mapOf(
                            "username" to name,
                            "email" to email,
                            "userId" to userId
                        )
                        userRef.setValue(userData).addOnSuccessListener {
                            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvGoToLogin.setOnClickListener {
            finish()
        }
    }
}