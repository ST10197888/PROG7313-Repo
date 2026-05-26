package com.budget.app.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.budget.app.R
import com.budget.app.firebase.FirebaseManager
import com.budget.app.utils.AppData

class RegisterActivity : AppCompatActivity() {

    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Fix keyboard pushing content off-screen on Android 11+
        val scrollView = findViewById<ScrollView>(R.id.scrollViewRegister)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                imeInsets.bottom.coerceAtLeast(navInsets.bottom)
            )
            insets
        }

        val etName     = findViewById<EditText>(R.id.etName)
        val etEmail    = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirm  = findViewById<EditText>(R.id.etConfirmPassword)
        val btnReg     = findViewById<Button>(R.id.btnRegister)
        val tvLogin    = findViewById<TextView>(R.id.tvLogin)

        btnReg.setOnClickListener {
            val name    = etName.text.toString().trim()
            val email   = etEmail.text.toString().trim()
            val pass    = etPassword.text.toString()
            val confirm = etConfirm.text.toString()

            Log.d(TAG, "Registration attempt for email: $email")
            when {
                name.isEmpty()       -> etName.error = "Enter your name"
                email.isEmpty()      -> etEmail.error = "Enter your email"
                !email.contains("@") -> etEmail.error = "Enter a valid email"
                pass.length < 6      -> etPassword.error = "Password must be at least 6 characters"
                pass != confirm      -> etConfirm.error = "Passwords do not match"
                !AppData.register(name, email, pass, this) -> {
                    Log.d(TAG, "Registration failed for email: $email, already registered")
                    Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Local registration succeeded — now register in Firebase
                    Log.d(TAG, "Local registration successful, registering with Firebase: $email")
                    btnReg.isEnabled = false

                    FirebaseManager.registerUser(
                        email = email,
                        password = pass,
                        name = name,
                        onSuccess = {
                            Log.d(TAG, "Firebase registration successful for email: $email")
                            runOnUiThread {
                                Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Firebase registration failed for email: $email, error: $error")
                            runOnUiThread {
                                btnReg.isEnabled = true
                                Toast.makeText(this, "Registration failed: $error", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }
        }

        tvLogin.setOnClickListener { finish() }
    }
}