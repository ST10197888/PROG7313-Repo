package com.budget.app

// Import Android test runner (required for instrumented tests)
import androidx.test.ext.junit.runners.AndroidJUnit4

// Provides access to the app context during testing
import androidx.test.platform.app.InstrumentationRegistry

// Assertion function to compare expected vs actual results
import org.junit.Assert.assertEquals

// Annotation to mark test methods
import org.junit.Test

// Specifies that this test runs with AndroidJUnit4
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    /**
     * This test checks if the app context (package name) is correct.
     * It ensures the app is running under the correct package.
     */
    @Test
    fun appContext_isCorrect() {

        // Logging start of test
        println("Starting appContext_isCorrect test...")

        // Get the application context from the device/emulator
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Log expected and actual values
        println("Expected package: com.budget.app")
        println("Actual package: ${appContext.packageName}")

        // Assertion to verify package name is correct
        assertEquals("com.budget.app", appContext.packageName)

        // Logging success
        println("appContext_isCorrect test passed successfully")
    }

    /**
     * This test ensures that the app context is accessible and the app can run.
     * It simulates a basic launch/environment check.
     */
    @Test
    fun appLaunchContext_isAvailable() {

        // Logging start of test
        println("Starting appLaunchContext_isAvailable test...")

        // Retrieve app context again
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Log confirmation
        println("Application context retrieved successfully")

        // Assertion to confirm app is still under correct package
        assertEquals("com.budget.app", appContext.packageName)

        // Logging success
        println("appLaunchContext_isAvailable test passed successfully")
    }
}