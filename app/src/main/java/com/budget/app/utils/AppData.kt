package com.budget.app.utils

import android.content.Context
import com.budget.app.database.AppDatabase
import com.budget.app.database.BudgetGoalEntity
import com.budget.app.database.DebtEntity
import com.budget.app.database.FinancialGoalEntity
import com.budget.app.database.TransactionEntity
import com.budget.app.database.UserEntity
import com.budget.app.models.*
import java.util.*

object AppData {

    private const val PREFS_NAME = "BudgetAppPrefs"

    private val users = mutableListOf<User>()
    var currentUser: User? = null
    private var nextUserId = 1

    private val transactions = mutableListOf<Transaction>()
    private var nextTxId = 1

    private val budgetGoals = mutableListOf<BudgetGoal>()
    private val financialGoals = mutableListOf<FinancialGoal>()
    private val debts = mutableListOf<Debt>()
    private val achievements = mutableListOf<Achievement>()

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

    val tips = listOf(
        "Talk to Your Parents or Guardians: Know how much support they'll provide before heading out on your own.",
        "Consider Cost When Choosing a School: In-state vs out-of-state and public vs private can drastically change what you pay.",
        "Know Your Financial Need: Your need is calculated as Cost of Attendance minus Expected Family Contribution.",
        "Complete the FAFSA Early: It opens in October — apply as soon as possible as some deadlines are as early as February.",
        "Understand Student Loans: You're borrowing money plus interest — federal loans are safer than private ones.",
        "Know Your Repayment Options: Keep loan payments under 10-15% of your income to avoid falling behind.",
        "Discover Free Money: Grants and scholarships don't need to be repaid — explore every option available.",
        "Apply for Scholarships Smart: Meet deadlines, apply for as many as possible and watch out for scams.",
        "Earn Your Aid: Work-study and ROTC programs can help fund your education in exchange for work or service.",
        "Evaluate Your Aid Options: Only borrow what you need and understand the terms before accepting any aid.",
        "Know What to Do If Something Changes: File a financial aid appeal if your family's situation changes during school.",
        "Start with the Basics: Use free resources like CashCourse to build your money management foundation.",
        "Put Your Money in the Bank: A bank account keeps your money safe, accessible and potentially earning interest.",
        "Use Your Checking Account Responsibly: Monitor transactions, set up alerts and never spend money you don't have.",
        "Get Organized: Keep physical or digital records of all financial documents, bills and contracts.",
        "Watch Out for Identity Fraud: Students are among the least likely to detect fraud — stay alert.",
        "Protect Yourself from Identity Fraud: Shred documents, guard personal info and shop only on secure websites.",
        "Report and Remedy Identity Fraud: Act within 48 hours, alert credit bureaus and file a police report.",
        "Create a Spending Plan: Track every expense for a month then build a plan ranking fixed expenses first.",
        "Develop a Money Management Style: Align your spending with your values and know what drives your financial decisions.",
        "Set SMART Financial Goals: Make goals specific, measurable, achievable, realistic and time-bound.",
        "Spend Your Money Wisely: Make a list before shopping, compare prices and think big purchases through carefully.",
        "Start Saving Today: Treat savings like a bill — pay yourself first and automate it so you never skip.",
        "Limit Transportation Costs: Walk, bike or use public transport when possible; combine errands to save on fuel.",
        "Curb Tech Expenses: Assess what you truly need, compare prices and consider refurbished devices.",
        "Select Housing Mindfully: Compare dorm vs off-campus costs including utilities, deposits and renters insurance.",
        "Get Real with Your Roommates: Set clear expectations about bills, shared items and house rules before signing a lease.",
        "Plug Spending Leaks: Cut costly habits, seek student discounts and avoid unnecessary charges on your student account.",
        "Find the Right Health Care Coverage: Compare your family plan, school plan and marketplace options carefully.",
        "Earn Extra Money with a Job: Look for flexible on-campus or off-campus work that doesn't interfere with your studies.",
        "Know How Credit Cards Work: A credit card is a loan — unpaid balances accrue interest that compounds over time.",
        "Choose Your Credit Card Carefully: Look for no annual fees, low interest rates and a 20-30 day grace period.",
        "Use Your Credit Card Responsibly: Pay your full balance on time every month and never use it for cash advances.",
        "Build a Good Credit History: Pay everything on time and keep your credit usage below 25% of your available limit.",
        "Get Help If You're in Debt Trouble: Contact creditors, seek credit counselling and follow the 20-10 debt rule.",
        "Harness the Power of Compounding Interest: Start saving early — even small amounts grow significantly over time.",
        "Know Where to Invest Your Money: Stocks and mutual funds grow wealth long-term but carry risk — seek advice.",
        "Look for Guidance: Use campus resources including advisors, financial aid offices and legal services when needed.",
        "Choose a Major Wisely: Research employment outlook and earning potential before committing to a field of study.",
        "Make Your Career a Main Focus: Visit the career centre early, take internships and start networking before graduation."
    )

    init {
        register("Seed User", "seed@budget.com", "password123")
        initAchievements()
    }

    private fun initAchievements() {
        achievements.clear()
        achievements.add(Achievement("first_tx", "First Step", "Add your first transaction", 0))
        achievements.add(Achievement("budget_master", "Budget Master", "Set 5 budget goals", 0))
        achievements.add(Achievement("goal_setter", "Goal Setter", "Create your first financial goal", 0))
        achievements.add(Achievement("debt_slayer", "Debt Slayer", "Pay off a debt completely", 0))
        
        // Fitness Score Tiered Achievements
        achievements.add(Achievement("fitness_starter", "Financial Starter", "Reach a fitness score of 50%", 0))
        achievements.add(Achievement("fitness_pro", "Financial Athlete", "Reach a fitness score of 80%", 0))
        achievements.add(Achievement("fitness_elite", "Financial Elite", "Reach a fitness score of 95%", 0))
    }

    fun init(context: Context) {
        val db = AppDatabase.getInstance(context)
        db.userDao().getAll().forEach { entity: UserEntity ->
            val u = entity.toModel()
            if (users.none { user -> user.id == u.id }) users.add(u)
            if (nextUserId <= u.id) nextUserId = u.id + 1
        }
        if (users.none { user -> user.email == "seed@budget.com" }) {
            register("Seed User", "seed@budget.com", "password123")
        }
        if (transactions.isEmpty()) {
            initAchievements()
        } else {
            checkAchievements()
        }
    }

    fun register(name: String, email: String, password: String, context: Context? = null): Boolean {
        if (users.any { it.email.equals(email, ignoreCase = true) }) return false
        val user = User(nextUserId++, name, email, password)
        users.add(user)
        context?.let {
            val db = AppDatabase.getInstance(it)
            db.userDao().insert(UserEntity.fromModel(user))
        }
        return true
    }

    fun login(email: String, password: String, context: Context): Boolean {
        val user = users.find { it.email.equals(email, ignoreCase = true) && it.password == password }
        currentUser = user
        if (user != null) {
            if (user.email == "seed@budget.com" && transactions.isEmpty()) {
                seedDemoData()
            }
        }
        return user != null
    }

    fun logout() { currentUser = null }

    // ── Transactions   
    fun addTransaction(
        title: String, amount: Double, type: TransactionType,
        category: String, notes: String = "", date: Date = Date(),
        attachmentUri: String? = null, attachmentName: String? = null
    ) {
        transactions.add(Transaction(nextTxId++, title, amount, type, category, date, notes, attachmentUri, attachmentName))
        recalcBudgetGoals()
        checkAchievements()
    }

    fun removeTransaction(id: Int) {
        transactions.removeAll { it.id == id }
        recalcBudgetGoals()
        checkAchievements()
    }

    fun getAllTransactions(): List<Transaction> = transactions.sortedByDescending { it.date }
    fun getTransactionsWithAttachments(): List<Transaction> = transactions.filter { it.attachmentUri != null }

    fun getTotalIncome(): Double = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    fun getTotalExpenses(): Double = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    fun getTotalSavings(): Double = transactions.filter { it.type == TransactionType.SAVINGS }.sumOf { it.amount }
    fun getBalance(): Double = getTotalIncome() - getTotalExpenses() - getTotalSavings()

    fun getTransactionsForMonth(month: Int, year: Int): List<Transaction> {
        val cal = Calendar.getInstance()
        return transactions.filter {
            cal.time = it.date
            cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
        }.sortedByDescending { it.date }
    }

    fun getTotalsByCategory(type: TransactionType, month: Int, year: Int): Map<String, Double> {
        return getTransactionsForMonth(month, year)
            .filter { it.type == type }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { t -> t.amount } }
    }

    fun getExpensesByCategory(): Map<String, Double> {
        return transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { t -> t.amount } }
    }

    // Budgeting
    fun getBudgetGoals(): List<BudgetGoal> = budgetGoals.toList()
    fun getBudgetGoalForCategory(category: String): BudgetGoal? = budgetGoals.find { it.category == category }

    fun addOrUpdateBudgetGoal(category: String, limit: Double) {
        val existing = budgetGoals.find { it.category == category }
        if (existing != null) existing.limitAmount = limit
        else budgetGoals.add(BudgetGoal(category, limit))
        recalcBudgetGoals()
        checkAchievements()
    }
    fun removeBudgetGoal(category: String) { budgetGoals.removeAll { it.category == category } }
    private fun recalcBudgetGoals() {
        val byCategory = transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
        budgetGoals.forEach { goal -> goal.spentAmount = byCategory[goal.category] ?: 0.0 }
    }

    fun getUnallocatedIncome(): Double {
        val totalAllocated = budgetGoals.sumOf { it.limitAmount }
        return getTotalIncome() - totalAllocated
    }

    // Goals
    fun getFinancialGoals() = financialGoals.toList()
    fun getFinancialGoalByName(name: String): FinancialGoal? = financialGoals.find { it.name == name }

    fun addFinancialGoal(name: String, target: Double, deadline: Date) {
        financialGoals.add(FinancialGoal(financialGoals.size + 1, name, target, 0.0, deadline))
        checkAchievements()
    }
    fun updateGoalProgress(id: Int, amount: Double) {
        financialGoals.find { it.id == id }?.let { it.currentAmount += amount }
    }

    // Debts
    fun getDebts() = debts.toList()
    fun addDebt(name: String, amount: Double, rate: Double, minPay: Double) {
        debts.add(Debt(debts.size + 1, name, amount, rate, minPay, amount))
    }
    fun recordDebtPayment(id: Int, amount: Double) {
        debts.find { it.id == id }?.let {
            it.remainingAmount -= amount
            if (it.remainingAmount <= 0) {
                it.remainingAmount = 0.0
                checkAchievements()
            }
            // Record payment as a transaction for reporting
            addTransaction("Debt Payment: ${it.name}", amount, TransactionType.EXPENSE, "Debt Payment")
        }
    }

    // Fitness Score
    fun getFinancialFitnessScore(): Double {
        val income = getTotalIncome()
        if (income <= 0) return 0.0
        
        val expenses = getTotalExpenses()
        val savings = getTotalSavings()
        
        // 70% of score comes from Surplus (Income - Expenses)
        val surplusRatio = ((income - expenses) / income).coerceIn(0.0, 1.0)
        val surplusScore = surplusRatio * 70.0
        
        // 30% of score comes from Savings Rate (Target 20% of income)
        val savingsRatio = (savings / income) / 0.20
        val savingsScore = (savingsRatio * 30.0).coerceAtMost(30.0)
        
        return (surplusScore + savingsScore).coerceIn(0.0, 100.0)
    }

    // Gamification
    fun getAchievements() = achievements.toList()
    private fun checkAchievements() {
        if (transactions.isNotEmpty()) unlock("first_tx")
        if (budgetGoals.size >= 5) unlock("budget_master")
        if (financialGoals.isNotEmpty()) unlock("goal_setter")
        if (debts.any { it.remainingAmount <= 0 && it.amount > 0 }) unlock("debt_slayer")
        
        val score = getFinancialFitnessScore()
        if (score >= 50.0) unlock("fitness_starter")
        if (score >= 80.0) unlock("fitness_pro")
        if (score >= 95.0) unlock("fitness_elite")
    }
    private fun unlock(id: String) { achievements.find { it.id == id }?.isUnlocked = true }

    // Persistence 
    fun saveData(context: Context) {
        val user = currentUser ?: return
        val db = com.budget.app.database.AppDatabase.getInstance(context)

        // Save user
        db.userDao().insert(com.budget.app.database.UserEntity.fromModel(user))

        // Save transactions
        transactions.forEach { tx ->
            db.transactionDao().insert(com.budget.app.database.TransactionEntity.fromModel(tx, user.id))
        }

        // Save budget goals
        budgetGoals.forEach { goal ->
            db.budgetGoalDao().insertOrUpdate(com.budget.app.database.BudgetGoalEntity.fromModel(goal, user.id))
        }

        // Save financial goals
        financialGoals.forEach { goal ->
            db.financialGoalDao().insertOrUpdate(com.budget.app.database.FinancialGoalEntity.fromModel(goal, user.id))
        }

        // Save debts
        debts.forEach { debt ->
            db.debtDao().insertOrUpdate(com.budget.app.database.DebtEntity.fromModel(debt, user.id))
        }
    }
    fun loadUserData(context: Context) {
        val user = currentUser ?: return
        val db = com.budget.app.database.AppDatabase.getInstance(context)

        transactions.clear()
        db.transactionDao().getAllForUser(user.id).forEach { transactions.add(it.toModel()) }
        if (nextTxId <= (transactions.maxOfOrNull { it.id } ?: 0)) {
            nextTxId = (transactions.maxOfOrNull { it.id } ?: 0) + 1
        }

        budgetGoals.clear()
        db.budgetGoalDao().getAllForUser(user.id).forEach { budgetGoals.add(it.toModel()) }

        financialGoals.clear()
        db.financialGoalDao().getAllForUser(user.id).forEach { financialGoals.add(it.toModel()) }

        debts.clear()
        db.debtDao().getAllForUser(user.id).forEach { debts.add(it.toModel()) }

        recalcBudgetGoals()
        checkAchievements()
    }

    fun getCategoriesForType(type: TransactionType): List<String> = when (type) {
        TransactionType.EXPENSE -> expenseCategories
        TransactionType.INCOME -> incomeCategories
        TransactionType.SAVINGS -> savingsCategories
    }

    private fun seedDemoData() {
        val cal = Calendar.getInstance()
        val now = cal.time
        
        cal.add(Calendar.MONTH, -1)
        val prev = cal.time

        // 5 INCOME (3 Current, 2 Previous)
        addTransaction("Salary", 25000.0, TransactionType.INCOME, "Salary", date = prev)
        addTransaction("Freelance Project", 5000.0, TransactionType.INCOME, "Freelance / Contract Work", date = prev)
        addTransaction("Salary", 25000.0, TransactionType.INCOME, "Salary", date = now)
        addTransaction("Project Bonus", 3000.0, TransactionType.INCOME, "Bonus", date = now)
        addTransaction("Stock Dividends", 1500.0, TransactionType.INCOME, "Dividends", date = now)

        // 5 EXPENSES (2 Current, 3 Previous)
        addTransaction("Monthly Rent", 8500.0, TransactionType.EXPENSE, "Rent / Mortgage", date = prev)
        addTransaction("Grocery Shopping", 2200.0, TransactionType.EXPENSE, "Food & Groceries", date = prev)
        addTransaction("Internet Bill", 600.0, TransactionType.EXPENSE, "Internet & Phone", date = prev)
        addTransaction("Monthly Rent", 8500.0, TransactionType.EXPENSE, "Rent / Mortgage", date = now)
        addTransaction("Dinner Out", 800.0, TransactionType.EXPENSE, "Eating Out / Restaurants", date = now)

        // 5 SAVINGS (2 Current, 3 Previous)
        addTransaction("Emergency Fund Contribution", 2000.0, TransactionType.SAVINGS, "Emergency Fund", date = prev)
        addTransaction("Retirement Savings", 3000.0, TransactionType.SAVINGS, "Retirement Fund", date = prev)
        addTransaction("Car Fund", 1500.0, TransactionType.SAVINGS, "Car Fund", date = prev)
        addTransaction("Emergency Fund Contribution", 2000.0, TransactionType.SAVINGS, "Emergency Fund", date = now)
        addTransaction("Holiday Savings", 2500.0, TransactionType.SAVINGS, "Holiday / Travel Fund", date = now)

        // 3 DEBT PAYMENTS (As Expense transactions)
        addTransaction("Credit Card Payment", 1200.0, TransactionType.EXPENSE, "Debt Payment", date = prev)
        addTransaction("Personal Loan Payment", 2000.0, TransactionType.EXPENSE, "Debt Payment", date = prev)
        addTransaction("Credit Card Payment", 1500.0, TransactionType.EXPENSE, "Debt Payment", date = now)

        // 4 FINANCIAL GOALS
        addFinancialGoal("Emergency Fund", 50000.0, Calendar.getInstance().apply { add(Calendar.YEAR, 1) }.time)
        addFinancialGoal("Retirement Fund", 1000000.0, Calendar.getInstance().apply { add(Calendar.YEAR, 30) }.time)
        addFinancialGoal("New Car", 150000.0, Calendar.getInstance().apply { add(Calendar.YEAR, 2) }.time)
        addFinancialGoal("Holiday 2024", 15000.0, Calendar.getInstance().apply { add(Calendar.MONTH, 6) }.time)

        // Update progress for some goals
        updateGoalProgress(1, 4000.0) // Emergency Fund
        updateGoalProgress(2, 3000.0) // Retirement
        updateGoalProgress(3, 1500.0) // New Car
        updateGoalProgress(4, 2500.0) // Holiday

        // 1 Budget Goal
        addOrUpdateBudgetGoal("Rent / Mortgage", 9000.0)
    }
}
