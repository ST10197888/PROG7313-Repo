package com.budget.app.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.budget.app.R
import com.budget.app.activities.LoginActivity
import com.budget.app.activities.MainActivity
import com.budget.app.utils.AppData

class SettingsFragment : Fragment(), MainActivity.OnBackPressedListener {

    private lateinit var overlayContainer: View
    private lateinit var progressBarCard: CardView
    private lateinit var tvProgressBar: TextView
    private lateinit var tvWipingLabel: TextView
    private lateinit var btnCancelWipe: Button
    private var scrollView: ScrollView? = null

    private var wipingTimer: CountDownTimer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Attempt to find ScrollView in the hierarchy
        scrollView = view.findViewById(R.id.scrollView) ?: (view as? ScrollView) ?: findScrollView(view)

        val switchNotifications = view.findViewById<Switch>(R.id.switchNotifications)
        val switchDarkMode      = view.findViewById<Switch>(R.id.switchDarkMode)
        val btnClearData        = view.findViewById<Button>(R.id.btnClearData)

        overlayContainer = view.findViewById(R.id.overlayContainer)
        progressBarCard = view.findViewById(R.id.progressBarCard)
        tvProgressBar = view.findViewById(R.id.tvProgressBar)
        tvWipingLabel = view.findViewById(R.id.tvWipingLabel)
        btnCancelWipe = view.findViewById(R.id.btnCancelWipe)

        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        
        switchDarkMode.isChecked = prefs.getBoolean("dark_mode", false)
        switchNotifications.isChecked = prefs.getBoolean("notifications", true)

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications", isChecked).apply()
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        btnClearData.setOnClickListener {
            showPasswordConfirmationDialog()
        }

        btnCancelWipe.setOnClickListener {
            wipingTimer?.cancel()
            hideOverlay()
            Toast.makeText(requireContext(), "Wiping cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findScrollView(view: View): ScrollView? {
        if (view is ScrollView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = findScrollView(view.getChildAt(i))
                if (child != null) return child
            }
        }
        return null
    }

    override fun onBackPressed(): Boolean {
        scrollView?.let {
            if (it.scrollY > 0) {
                it.smoothScrollTo(0, 0)
                return true
            }
        }
        return false
    }

    private fun showPasswordConfirmationDialog() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = "Enter your password"

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Please enter your password to clear all data. This cannot be undone.")
            .setView(input)
            .setPositiveButton("Clear Everything") { _, _ ->
                val enteredPassword = input.text.toString()
                if (enteredPassword == AppData.currentUser?.password) {
                    startWipingProcess()
                } else {
                    Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startWipingProcess() {
        val userName = AppData.currentUser?.name ?: "User"
        tvWipingLabel.text = "Wiping $userName Data..."
        showOverlay()

        var elapsed = 0L
        val totalDuration = 3500L
        val tickInterval = 50L

        wipingTimer = object : CountDownTimer(totalDuration, tickInterval) {
            override fun onTick(millisUntilFinished: Long) {
                elapsed += tickInterval
                val progress = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
                updateProgressBarText(progress)
            }

            override fun onFinish() {
                updateProgressBarText(1f)
                completeWiping()
            }
        }.start()
    }

    private fun updateProgressBarText(progress: Float) {
        val length = 20
        val filledCount = (progress * length).toInt()
        val emptyCount = length - filledCount
        val percentage = (progress * 100).toInt()

        val bar = buildString {
            append("[")
            repeat(filledCount) { append("█") }
            repeat(emptyCount) { append("░") }
            append("] $percentage%")
        }
        tvProgressBar.text = bar
    }

    private fun showOverlay() {
        overlayContainer.visibility = View.VISIBLE
        progressBarCard.visibility = View.VISIBLE
    }

    private fun hideOverlay() {
        overlayContainer.visibility = View.GONE
        progressBarCard.visibility = View.GONE
    }

    private fun completeWiping() {
        // Clear data
        AppData.getAllTransactions().toList().forEach { AppData.removeTransaction(it.id) }
        AppData.getBudgetGoals().toList().forEach { AppData.removeBudgetGoal(it.category) }
        
        // Force Logout
        AppData.logout()
        
        Toast.makeText(requireContext(), "All data wiped. Logging out...", Toast.LENGTH_SHORT).show()
        
        // Go to Login
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        wipingTimer?.cancel()
    }
}
