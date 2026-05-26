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

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val scrollView = findViewById<ScrollView>(R.id.scrollViewLogin)
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

        Log.d(TAG, "Initialising AppData from LoginActivity")
        AppData.init(this)

        val etEmail    = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin   = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass  = etPassword.text.toString()

            when {
                email.isEmpty() -> etEmail.error = "Enter your email"
                pass.isEmpty()  -> etPassword.error = "Enter your password"
                else -> {
                    btnLogin.isEnabled = false
                    Log.d(TAG, "Login attempt for email: $email")

                    FirebaseManager.loginUser(
                        email = email,
                        password = pass,
                        onSuccess = {
                            Log.d(TAG, "Firebase login successful for email: $email")

                            // Try local login first
                            if (AppData.login(email, pass, this)) {
                                // User exists locally — proceed normally
                                Log.d(TAG, "Local login successful, navigating to MainActivity")
                                runOnUiThread {
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                            } else {
                                // Firebase knows this user but local DB doesn't
                                // This happens when: user was added via Firebase Console,
                                // or registered on a different device
                                Log.d(TAG, "User not in local DB — fetching Firebase profile to register locally")

                                FirebaseManager.fetchUserProfile { profile ->
                                    if (profile != null) {
                                        val (name, _) = profile
                                        Log.d(TAG, "Profile fetched: name=$name — registering locally")

                                        // Register them locally so the app works fully
                                        AppData.register(name, email, pass, this)

                                        // Now log in locally with their data loaded
                                        AppData.login(email, pass, this)
                                    } else {
                                        // No profile in DB either (Console-created user with no profile node)
                                        // Use email prefix as fallback name
                                        val fallbackName = email.substringBefore("@")
                                            .replaceFirstChar { it.uppercaseChar() }
                                        Log.d(TAG, "No profile found — using fallback name: $fallbackName")
                                        AppData.register(fallbackName, email, pass, this)
                                        AppData.login(email, pass, this)
                                    }

                                    runOnUiThread {
                                        btnLogin.isEnabled = true
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }
                                }
                            }
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Firebase login failed for email: $email, error: $error")
                            runOnUiThread {
                                btnLogin.isEnabled = true
                                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }

        tvRegister.setOnClickListener {
            Log.d(TAG, "Navigating to RegisterActivity")
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}