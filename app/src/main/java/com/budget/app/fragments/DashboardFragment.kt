package com.budget.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.budget.app.R
import com.budget.app.activities.MainActivity
import com.budget.app.adapters.TransactionAdapter
import com.budget.app.models.Transaction
import com.budget.app.models.TransactionType
import com.budget.app.utils.AppData
import com.budget.app.utils.CurrencyFormatter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class DashboardFragment : Fragment(), MainActivity.OnBackPressedListener {

    private lateinit var scrollView: ScrollView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scrollView = view.findViewById(R.id.scrollView)

        // Basic Views
        val tvWelcome      = view.findViewById<TextView>(R.id.tvWelcome)
        val tvBalance      = view.findViewById<TextView>(R.id.tvBalance)
        val tvIncome       = view.findViewById<TextView>(R.id.tvIncome)
        val tvExpenses     = view.findViewById<TextView>(R.id.tvExpenses)
        val tvUnallocated  = view.findViewById<TextView>(R.id.tvUnallocated)
        val pbFitness      = view.findViewById<ProgressBar>(R.id.pbFitness)
        val tvFitnessScore = view.findViewById<TextView>(R.id.tvFitnessScore)
        val layoutBadges   = view.findViewById<LinearLayout>(R.id.layoutBadges)
        val layoutChart    = view.findViewById<LinearLayout>(R.id.layoutBarChart)
        val rvRecent       = view.findViewById<RecyclerView>(R.id.rvRecentTransactions)
        val fabAdd         = view.findViewById<FloatingActionButton>(R.id.fabAdd)
        val tvSeeAll       = view.findViewById<TextView>(R.id.tvSeeAll)

        // Quick Actions
        val cardActionAdd          = view.findViewById<CardView>(R.id.cardActionAdd)
        val cardActionTransactions = view.findViewById<CardView>(R.id.cardActionTransactions)
        val cardActionBudget       = view.findViewById<CardView>(R.id.cardActionBudget)
        val cardActionReports      = view.findViewById<CardView>(R.id.cardActionReports)

        // More Features
        val cardGoals       = view.findViewById<CardView>(R.id.cardGoals)
        val cardDebts       = view.findViewById<CardView>(R.id.cardDebts)
        val cardCategories  = view.findViewById<CardView>(R.id.cardCategories)
        val cardEducation   = view.findViewById<CardView>(R.id.cardEducation)
        val cardAttachments = view.findViewById<CardView>(R.id.cardAttachments)

        // Set Data
        val name = AppData.currentUser?.name ?: ""
        tvWelcome.text  = "Hello $name"
        tvBalance.text  = CurrencyFormatter.format(AppData.getBalance())
        tvIncome.text   = CurrencyFormatter.format(AppData.getTotalIncome())
        tvExpenses.text = CurrencyFormatter.format(AppData.getTotalExpenses())
        
        tvUnallocated.text = CurrencyFormatter.format(AppData.getUnallocatedIncome())

        val score = AppData.getFinancialFitnessScore()
        pbFitness.progress = score.toInt()
        tvFitnessScore.text = "${score.toInt()}%"

        updateBadges(layoutBadges)
        setupMiniTripleBar(layoutChart)

        val recent = AppData.getAllTransactions().take(5)
        rvRecent.layoutManager = LinearLayoutManager(requireContext())
        rvRecent.adapter = TransactionAdapter(recent, onDelete = null)

        // Click Listeners
        val main = activity as? MainActivity

        fabAdd.setOnClickListener { main?.navigateTo(R.id.nav_add) }
        tvSeeAll.setOnClickListener { main?.navigateTo(R.id.nav_transactions) }

        // Quick Action Listeners
        cardActionAdd.setOnClickListener          { main?.navigateTo(R.id.nav_add) }
        cardActionTransactions.setOnClickListener { main?.navigateTo(R.id.nav_transactions) }
        cardActionBudget.setOnClickListener       { main?.navigateTo(R.id.nav_budget_goals) }
        cardActionReports.setOnClickListener      { main?.navigateTo(R.id.nav_reports) }

        // More Features Listeners
        cardGoals.setOnClickListener       { main?.navigateTo(R.id.nav_goals) }
        cardDebts.setOnClickListener       { main?.navigateTo(R.id.nav_debts) }
        cardCategories.setOnClickListener  { main?.navigateTo(R.id.nav_categories) }
        cardEducation.setOnClickListener   { main?.navigateTo(R.id.nav_education) }
        cardAttachments.setOnClickListener { main?.navigateTo(R.id.nav_attachments) }
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
        
        val tv = TextView(requireContext()).apply {
            text = "🏅 ${unlocked.size}"
            textSize = 13f
            setTextColor(requireContext().getColor(R.color.colorPrimary))
        }
        container.addView(tv)
    }

    private fun setupMiniTripleBar(container: LinearLayout) {
        container.removeAllViews()

        val cal = Calendar.getInstance()
        val cm  = cal.get(Calendar.MONTH)
        val cy  = cal.get(Calendar.YEAR)
        cal.add(Calendar.MONTH, -1)
        val lm  = cal.get(Calendar.MONTH)
        val ly  = cal.get(Calendar.YEAR)

        val lmData = calculateWeeklyData(AppData.getTransactionsForMonth(lm, ly))
        val cmData = calculateWeeklyData(AppData.getTransactionsForMonth(cm, cy))
        val maxVal = (lmData + cmData).maxOf { it.max() }.coerceAtLeast(1.0)

        val wrapper = LinearLayout(requireContext()).apply {
            orientation   = LinearLayout.HORIZONTAL
            layoutParams  = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            gravity = android.view.Gravity.BOTTOM
        }

        fun addDivider() {
            wrapper.addView(View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                    setMargins(4, 0, 4, 0)
                }
                setBackgroundColor(requireContext().getColor(R.color.background_grey))
            })
        }

        lmData.forEachIndexed { i, data ->
            addMiniGroup(wrapper, data, "W${i + 1}", maxVal, isCurrentMonth = false)
        }

        addDivider()

        cmData.forEachIndexed { i, data ->
            addMiniGroup(wrapper, data, "W${i + 1}", maxVal, isCurrentMonth = true)
        }

        container.addView(wrapper)
    }

    private fun addMiniGroup(
        container: LinearLayout,
        data: WeeklyData,
        label: String,
        maxVal: Double,
        isCurrentMonth: Boolean
    ) {
        val group = LinearLayout(requireContext()).apply {
            orientation  = LinearLayout.VERTICAL
            gravity      = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                setMargins(2, 0, 2, 0)
            }
        }

        val barsRow = LinearLayout(requireContext()).apply {
            orientation  = LinearLayout.HORIZONTAL
            gravity      = android.view.Gravity.BOTTOM
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        fun bar(amount: Double, colorRes: Int) {
            val maxPx    = (120 * resources.displayMetrics.density).toInt()
            val heightPx = ((amount / maxVal) * maxPx).toInt().coerceAtLeast(if (amount > 0) 4 else 0)
            barsRow.addView(View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, heightPx, 1f).apply {
                    setMargins(1, 0, 1, 0)
                }
                setBackgroundColor(requireContext().getColor(colorRes))
                alpha = if (isCurrentMonth) 1f else 0.5f
            })
        }

        bar(data.income,  R.color.income_green)
        bar(data.expense, R.color.expense_red)
        bar(data.savings, R.color.colorPrimary)

        val tvLabel = TextView(requireContext()).apply {
            text      = label
            textSize  = 7f
            gravity   = android.view.Gravity.CENTER
            setTextColor(requireContext().getColor(
                if (isCurrentMonth) R.color.text_primary else R.color.text_secondary
            ))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        group.addView(barsRow)
        group.addView(tvLabel)
        container.addView(group)
    }

    private data class WeeklyData(val income: Double, val expense: Double, val savings: Double) {
        fun max() = maxOf(income, expense, savings)
    }

    private fun calculateWeeklyData(transactions: List<Transaction>): List<WeeklyData> {
        val result = List(4) { mutableMapOf<TransactionType, Double>() }
        val cal    = Calendar.getInstance()
        transactions.forEach {
            cal.time = it.date
            val week = ((cal.get(Calendar.DAY_OF_MONTH) - 1) / 7).coerceAtMost(3)
            result[week][it.type] = (result[week][it.type] ?: 0.0) + it.amount
        }
        return result.map { m ->
            WeeklyData(
                m[TransactionType.INCOME]  ?: 0.0,
                m[TransactionType.EXPENSE] ?: 0.0,
                m[TransactionType.SAVINGS] ?: 0.0
            )
        }
    }
}
