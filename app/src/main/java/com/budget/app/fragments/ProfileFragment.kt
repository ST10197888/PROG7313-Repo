package com.budget.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
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
        val tvTotalDebt         = view.findViewById<TextView>(R.id.tvTotalDebt)
        val pbFitness           = view.findViewById<ProgressBar>(R.id.pbProfileFitness)
        val tvFitnessScore      = view.findViewById<TextView>(R.id.tvProfileFitnessScore)
        val layoutBadges        = view.findViewById<LinearLayout>(R.id.layoutProfileBadges)
        val tvSummaryIncome     = view.findViewById<TextView>(R.id.tvSummaryIncome)
        val tvSummaryExpenses   = view.findViewById<TextView>(R.id.tvSummaryExpenses)
        val tvSummarySavings    = view.findViewById<TextView>(R.id.tvSummarySavings)
        val tvSummarySavingsRate = view.findViewById<TextView>(R.id.tvSummarySavingsRate)
        val btnSettings         = view.findViewById<Button>(R.id.btnProfileSettings)
        val btnLogout           = view.findViewById<Button>(R.id.btnLogout)

        // Usage block views
        val tvUsageTotalAmount  = view.findViewById<TextView>(R.id.tvUsageTotalAmount)
        val pbUsageTotal        = view.findViewById<ProgressBar>(R.id.pbUsageTotal)
        val tvUsageIncomePercent = view.findViewById<TextView>(R.id.tvUsageIncomePercent)
        val pbUsageIncome       = view.findViewById<ProgressBar>(R.id.pbUsageIncome)
        val tvUsageExpensePercent = view.findViewById<TextView>(R.id.tvUsageExpensePercent)
        val pbUsageExpense      = view.findViewById<ProgressBar>(R.id.pbUsageExpense)
        val tvUsageSavingsPercent = view.findViewById<TextView>(R.id.tvUsageSavingsPercent)
        val pbUsageSavings      = view.findViewById<ProgressBar>(R.id.pbUsageSavings)
        val tvUsageDebtPercent  = view.findViewById<TextView>(R.id.tvUsageDebtPercent)
        val pbUsageDebt         = view.findViewById<ProgressBar>(R.id.pbUsageDebt)

        val user = AppData.currentUser
        tvName.text       = user?.name ?: "Unknown"
        tvEmail.text      = user?.email ?: ""
        
        val transactions = AppData.getAllTransactions()
        tvTxCount.text    = "Total Transactions: ${transactions.size}"
        tvTotalSaved.text = "Remaining balance: ${CurrencyFormatter.format(AppData.getBalance())}"
        
        val totalDebtValue = AppData.getDebts().sumOf { it.remainingAmount }
        tvTotalDebt.text  = "Acquired Debt: ${CurrencyFormatter.format(totalDebtValue)}"

        // Financial Fitness
        val fitnessScore = AppData.getFinancialFitnessScore()
        pbFitness.progress = fitnessScore.toInt()
        tvFitnessScore.text = "Score: ${fitnessScore.toInt()}%"

        // Update Usage Block
        val incomeValue   = AppData.getTotalIncome()
        val expensesValue = AppData.getTotalExpenses()
        val savingsValue  = AppData.getTotalSavings()
        val totalVolume   = incomeValue + expensesValue + savingsValue + totalDebtValue

        tvUsageTotalAmount.text = "100% - ${CurrencyFormatter.format(totalVolume)}"
        pbUsageTotal.progress = 100 // Baseline is always 100%

        if (totalVolume > 0) {
            val incPercent = ((incomeValue / totalVolume) * 100).toInt()
            val expPercent = ((expensesValue / totalVolume) * 100).toInt()
            val savPercent = ((savingsValue / totalVolume) * 100).toInt()
            val debtPercent = ((totalDebtValue / totalVolume) * 100).toInt()

            tvUsageIncomePercent.text = "$incPercent%"
            pbUsageIncome.progress = incPercent

            tvUsageExpensePercent.text = "$expPercent%"
            pbUsageExpense.progress = expPercent

            tvUsageSavingsPercent.text = "$savPercent%"
            pbUsageSavings.progress = savPercent

            tvUsageDebtPercent.text = "$debtPercent%"
            pbUsageDebt.progress = debtPercent
        } else {
            listOf(tvUsageIncomePercent, tvUsageExpensePercent, tvUsageSavingsPercent, tvUsageDebtPercent).forEach { it.text = "0%" }
            listOf(pbUsageIncome, pbUsageExpense, pbUsageSavings, pbUsageDebt).forEach { it.progress = 0 }
        }

        // Badges
        updateBadges(layoutBadges)

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

    private fun updateBadges(container: LinearLayout) {
        container.removeAllViews()
        val unlocked = AppData.getAchievements().filter { it.isUnlocked }
        
        if (unlocked.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "No badges earned yet. Start managing your budget to unlock achievements!"
                textSize = 14f
                setTextColor(requireContext().getColor(R.color.text_secondary))
            }
            container.addView(tv)
        } else {
            unlocked.forEach { achievement ->
                val badgeLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 8, 0, 8)
                    gravity = android.view.Gravity.CENTER_VERTICAL
                }

                val icon = ImageView(requireContext()).apply {
                    val size = (32 * resources.displayMetrics.density).toInt()
                    layoutParams = LinearLayout.LayoutParams(size, size).apply {
                        setMargins(0, 0, 16, 0)
                    }
                    setImageResource(android.R.drawable.star_on)
                }

                val textLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                }

                val title = TextView(requireContext()).apply {
                    text = achievement.title
                    textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setTextColor(requireContext().getColor(R.color.text_primary))
                }

                val desc = TextView(requireContext()).apply {
                    text = achievement.description
                    textSize = 12f
                    setTextColor(requireContext().getColor(R.color.text_secondary))
                }

                textLayout.addView(title)
                textLayout.addView(desc)
                
                badgeLayout.addView(icon)
                badgeLayout.addView(textLayout)
                
                container.addView(badgeLayout)
            }
        }
    }
}
