package com.budget.app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.budget.app.R
import com.budget.app.activities.MainActivity
import com.budget.app.adapters.FinancialGoalAdapter
import com.budget.app.models.FinancialGoal
import com.budget.app.utils.AppData
import java.util.Date
import java.util.Locale

class FinancialGoalsFragment : Fragment(), MainActivity.OnBackPressedListener {

    private lateinit var adapter: FinancialGoalAdapter
    private lateinit var rv: RecyclerView

    companion object {
        private const val TAG = "FinancialGoalsFrag"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_goals, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (AppData.currentUser == null) {
            Log.e(TAG, "No current user found. Aborting setup.")
            return
        }

        val etName   = view.findViewById<EditText>(R.id.etGoalName)
        val etTarget = view.findViewById<EditText>(R.id.etGoalTarget)
        val btnAdd   = view.findViewById<Button>(R.id.btnAddGoal)
        rv = view.findViewById(R.id.rvGoals)

        adapter = FinancialGoalAdapter(emptyList()) { goal ->
            showAddProgressDialog(goal)
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        refreshList()

        btnAdd.setOnClickListener {
            val name      = etName.text.toString().trim()
            val targetStr = etTarget.text.toString().trim()

            if (name.isNotEmpty() && targetStr.isNotEmpty()) {
                val target = targetStr.toDoubleOrNull() ?: 0.0
                if (target > 0) {
                    Log.d(TAG, "Creating new goal: $name with target $target")
                    AppData.addFinancialGoal(requireContext(), name, target, Date())
                    etName.text.clear()
                    etTarget.text.clear()
                    refreshList()
                } else {
                    Log.w(TAG, "User input target was non-positive: $target")
                }
            } else {
                Log.w(TAG, "Validation failed: Name or target is empty")
            }
        }
    }

    override fun onBackPressed(): Boolean {
        if (::rv.isInitialized && rv.computeVerticalScrollOffset() > 0) {
            Log.d(TAG, "Back pressed: scrolling RecyclerView to top")
            rv.smoothScrollToPosition(0)
            return true
        }
        return false
    }

    private fun showAddProgressDialog(goal: FinancialGoal) {
        val remaining = goal.targetAmount - goal.currentAmount

        if (remaining <= 0) {
            Log.i(TAG, "User clicked completed goal: ${goal.name}")
            Toast.makeText(requireContext(), "Goal already reached!", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(requireContext())
        val remainingStr = String.format(Locale.getDefault(), "%.2f", remaining)
        input.hint = "Amount to add (max $remainingStr)"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Progress to ${goal.name}")
            .setView(input)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val amount = input.text.toString().toDoubleOrNull()
                Log.d(TAG, "Input progress amount: $amount for goal ID: ${goal.id}")

                when {
                    amount == null || amount <= 0 -> input.error = "Enter a valid amount"
                    amount > remaining -> {
                        val limit = String.format(Locale.getDefault(), "%.2f", remaining)
                        input.error = "Cannot exceed remaining amount ($limit)"
                    }
                    else -> {
                        AppData.updateGoalProgress(requireContext(), goal.id, amount)
                        Log.d(TAG, "Progress updated successfully.")
                        refreshList()
                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun refreshList() {
        Log.d(TAG, "Refreshing goal list")
        val goals = AppData.getFinancialGoals()
        adapter.updateData(goals)
        Log.d(TAG, "UI updated with ${goals.size} goals.")
    }
}