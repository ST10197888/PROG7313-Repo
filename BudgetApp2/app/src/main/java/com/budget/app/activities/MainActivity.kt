package com.budget.app.activities

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.budget.app.R
import com.budget.app.fragments.*
import com.budget.app.utils.AppData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Load Dark Mode Preference BEFORE super.onCreate
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        bottomNav = findViewById(R.id.bottomNavigationView)

        // Update Nav Header
        val headerView = navigationView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.tvNavEmail).text = AppData.currentUser?.email ?: ""

        // Show dashboard on start
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_dashboard    -> DashboardFragment()
                R.id.nav_add          -> AddTransactionFragment()
                R.id.nav_profile      -> ProfileFragment()
                else -> DashboardFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment: Fragment = when (item.itemId) {
            R.id.nav_dashboard    -> { bottomNav.selectedItemId = R.id.nav_dashboard; DashboardFragment() }
            R.id.nav_transactions -> TransactionsFragment()
            R.id.nav_add          -> { bottomNav.selectedItemId = R.id.nav_add; AddTransactionFragment() }
            R.id.nav_attachments  -> AttachmentsFragment()
            R.id.nav_reports      -> ReportsFragment()
            R.id.nav_categories   -> CategoriesFragment()
            R.id.nav_budget_goals -> BudgetGoalsFragment()
            R.id.nav_profile      -> { bottomNav.selectedItemId = R.id.nav_profile; ProfileFragment() }
            R.id.nav_settings     -> SettingsFragment()
            else -> DashboardFragment()
        }
        
        loadFragment(fragment)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // Allow fragments to switch tabs programmatically
    fun navigateTo(itemId: Int) {
        bottomNav.selectedItemId = itemId
    }
}
