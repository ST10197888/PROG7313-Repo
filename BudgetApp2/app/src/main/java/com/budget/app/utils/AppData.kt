package com.budget.app.utils

import com.budget.app.models.BudgetGoal
import com.budget.app.models.Transaction
import com.budget.app.models.TransactionType
import com.budget.app.models.User
import java.util.Calendar
import java.util.Date

object AppData {

    private val users = mutableListOf<User>()
    var currentUser: User? = null
    private var nextUserId = 1

    init {
        // Pre-registered dummy account
        register("Seed User", "seed@budget.com", "password123")
    }

    fun register(name: String, email: String, password: String): Boolean {
        if (users.any { it.email.equals(email, ignoreCase = true) }) return false
        users.add(User(nextUserId++, name, email, password))
        return true
    }

    fun login(email: String, password: String): Boolean {
        val user = users.find { it.email.equals(email, ignoreCase = true) && it.password == password }
        currentUser = user
        if (user != null && user.email == "seed@budget.com") {
            seedDemoData()
        }
        return user != null
    }

    fun logout() { currentUser = null }

    // ── Transactions ──────────────────────────────────────────────────────────
    private val transactions = mutableListOf<Transaction>()
    private var nextTxId = 1

    fun addTransaction(
        title: String, amount: Double, type: TransactionType,
        category: String, notes: String = "", date: Date = Date(),
        attachmentUri: String? = null, attachmentName: String? = null
    ) {
        transactions.add(Transaction(nextTxId++, title, amount, type, category, date, notes, attachmentUri, attachmentName))
        recalcBudgetGoals()
    }

    fun removeTransaction(id: Int) {
        transactions.removeAll { it.id == id }
        recalcBudgetGoals()
    }

    fun getAllTransactions(): List<Transaction> = transactions.sortedByDescending { it.date }

    fun getIncomeTransactions(): List<Transaction> = transactions.filter { it.type == TransactionType.INCOME }

    fun getExpenseTransactions(): List<Transaction> = transactions.filter { it.type == TransactionType.EXPENSE }

    fun getSavingsTransactions(): List<Transaction> = transactions.filter { it.type == TransactionType.SAVINGS }

    fun getTotalIncome(): Double = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

    fun getTotalExpenses(): Double = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    fun getTotalSavings(): Double = transactions.filter { it.type == TransactionType.SAVINGS }.sumOf { it.amount }

    fun getBalance(): Double = getTotalIncome() - getTotalExpenses() - getTotalSavings()

    fun getExpensesByCategory(): Map<String, Double> =
        transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

    fun getTransactionsForMonth(month: Int, year: Int): List<Transaction> {
        val cal = Calendar.getInstance()
        return transactions.filter {
            cal.time = it.date
            cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
        }.sortedByDescending { it.date }
    }

    fun getSpendingByDayForMonth(month: Int, year: Int): Map<Int, Double> {
        val cal = Calendar.getInstance()
        return transactions.filter {
            it.type == TransactionType.EXPENSE &&
            run {
                cal.time = it.date
                cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
            }
        }.groupBy {
            cal.time = it.date
            cal.get(Calendar.DAY_OF_MONTH)
        }.mapValues { (_, list) -> list.sumOf { it.amount } }
    }
    
    fun getTransactionsWithAttachments(): List<Transaction> = 
        transactions.filter { it.attachmentUri != null }.sortedByDescending { it.date }

    // ── Budget Goals ──────────────────────────────────────────────────────────
    private val budgetGoals = mutableListOf<BudgetGoal>()

    fun getBudgetGoals(): List<BudgetGoal> = budgetGoals.toList()

    fun addOrUpdateBudgetGoal(category: String, limit: Double) {
        val existing = budgetGoals.find { it.category == category }
        if (existing != null) existing.limitAmount = limit
        else budgetGoals.add(BudgetGoal(category, limit))
        recalcBudgetGoals()
    }

    fun removeBudgetGoal(category: String) {
        budgetGoals.removeAll { it.category == category }
    }

    private fun recalcBudgetGoals() {
        val byCategory = getExpensesByCategory()
        budgetGoals.forEach { goal ->
            goal.spentAmount = byCategory[goal.category] ?: 0.0
        }
    }

    // ── Categories ────────────────────────────────────────────────────────────
    val expenseCategories = listOf(
        "Food & Groceries", "Transport", "Rent / Mortgage", "Electricity & Water",
        "Internet & Phone", "Entertainment", "Clothing", "Healthcare / Medical",
        "Education", "Insurance", "Eating Out / Restaurants", "Personal Care",
        "Subscriptions (Netflix, Spotify etc.)", "Household Supplies", "Petrol / Fuel", "Other"
    )

    val incomeCategories = listOf(
        "Salary", "Freelance / Contract Work", "Business Revenue", "Gift / Money Received",
        "Tax Refund", "Investment Returns", "Rental Income", "Commission", "Bonus",
        "Side Hustle", "Pension / Annuity", "Government Grant", "Cashback / Rewards",
        "Interest Earned", "Dividends", "Other"
    )

    val savingsCategories = listOf(
        "Emergency Fund", "Holiday / Travel Fund", "Retirement Fund", "Home Deposit",
        "Car Fund", "Education Fund", "Investment Account", "Business Fund",
        "Wedding Fund", "Medical Aid Reserve", "Tech / Electronics Fund", "Gift Fund",
        "Insurance Reserve", "Children's Fund", "General Savings", "Other"
    )

    fun getCategoriesForType(type: TransactionType): List<String> {
        return when (type) {
            TransactionType.EXPENSE -> expenseCategories
            TransactionType.INCOME -> incomeCategories
            TransactionType.SAVINGS -> savingsCategories
        }
    }

    private fun seedDemoData() {
        if (transactions.isNotEmpty()) return
        val cal = Calendar.getInstance()
        val now = cal.time

        // Income
        addTransaction("Monthly Salary", 25000.0, TransactionType.INCOME, "Salary", date = now)
        addTransaction("Freelance Project", 5000.0, TransactionType.INCOME, "Freelance / Contract Work", date = now)
        addTransaction("Tax Refund", 1200.0, TransactionType.INCOME, "Tax Refund", date = now)
        
        // Savings
        addTransaction("Emergency Savings", 2000.0, TransactionType.SAVINGS, "Emergency Fund", date = now)
        addTransaction("Vacation Fund", 1000.0, TransactionType.SAVINGS, "Holiday / Travel Fund", date = now)
        addTransaction("Home Deposit Fund", 3000.0, TransactionType.SAVINGS, "Home Deposit", date = now)

        // Expenses
        addTransaction("House Rent", 8500.0, TransactionType.EXPENSE, "Rent / Mortgage", date = now)
        addTransaction("Grocery Shopping", 2200.0, TransactionType.EXPENSE, "Food & Groceries", date = now)
        addTransaction("Petrol Fill-up", 1500.0, TransactionType.EXPENSE, "Petrol / Fuel", date = now)
        addTransaction("Dinner Night", 800.0, TransactionType.EXPENSE, "Eating Out / Restaurants", date = now)
        addTransaction("Fiber Internet", 999.0, TransactionType.EXPENSE, "Internet & Phone", date = now)
        addTransaction("Netflix & Spotify", 299.0, TransactionType.EXPENSE, "Subscriptions (Netflix, Spotify etc.)", date = now)
        addTransaction("Electricity Bill", 1200.0, TransactionType.EXPENSE, "Electricity & Water", date = now)
        addTransaction("Movie Tickets", 400.0, TransactionType.EXPENSE, "Entertainment", date = now)
        
        // Add some transactions for previous days
        for (i in 1..5) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_MONTH, -i)
            addTransaction("Groceries $i", 150.0 * i, TransactionType.EXPENSE, "Food & Groceries", date = c.time)
            addTransaction("Uber $i", 50.0 * i, TransactionType.EXPENSE, "Transport", date = c.time)
        }
        
        addOrUpdateBudgetGoal("Food & Groceries", 5000.0)
        addOrUpdateBudgetGoal("Transport", 2500.0)
        addOrUpdateBudgetGoal("Eating Out / Restaurants", 2000.0)
        addOrUpdateBudgetGoal("Rent / Mortgage", 9000.0)
    }
}
