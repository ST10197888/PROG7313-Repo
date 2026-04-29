package com.budget.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun totalExpenses_isCorrect() {
        println("Starting totalExpenses_isCorrect test...")

        val expenses = listOf(100.0, 50.0, 25.0)
        println("Expenses: $expenses")

        val total = expenses.sum()
        println("Calculated total: $total")
        println("Expected total: 175.0")

        assertEquals(175.0, total, 0.0)

        println("totalExpenses_isCorrect passed")
    }

    @Test
    fun addExpense_isCorrect() {
        println("Starting addExpense_isCorrect test...")

        val expenses = mutableListOf(100.0, 50.0)
        println("Initial expenses: $expenses")

        expenses.add(25.0)
        println("Expenses after adding new expense: $expenses")

        val total = expenses.sum()
        println("New total: $total")

        assertEquals(3, expenses.size)
        assertEquals(175.0, total, 0.0)

        println("addExpense_isCorrect passed")
    }

    @Test
    fun emptyExpenses_returnsZero() {
        println("Starting emptyExpenses_returnsZero test...")

        val expenses = emptyList<Double>()
        println("Expenses list is empty")

        val total = expenses.sum()
        println("Calculated total: $total")

        assertEquals(0.0, total, 0.0)

        println("emptyExpenses_returnsZero passed")
    }

    @Test
    fun negativeExpense_isHandledCorrectly() {
        println("Starting negativeExpense_isHandledCorrectly test...")

        val expenses = listOf(100.0, -50.0)
        println("Expenses with negative value: $expenses")

        val total = expenses.sum()
        println("Calculated total: $total")

        assertEquals(50.0, total, 0.0)

        println("negativeExpense_isHandledCorrectly passed")
    }

    @Test
    fun performance_totalCalculation_isFastEnough() {
        println("Starting performance_totalCalculation_isFastEnough test...")

        val expenses = List(1000) { 10.0 }
        println("Created ${expenses.size} expenses for performance test")

        val startTime = System.currentTimeMillis()

        val total = expenses.sum()

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        println("Total calculated: $total")
        println("Execution time: $duration ms")

        assertEquals(10000.0, total, 0.0)
        assertTrue("Calculation took too long", duration < 1000)

        println("performance_totalCalculation_isFastEnough passed")
    }
}
