package com.budget.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit // Necessary import for KTX extension

object SessionManager {
    private const val PREF_NAME = "BudgetAppSession"
    private const val KEY_USER_ID = "current_user_id"

    // Tag for filtering in Logcat
    private const val TAG = "SessionManager"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setUserId(context: Context, userId: Int) {
        Log.d(TAG, "Setting session for User ID: $userId")

        // Using KTX edit extension to avoid manual .apply() or .commit() calls
        getPreferences(context).edit {
            putInt(KEY_USER_ID, userId)
        }
    }

    fun getUserId(context: Context): Int {
        val id = getPreferences(context).getInt(KEY_USER_ID, -1)

        // Log a warning if we are accessing the ID but no one is logged in
        if (id == -1) {
            Log.w(TAG, "getUserId called but no active session found (returning -1)")
        }

        return id
    }

    /**
     * Helper to wipe the session during logout.
     * Suppressing "unused" because this is reserved for future logout implementation.
     */
    @Suppress("unused")
    fun clearSession(context: Context) {
        Log.i(TAG, "Clearing user session/Logging out")
        getPreferences(context).edit {
            clear()
        }
    }
}