package com.budget.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.budget.app.R
import com.budget.app.activities.LoginActivity
import com.budget.app.activities.MainActivity
import com.budget.app.utils.AppData
import com.budget.app.utils.CurrencyFormatter

class ProfileFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName              = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail             = view.findViewById<TextView>(R.id.tvProfileEmail)
        val tvTxCount           = view.findViewById<TextView>(R.id.tvTxCount)
        val tvTotalSaved        = view.findViewById<TextView>(R.id.tvTotalSaved)
        val tvSummaryIncome     = view.findViewById<TextView>(R.id.tvSummaryIncome)
        val tvSummaryExpenses   = view.findViewById<TextView>(R.id.tvSummaryExpenses)
        val tvSummarySavings    = view.findViewById<TextView>(R.id.tvSummarySavings)
        val tvSummarySavingsRate = view.findViewById<TextView>(R.id.tvSummarySavingsRate)
        val btnSettings         = view.findViewById<Button>(R.id.btnProfileSettings)
        val btnLogout           = view.findViewById<Button>(R.id.btnLogout)

        val user = AppData.currentUser
        tvName.text       = user?.name ?: "Unknown"
        tvEmail.text      = user?.email ?: ""
        
        val transactions = AppData.getAllTransactions()
        tvTxCount.text    = "${transactions.size} transactions logged"
        tvTotalSaved.text = "Remaining balance: ${CurrencyFormatter.format(AppData.getBalance())}"

        // Financial Summary Data
        val income   = AppData.getTotalIncome()
        val expenses = AppData.getTotalExpenses()
        val savings  = AppData.getTotalSavings()
        val savingsRate = if (income > 0) ((savings / income) * 100).toInt().coerceIn(0, 100) else 0

        tvSummaryIncome.text   = CurrencyFormatter.format(income)
        tvSummaryExpenses.text = CurrencyFormatter.format(expenses)
        tvSummarySavings.text  = CurrencyFormatter.format(savings)
        tvSummarySavingsRate.text = "Overall Savings Rate: $savingsRate%"

        btnSettings.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(SettingsFragment())
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out") { _, _ ->
                    AppData.logout()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
