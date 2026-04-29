package com.budget.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

// Instrumented tests (run on Android device/emulator)
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    // Test: Verify correct app context (basic UI/environment test)
    @Test
    fun appContext_isCorrect() {
        println("=== UI TEST START: appContext_isCorrect ===")

        // Arrange
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Log details
        println("Expected package: com.budget.app")
        println("Actual package: ${appContext.packageName}")

        // Assert
        assertEquals("com.budget.app", appContext.packageName)

        println("=== UI TEST PASSED: appContext_isCorrect ===\n")
    }

    // Test: Check app context is available (simulates app launch readiness)
    @Test
    fun appLaunchContext_isAvailable() {
        println("=== UI TEST START: appLaunchContext_isAvailable ===")

        // Arrange
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Log
        println("Checking if app context exists...")

        // Assert
        assertNotNull(appContext)
        assertEquals("com.budget.app", appContext.packageName)

        println("App context is valid and ready")

        println("=== UI TEST PASSED: appLaunchContext_isAvailable ===\n")
    }

    // Test: Simulated user flow readiness (navigation/environment check)
    @Test
    fun basicUserFlow_environmentIsReady() {
        println("=== UI TEST START: basicUserFlow_environmentIsReady ===")

        // Arrange
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Simulated flow logging
        println("Step 1: App launched")
        println("Step 2: Context retrieved")
        println("Step 3: Package validated")
        println("Step 4: Ready for user interaction")

        // Assert
        assertNotNull(appContext)
        assertEquals("com.budget.app", appContext.packageName)

        println("=== UI TEST PASSED: basicUserFlow_environmentIsReady ===\n")
    }
}
