package com.budget.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Unit tests for basic business logic (expense calculations)
class ExampleUnitTest {

    // Test: Verify total expense calculation is correct
    @Test
    fun totalExpenses_isCorrect() {
        println("=== TEST START: totalExpenses_isCorrect ===")

        // Arrange: create sample expense list
        val expenses = listOf(100.0, 50.0, 25.0)
        println("Expenses list: $expenses")

        // Act: calculate total
        val total = expenses.sum()
        println("Calculated total: $total")

        // Assert: verify expected result
        assertEquals(175.0, total, 0.0)

        println("=== TEST PASSED: totalExpenses_isCorrect ===\n")
    }

    // Test: Verify adding a new expense works correctly
    @Test
    fun addExpense_isCorrect() {
        println("=== TEST START: addExpense_isCorrect ===")

        // Arrange
        val expenses = mutableListOf(100.0, 50.0)
        println("Initial expenses: $expenses")

        // Act
        expenses.add(25.0)
        println("Updated expenses: $expenses")

        val total = expenses.sum()
        println("New total: $total")

        // Assert
        assertEquals(3, expenses.size)
        assertEquals(175.0, total, 0.0)

        println("=== TEST PASSED: addExpense_isCorrect ===\n")
    }

    // Test: Edge case — empty list should return 0
    @Test
    fun emptyExpenses_returnsZero() {
        println("=== TEST START: emptyExpenses_returnsZero ===")

        // Arrange
        val expenses = emptyList<Double>()
        println("Expenses list is empty")

        // Act
        val total = expenses.sum()
        println("Calculated total: $total")

        // Assert
        assertEquals(0.0, total, 0.0)

        println("=== TEST PASSED: emptyExpenses_returnsZero ===\n")
    }

    // Test: Edge case — negative values handled correctly
    @Test
    fun negativeExpense_isHandledCorrectly() {
        println("=== TEST START: negativeExpense_isHandledCorrectly ===")

        // Arrange
        val expenses = listOf(100.0, -50.0)
        println("Expenses: $expenses")

        // Act
        val total = expenses.sum()
        println("Calculated total: $total")

        // Assert
        assertEquals(50.0, total, 0.0)

        println("=== TEST PASSED: negativeExpense_isHandledCorrectly ===\n")
    }

    // Test: Performance check (simple timing)
    @Test
    fun performance_totalCalculation_isFastEnough() {
        println("=== TEST START: performance_totalCalculation_isFastEnough ===")

        // Arrange
        val expenses = List(1000) { 10.0 }
        println("Generated ${expenses.size} expenses")

        // Act: measure execution time
        val startTime = System.currentTimeMillis()
        val total = expenses.sum()
        val endTime = System.currentTimeMillis()

        val duration = endTime - startTime
        println("Total: $total")
        println("Execution time: $duration ms")

        // Assert
        assertEquals(10000.0, total, 0.0)
        assertTrue("Performance too slow", duration < 1000)

        println("=== TEST PASSED: performance_totalCalculation_isFastEnough ===\n")
    }
}
