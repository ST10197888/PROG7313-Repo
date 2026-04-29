package com.budget.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    // Test a simple expense calculation
    @Test
    fun totalExpenses_isCorrect() {
        val expenses = listOf(100.0, 50.0, 25.0)
        val total = expenses.sum()

        assertEquals(175.0, total, 0.0)
    }

    // Test adding a new expense
    @Test
    fun addExpense_isCorrect() {
        val expenses = mutableListOf(100.0, 50.0)
        expenses.add(25.0)

        assertEquals(3, expenses.size)
        assertEquals(175.0, expenses.sum(), 0.0)
    }
}