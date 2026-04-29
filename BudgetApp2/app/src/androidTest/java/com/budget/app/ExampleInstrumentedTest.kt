package com.budget.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    /**
     * TEST 1: App launches and key UI elements are visible
     */
    @Test
    fun appLaunchDisplaysMainUI() {

        println("Starting UI test: appLaunchDisplaysMainUI")

        // Check balance text is visible
        onView(withId(R.id.tvBalance))
            .check(matches(isDisplayed()))

        println("Balance view is displayed ✅")

        // Check floating action button exists
        onView(withId(R.id.fabAdd))
            .check(matches(isDisplayed()))

        println("FAB button is visible ✅")
    }

    /**
     * TEST 2: User clicks Add Transaction (REAL USER FLOW)
     */
    @Test
    fun clickAddTransactionCard() {

        println("Starting UI test: clickAddTransactionCard")

        // Click "Add Transaction" card
        onView(withId(R.id.cardActionAdd))
            .perform(click())

        println("Clicked Add Transaction card ✅")

        // (Optional) Add assertion if new screen appears
        // Example:
        // onView(withText("Add Transaction")).check(matches(isDisplayed()))
    }

    /**
     * TEST 3: Floating button interaction
     */
    @Test
    fun clickFloatingAddButton() {

        println("Starting UI test: clickFloatingAddButton")

        onView(withId(R.id.fabAdd))
            .perform(click())

        println("Floating button clicked ✅")
    }
}