package com.budget.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.budget.app.R
import com.budget.app.models.FinancialGoal
import com.budget.app.utils.CurrencyFormatter

class FinancialGoalAdapter(
    private var goals: List<FinancialGoal>,
    private val onAddProgress: (FinancialGoal) -> Unit
) : RecyclerView.Adapter<FinancialGoalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvGoalName)
        val tvAmount: TextView = view.findViewById(R.id.tvGoalAmount)
        val pbGoal: ProgressBar = view.findViewById(R.id.pbGoal)
        val tvPercent: TextView = view.findViewById(R.id.tvGoalPercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_financial_goal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = goals[position]
        holder.tvName.text = goal.name
        holder.tvAmount.text = "${CurrencyFormatter.format(goal.currentAmount)} / ${CurrencyFormatter.format(goal.targetAmount)}"
        holder.pbGoal.progress = goal.progress
        holder.tvPercent.text = "${goal.progress}%"
        
        holder.itemView.setOnClickListener { onAddProgress(goal) }
    }

    override fun getItemCount() = goals.size

    fun updateData(newGoals: List<FinancialGoal>) {
        goals = newGoals
        notifyDataSetChanged()
    }
}
