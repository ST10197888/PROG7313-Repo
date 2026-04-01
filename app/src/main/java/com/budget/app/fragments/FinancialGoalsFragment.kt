package com.budget.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.budget.app.R
import com.budget.app.adapters.FinancialGoalAdapter
import com.budget.app.utils.AppData
import java.util.Date

class FinancialGoalsFragment : Fragment() {

    private lateinit var adapter: FinancialGoalAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_goals, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<EditText>(R.id.etGoalName)
        val etTarget = view.findViewById<EditText>(R.id.etGoalTarget)
        val btnAdd = view.findViewById<Button>(R.id.btnAddGoal)
        val rv = view.findViewById<RecyclerView>(R.id.rvGoals)

        adapter = FinancialGoalAdapter(AppData.getFinancialGoals()) { goal ->
            showAddProgressDialog(goal.id)
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val targetStr = etTarget.text.toString().trim()

            if (name.isNotEmpty() && targetStr.isNotEmpty()) {
                val target = targetStr.toDoubleOrNull() ?: 0.0
                if (target > 0) {
                    AppData.addFinancialGoal(name, target, Date()) // Simplified deadline
                    etName.text.clear()
                    etTarget.text.clear()
                    refreshList()
                }
            }
        }
    }

    private fun showAddProgressDialog(goalId: Int) {
        val input = EditText(requireContext())
        input.hint = "Amount to add"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Progress")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    AppData.updateGoalProgress(goalId, amount)
                    refreshList()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun refreshList() {
        adapter.updateData(AppData.getFinancialGoals())
    }
}
