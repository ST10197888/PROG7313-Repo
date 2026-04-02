package com.budget.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.budget.app.R
import com.budget.app.adapters.DebtAdapter
import com.budget.app.utils.AppData

class DebtReductionFragment : Fragment() {

    private lateinit var adapter: DebtAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_debts, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<EditText>(R.id.etDebtName)
        val etAmount = view.findViewById<EditText>(R.id.etDebtAmount)
        val etRate = view.findViewById<EditText>(R.id.etDebtRate)
        val etMinPay = view.findViewById<EditText>(R.id.etDebtMinPay)
        val btnAdd = view.findViewById<Button>(R.id.btnAddDebt)
        val rv = view.findViewById<RecyclerView>(R.id.rvDebts)

        adapter = DebtAdapter(AppData.getDebts()) { debt ->
            showPaymentDialog(debt.id)
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val rate = etRate.text.toString().toDoubleOrNull() ?: 0.0
            val minPay = etMinPay.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isNotEmpty() && amount > 0) {
                AppData.addDebt(name, amount, rate, minPay)
                etName.text.clear()
                etAmount.text.clear()
                etRate.text.clear()
                etMinPay.text.clear()
                refreshList()
            }
        }
    }

    private fun showPaymentDialog(debtId: Int) {
        val input = EditText(requireContext())
        input.hint = "Payment Amount"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Record Payment")
            .setView(input)
            .setPositiveButton("Pay") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    AppData.recordDebtPayment(debtId, amount)
                    refreshList()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun refreshList() {
        adapter.updateData(AppData.getDebts())
    }
}
