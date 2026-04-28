package com.budget.app.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.budget.app.R
import com.budget.app.models.TransactionType
import com.budget.app.utils.AppData
import com.budget.app.utils.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportsFragment : Fragment() {

    private var selectedMonth = 0
    private var selectedYear  = 0
    private lateinit var tvMonthLabel    : TextView
    private lateinit var layoutGraphs    : LinearLayout
    private lateinit var layoutBreakdown : LinearLayout
    private lateinit var tvReportIncome  : TextView
    private lateinit var tvReportExpenses: TextView
    private lateinit var tvReportSavings : TextView
    private lateinit var tvSavingsRate   : TextView
    private lateinit var pbSavingsRate   : ProgressBar
    private lateinit var layoutBarChart  : LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_reports, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvReportIncome   = view.findViewById(R.id.tvReportIncome)
        tvReportExpenses = view.findViewById(R.id.tvReportExpenses)
        tvReportSavings  = view.findViewById(R.id.tvReportSavings)
        tvSavingsRate    = view.findViewById(R.id.tvSavingsRate)
        pbSavingsRate    = view.findViewById(R.id.pbSavingsRate)
        layoutBreakdown  = view.findViewById(R.id.layoutCategoryBreakdown)
        tvMonthLabel     = view.findViewById(R.id.tvMonthLabel)
        layoutGraphs     = view.findViewById(R.id.layoutSkylineGraphs)
        layoutBarChart   = view.findViewById(R.id.layoutBarChart)

        val btnPrev = view.findViewById<TextView>(R.id.btnPrevMonth)
        val btnNext = view.findViewById<TextView>(R.id.btnNextMonth)

        val now = Calendar.getInstance()
        selectedMonth = now.get(Calendar.MONTH)
        selectedYear  = now.get(Calendar.YEAR)

        btnPrev.setOnClickListener {
            if (selectedMonth == 0) { selectedMonth = 11; selectedYear-- }
            else selectedMonth--
            refresh()
        }

        btnNext.setOnClickListener {
            if (selectedMonth == 11) { selectedMonth = 0; selectedYear++ }
            else selectedMonth++
            refresh()
        }

        refresh()
    }

    private fun refresh() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, selectedMonth)
        cal.set(Calendar.YEAR, selectedYear)
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
        tvMonthLabel.text = monthName

        val transactions = AppData.getTransactionsForMonth(selectedMonth, selectedYear)

        val income   = transactions.filter { it.type == TransactionType.INCOME  }.sumOf { it.amount }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val savings  = transactions.filter { it.type == TransactionType.SAVINGS }.sumOf { it.amount }
        val rate     = if (income > 0) ((savings / income) * 100).toInt().coerceIn(0, 100) else 0

        tvReportIncome.text   = CurrencyFormatter.format(income)
        tvReportExpenses.text = CurrencyFormatter.format(expenses)
        tvReportSavings.text  = CurrencyFormatter.format(savings)
        tvSavingsRate.text    = "Savings Rate: $rate%"
        pbSavingsRate.progress = rate

        setupWeeklyTracker(layoutBarChart)
        buildVisualDailyGraphs(selectedMonth, selectedYear)
        setupCategoryBreakdown(expenses)
    }

    private fun setupWeeklyTracker(container: LinearLayout) {
        container.removeAllViews()
        
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        
        cal.add(Calendar.MONTH, -1)
        val lastMonth = cal.get(Calendar.MONTH)
        val lastYear = cal.get(Calendar.YEAR)
        
        val lmWeekly = calculateWeeklyData(AppData.getTransactionsForMonth(lastMonth, lastYear))
        val cmWeekly = calculateWeeklyData(AppData.getTransactionsForMonth(currentMonth, currentYear))
        
        val maxVal = (lmWeekly + cmWeekly).maxOfOrNull { it.max() }?.coerceAtLeast(1.0) ?: 1.0

        lmWeekly.forEachIndexed { i, data -> addWeeklyGroup(container, data, "LM W${i+1}", maxVal) }
        cmWeekly.forEachIndexed { i, data -> addWeeklyGroup(container, data, "CM W${i+1}", maxVal) }
    }

    private data class WeeklyData(val income: Double, val expense: Double, val savings: Double) {
        fun max() = maxOf(income, maxOf(expense, savings))
    }

    private fun calculateWeeklyData(transactions: List<com.budget.app.models.Transaction>): List<WeeklyData> {
        val result = List(4) { mutableMapOf<TransactionType, Double>() }
        val cal = Calendar.getInstance()
        transactions.forEach {
            cal.time = it.date
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val weekIndex = ((day - 1) / 7).coerceAtMost(3)
            val current = result[weekIndex].getOrDefault(it.type, 0.0)
            result[weekIndex][it.type] = current + it.amount
        }
        return result.map { map ->
            WeeklyData(
                map[TransactionType.INCOME] ?: 0.0,
                map[TransactionType.EXPENSE] ?: 0.0,
                map[TransactionType.SAVINGS] ?: 0.0
            )
        }
    }

    private fun addWeeklyGroup(container: LinearLayout, data: WeeklyData, label: String, maxVal: Double) {
        val groupLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.BOTTOM
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        }

        val barsLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.BOTTOM
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            setPadding(2, 0, 2, 0)
        }

        fun createBar(valAmount: Double, colorRes: Int): View {
            val barHeight = (valAmount / maxVal * 120).toInt().coerceAtLeast(0)
            return View(requireContext()).apply {
                val barHeightPx = (barHeight * resources.displayMetrics.density).toInt().coerceAtLeast(2)
                layoutParams = LinearLayout.LayoutParams(0, barHeightPx, 1f).apply { setMargins(1, 0, 1, 0) }
                setBackgroundColor(requireContext().getColor(colorRes))
            }
        }

        barsLayout.addView(createBar(data.income, R.color.income_green))
        barsLayout.addView(createBar(data.expense, R.color.expense_red))
        barsLayout.addView(createBar(data.savings, R.color.colorPrimary))

        val labelTv = TextView(requireContext()).apply {
            text = label
            textSize = 6f
            gravity = android.view.Gravity.CENTER
        }

        groupLayout.addView(barsLayout)
        groupLayout.addView(labelTv)
        container.addView(groupLayout)
    }

    private fun buildVisualDailyGraphs(month: Int, year: Int) {
        layoutGraphs.removeAllViews()

        val cal = Calendar.getInstance().apply { set(Calendar.MONTH, month); set(Calendar.YEAR, year) }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val transactions = AppData.getTransactionsForMonth(month, year)

        fun getDailyMap(type: TransactionType): Map<Int, Double> {
            return transactions.filter { it.type == type }
                .groupBy { 
                    val c = Calendar.getInstance().apply { time = it.date }
                    c.get(Calendar.DAY_OF_MONTH)
                }
                .mapValues { it.value.sumOf { t -> t.amount } }
        }

        val incomeData = getDailyMap(TransactionType.INCOME)
        val expenseData = getDailyMap(TransactionType.EXPENSE)
        val savingsData = getDailyMap(TransactionType.SAVINGS)

        addDailyVisualChart("Monthly Income Trends", incomeData, daysInMonth, R.color.income_green)
        addDailyVisualChart("Monthly Expense Trends", expenseData, daysInMonth, R.color.expense_red)
        addDailyVisualChart("Monthly Savings Trends", savingsData, daysInMonth, R.color.colorPrimary)
    }

    private fun addDailyVisualChart(title: String, data: Map<Int, Double>, days: Int, colorRes: Int) {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 32.dpToPx())
            }
        }

        val titleTv = TextView(requireContext()).apply {
            text = title
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(requireContext().getColor(R.color.text_primary))
            setPadding(0, 0, 0, 16.dpToPx())
        }
        container.addView(titleTv)

        val hsv = HorizontalScrollView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 180.dpToPx())
            isHorizontalScrollBarEnabled = false
        }

        val chartLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.BOTTOM
            setPadding(0, 8.dpToPx(), 0, 8.dpToPx())
        }

        val maxVal = data.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

        for (day in 1..days) {
            val amount = data[day] ?: 0.0
            val barHeight = (amount / maxVal * 140).toInt()
            
            val barGroup = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.BOTTOM
                layoutParams = LinearLayout.LayoutParams(36.dpToPx(), LinearLayout.LayoutParams.MATCH_PARENT)
            }

            val bar = View(requireContext()).apply {
                val heightPx = (barHeight * resources.displayMetrics.density).toInt().coerceAtLeast(2)
                layoutParams = LinearLayout.LayoutParams(20.dpToPx(), heightPx).apply {
                    setMargins(8.dpToPx(), 0, 8.dpToPx(), 0)
                }
                setBackgroundColor(requireContext().getColor(colorRes))
            }

            val label = TextView(requireContext()).apply {
                text = day.toString()
                textSize = 8f
                gravity = android.view.Gravity.CENTER
                setTextColor(requireContext().getColor(R.color.text_secondary))
            }

            barGroup.addView(bar)
            barGroup.addView(label)
            chartLayout.addView(barGroup)
        }

        hsv.addView(chartLayout)
        container.addView(hsv)
        layoutGraphs.addView(container)
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun setupCategoryBreakdown(totalExpenses: Double) {
        layoutBreakdown.removeAllViews()
        val byCategory = AppData.getExpensesByCategory()
        val totalExp   = if (totalExpenses > 0) totalExpenses else 1.0

        if (byCategory.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "No expense data yet."
                setTextColor(Color.GRAY)
            }
            layoutBreakdown.addView(tv)
            return
        }

        val colors = listOf("#F44336","#2196F3","#4CAF50","#FF9800","#9C27B0","#00BCD4","#FF5722","#607D8B")

        byCategory.entries.sortedByDescending { it.value }.forEachIndexed { i, entry ->
            val percent = ((entry.value / totalExp) * 100).toInt()
            val row     = layoutInflater.inflate(R.layout.item_category_bar, layoutBreakdown, false)
            row.findViewById<TextView>(R.id.tvBarCategory).text = entry.key
            row.findViewById<TextView>(R.id.tvBarAmount).text   = CurrencyFormatter.format(entry.value)
            row.findViewById<TextView>(R.id.tvBarPercent).text  = "$percent%"
            val bar = row.findViewById<ProgressBar>(R.id.pbCategory)
            bar.progress = percent
            bar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(colors[i % colors.size]))
            layoutBreakdown.addView(row)
        }
    }
}
