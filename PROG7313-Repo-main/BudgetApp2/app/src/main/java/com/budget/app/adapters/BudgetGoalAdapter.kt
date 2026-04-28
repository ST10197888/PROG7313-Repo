package com.budget.app.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.budget.app.R
import com.budget.app.models.BudgetGoal
import com.budget.app.utils.CurrencyFormatter

class BudgetGoalAdapter(
    private val data: MutableList<BudgetGoal>,
    private val onDelete: (BudgetGoal) -> Unit
) : RecyclerView.Adapter<BudgetGoalAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory  : TextView    = view.findViewById(R.id.tvGoalCategory)
        val tvSpent     : TextView    = view.findViewById(R.id.tvGoalSpent)
        val tvLimit     : TextView    = view.findViewById(R.id.tvGoalLimit)
        val tvRemaining : TextView    = view.findViewById(R.id.tvGoalRemaining)
        val progressBar : ProgressBar = view.findViewById(R.id.pbGoalProgress)
        val btnDelete   : ImageButton = view.findViewById(R.id.btnDeleteGoal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_budget_goal, parent, false))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = data[position]
        holder.tvCategory.text  = goal.category
        holder.tvSpent.text     = "Spent: ${CurrencyFormatter.format(goal.spentAmount)}"
        holder.tvLimit.text     = "Limit: ${CurrencyFormatter.format(goal.limitAmount)}"
        holder.tvRemaining.text = "Remaining: ${CurrencyFormatter.format(goal.remainingAmount)}"
        holder.progressBar.progress = goal.percentageUsed

        // Turn bar red when over 80%
        val barColor = when {
            goal.percentageUsed >= 100 -> Color.parseColor("#F44336")
            goal.percentageUsed >= 80  -> Color.parseColor("#FF9800")
            else                       -> Color.parseColor("#4CAF50")
        }
        holder.progressBar.progressTintList = ColorStateList.valueOf(barColor)

        holder.btnDelete.setOnClickListener { onDelete(goal) }
    }

    fun updateData(newList: List<BudgetGoal>) {
        data.clear()
        data.addAll(newList)
        notifyDataSetChanged()
    }
}