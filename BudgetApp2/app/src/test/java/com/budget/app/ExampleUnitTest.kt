package com.budget.app

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {

    // Test total expense calculation
    @Test
    fun totalExpenses_isCorrect() {
        val expenses = listOf(100.0, 50.0, 25.0)

        println("Running totalExpenses_isCorrect test...")
        println("Expenses list: $expenses")

        val total = expenses.sum()

        println("Calculated total: $total")
        println("Expected total: 175.0")

        assertEquals(175.0, total, 0.0)
    }

    @Test
fun appLaunchesCorrectly() {
    // basic launch test
}

@Test
fun emptyExpenses_returnsZero() {
    val expenses = listOf<Double>()
    assertEquals(0.0, expenses.sum(), 0.0)
}
    // Test adding a new expense
    @Test
    fun addExpense_isCorrect() {
        val expenses = mutableListOf(100.0, 50.0)

        println("Running addExpense_isCorrect test...")
        println("Initial expenses: $expenses")

        expenses.add(25.0)

        println("Expenses after adding new value: $expenses")

        val total = expenses.sum()

        println("New total: $total")
        println("Expected size: 3")

        assertEquals(3, expenses.size)
        assertEquals(175.0, total, 0.0)
    }

    // Test empty list (edge case)
    @Test
    fun emptyExpenses_isZero() {
        val expenses = listOf<Double>()

        println("Running emptyExpenses_isZero test...")
        println("Expenses list is empty")

        val total = expenses.sum()

        println("Calculated total: $total")
        println("Expected total: 0.0")

        assertEquals(0.0, total, 0.0)
    }

    // Test negative values
    @Test
    fun negativeExpense_handledCorrectly() {
        val expenses = listOf(100.0, -50.0)

        println("Running negativeExpense_handledCorrectly test...")
        println("Expenses list: $expenses")

        val total = expenses.sum()

        println("Calculated total: $total")
        println("Expected total: 50.0")

        assertEquals(50.0, total, 0.0)
    }
}
