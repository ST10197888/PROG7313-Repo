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
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.budget.app.R
import com.budget.app.activities.LoginActivity
import com.budget.app.activities.MainActivity
import com.budget.app.utils.AppData
import com.budget.app.utils.CurrencyFormatter

class ProfileFragment : Fragment(), MainActivity.OnBackPressedListener {

    private lateinit var scrollView: ScrollView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scrollView = view.findViewById(R.id.scrollViewProfile)
        
        val tvName              = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail             = view.findViewById<TextView>(R.id.tvProfileEmail)
        val tvTxCount           = view.findViewById<TextView>(R.id.tvTxCount)
        val tvTotalSaved        = view.findViewById<TextView>(R.id.tvTotalSaved)
        val tvTotalDebt         = view.findViewById<TextView>(R.id.tvTotalDebt)
        
        val pbFitness           = view.findViewById<ProgressBar>(R.id.pbProfileFitness)
        val tvFitnessScore      = view.findViewById<TextView>(R.id.tvProfileFitnessScore)
        val pbFitnessSurplus    = view.findViewById<ProgressBar>(R.id.pbFitnessSurplus)
        val tvFitnessSurplus    = view.findViewById<TextView>(R.id.tvFitnessSurplusScore)
        val pbFitnessSavings    = view.findViewById<ProgressBar>(R.id.pbFitnessSavings)
        val tvFitnessSavings    = view.findViewById<TextView>(R.id.tvFitnessSavingsScore)
        
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
        tvTxCount.text    = transactions.size.toString()
        
        val totalIncome   = AppData.getTotalIncome()
        val totalExpenses = AppData.getTotalExpenses()
        val totalSavings  = AppData.getTotalSavings()
        val balance       = AppData.getBalance()
        val totalDebtValue = AppData.getDebts().sumOf { it.remainingAmount }

        tvTotalSaved.text = CurrencyFormatter.format(balance)
        tvTotalDebt.text  = CurrencyFormatter.format(totalDebtValue)
        tvSummaryIncome.text   = CurrencyFormatter.format(totalIncome)
        tvSummaryExpenses.text = CurrencyFormatter.format(totalExpenses)
        tvSummarySavings.text  = CurrencyFormatter.format(totalSavings)
        
        val savingsRate = if (totalIncome > 0) ((totalSavings / totalIncome) * 100).toInt().coerceIn(0, 100) else 0
        tvSummarySavingsRate.text = "$savingsRate%"

        // ENHANCED Financial Fitness Calculation Logic Mirroring AppData
        val fitnessScore = AppData.getFinancialFitnessScore()
        pbFitness.progress = fitnessScore.toInt()
        tvFitnessScore.text = "Total Score: ${fitnessScore.toInt()}%"

        if (totalIncome > 0) {
            val surplusRatio = ((totalIncome - totalExpenses) / totalIncome).coerceIn(0.0, 1.0)
            val surplusScore = surplusRatio * 70.0
            pbFitnessSurplus.progress = surplusScore.toInt()
            tvFitnessSurplus.text = "${surplusScore.toInt()}/70"

            val savingsRatio = (totalSavings / totalIncome) / 0.20
            val savingsScore = (savingsRatio * 30.0).coerceAtMost(30.0)
            pbFitnessSavings.progress = savingsScore.toInt()
            tvFitnessSavings.text = "${savingsScore.toInt()}/30"
        } else {
            pbFitnessSurplus.progress = 0
            tvFitnessSurplus.text = "0/70"
            pbFitnessSavings.progress = 0
            tvFitnessSavings.text = "0/30"
        }

        // Update Usage Block
        val totalVolume   = totalIncome + totalExpenses + totalSavings + totalDebtValue
        tvUsageTotalAmount.text = CurrencyFormatter.format(totalVolume)
        pbUsageTotal.progress = 100

        if (totalVolume > 0) {
            val incPercent = ((totalIncome / totalVolume) * 100).toInt()
            val expPercent = ((totalExpenses / totalVolume) * 100).toInt()
            val savPercent = ((totalSavings / totalVolume) * 100).toInt()
            val debtPercent = ((totalDebtValue / totalVolume) * 100).toInt()

            tvUsageIncomePercent.text = "$incPercent%"
            pbUsageIncome.progress = incPercent
            tvUsageExpensePercent.text = "$expPercent%"
            pbUsageExpense.progress = expPercent
            tvUsageSavingsPercent.text = "$savPercent%"
            pbUsageSavings.progress = savPercent
            tvUsageDebtPercent.text = "$debtPercent%"
            pbUsageDebt.progress = debtPercent
        }

        updateBadges(layoutBadges)

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

    override fun onBackPressed(): Boolean {
        if (::scrollView.isInitialized && scrollView.scrollY > 0) {
            scrollView.smoothScrollTo(0, 0)
            return true
        }
        return false
    }

    private fun updateBadges(container: LinearLayout) {
        container.removeAllViews()
        val unlocked = AppData.getAchievements().filter { it.isUnlocked }
        
        if (unlocked.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "No badges earned yet."
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
