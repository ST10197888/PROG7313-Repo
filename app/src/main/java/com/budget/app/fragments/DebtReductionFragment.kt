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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.budget.app.R
import com.budget.app.activities.MainActivity
import com.budget.app.adapters.DebtAdapter
import com.budget.app.database.AppDatabase
import com.budget.app.database.DebtEntity
import com.budget.app.models.Debt
import com.budget.app.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DebtReductionFragment : Fragment(), MainActivity.OnBackPressedListener {

    private lateinit var adapter: DebtAdapter
    private lateinit var rv: RecyclerView

    private val db by lazy { AppDatabase.getInstance(requireContext()) }
    private val currentUserId by lazy { SessionManager.getUserId(requireContext()) }

    companion object {
        private const val TAG = "DebtReductionFrag"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_debts, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Safety check to ensure we have a session before doing anything
        if (currentUserId == -1) {
            Log.e(TAG, "Invalid session: currentUserId is -1. Aborting setup.")
            return
        }

        val etName = view.findViewById<EditText>(R.id.etDebtName)
        val etAmount = view.findViewById<EditText>(R.id.etDebtAmount)
        val etRate = view.findViewById<EditText>(R.id.etDebtRate)
        val etMinPay = view.findViewById<EditText>(R.id.etDebtMinPay)
        val btnAdd = view.findViewById<Button>(R.id.btnAddDebt)
        rv = view.findViewById(R.id.rvDebts)

        adapter = DebtAdapter(emptyList()) { debt ->
            showPaymentDialog(debt)
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        refreshList()

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val rate = etRate.text.toString().toDoubleOrNull() ?: 0.0
            val minPay = etMinPay.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isNotEmpty() && amount > 0) {
                Log.d(TAG, "Attempting to add new debt: $name, Amount: $amount")

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val newDebt = DebtEntity(
                            userId = currentUserId,
                            name = name,
                            amount = amount,
                            interestRate = rate,
                            minPayment = minPay,
                            remainingAmount = amount
                        )
                        db.debtDao().insertOrUpdate(newDebt)
                        Log.d(TAG, "Successfully inserted/updated debt in Room DB")

                        withContext(Dispatchers.Main) {
                            etName.text.clear()
                            etAmount.text.clear()
                            etRate.text.clear()
                            etMinPay.text.clear()
                            refreshList()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving debt to database", e)
                    }
                }
            } else {
                Log.w(TAG, "User attempted to save invalid debt input")
                Toast.makeText(requireContext(), "Please enter a valid name and amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        // Scroll to top if the user is deep in the list instead of exiting immediately
        if (::rv.isInitialized && rv.computeVerticalScrollOffset() > 0) {
            Log.d(TAG, "Back pressed: scrolling to top")
            rv.smoothScrollToPosition(0)
            return true
        }
        return false
    }

    private fun showPaymentDialog(debt: Debt) {
        // Prevent dialog from even opening if the debt is already fully paid
        if (debt.remainingAmount <= 0) {
            Log.i(TAG, "User clicked fully paid debt: ${debt.name}")
            Toast.makeText(requireContext(), "Debt already fully paid!", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(requireContext())
        input.hint = "Payment Amount (min ${String.format("%.2f", debt.minPayment)})"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Record Payment for ${debt.name}")
            .setView(input)
            .setPositiveButton("Pay", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val payment = input.text.toString().toDoubleOrNull()
                Log.d(TAG, "Processing payment input: $payment for debt ID: ${debt.id}")

                when {
                    payment == null || payment <= 0 -> {
                        input.error = "Enter a valid payment amount"
                    }
                    debt.minPayment > 0 && payment < debt.minPayment -> {
                        input.error = "Payment must be at least ${String.format("%.2f", debt.minPayment)}"
                    }
                    payment > debt.remainingAmount -> {
                        input.error = "Payment exceeds remaining balance (${String.format("%.2f", debt.remainingAmount)})"
                    }
                    else -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val newRemaining = (debt.remainingAmount - payment).coerceAtLeast(0.0)
                                db.debtDao().updateRemaining(debt.id, newRemaining)
                                Log.d(TAG, "Payment of $payment applied. New balance: $newRemaining")

                                withContext(Dispatchers.Main) {
                                    refreshList()
                                    dialog.dismiss()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Database error applying payment", e)
                            }
                        }
                    }
                }
            }
        }

        dialog.show()
    }

    private fun refreshList() {
        Log.d(TAG, "Refreshing debt list from database")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val data = db.debtDao().getAllForUser(currentUserId)
                val debts: List<Debt> = data.map { it.toModel() }

                withContext(Dispatchers.Main) {
                    adapter.updateData(debts)
                    Log.d(TAG, "RecyclerView updated with ${debts.size} items")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching debts", e)
            }
        }
    }
}
