package com.budget.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.budget.app.R
import com.budget.app.models.Debt
import com.budget.app.utils.CurrencyFormatter

class DebtAdapter(
    private var debts: List<Debt>,
    private val onPaymentClick: (Debt) -> Unit
) : RecyclerView.Adapter<DebtAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDebtName)
        val tvDetails: TextView = view.findViewById(R.id.tvDebtDetails)
        val tvRemaining: TextView = view.findViewById(R.id.tvDebtRemaining)
        val pbDebt: ProgressBar = view.findViewById(R.id.pbDebt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_debt, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val debt = debts[position]
        holder.tvName.text = debt.name
        holder.tvDetails.text = "Interest: ${debt.interestRate}% | Min Pay: ${CurrencyFormatter.format(debt.minPayment)}"
        holder.tvRemaining.text = "Remaining: ${CurrencyFormatter.format(debt.remainingAmount)}"
        
        val progress = if (debt.amount > 0) ((1 - (debt.remainingAmount / debt.amount)) * 100).toInt() else 100
        holder.pbDebt.progress = progress

        holder.itemView.setOnClickListener { onPaymentClick(debt) }
    }

    override fun getItemCount() = debts.size

    fun updateData(newDebts: List<Debt>) {
        debts = newDebts
        notifyDataSetChanged()
    }
}
