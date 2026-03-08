package com.budget.app.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.budget.app.R
import com.budget.app.models.TransactionType
import com.budget.app.utils.AppData

class CategoriesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_categories, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSection(
            view.findViewById(R.id.btnHeaderExpense),
            view.findViewById(R.id.layoutExpenseList),
            view.findViewById(R.id.ivArrowExpense),
            TransactionType.EXPENSE
        )

        setupSection(
            view.findViewById(R.id.btnHeaderIncome),
            view.findViewById(R.id.layoutIncomeList),
            view.findViewById(R.id.ivArrowIncome),
            TransactionType.INCOME
        )

        setupSection(
            view.findViewById(R.id.btnHeaderSavings),
            view.findViewById(R.id.layoutSavingsList),
            view.findViewById(R.id.ivArrowSavings),
            TransactionType.SAVINGS
        )
    }

    private fun setupSection(header: View, listContainer: LinearLayout, arrow: ImageView, type: TransactionType) {
        // Populate the list
        val categories = AppData.getCategoriesForType(type)
        listContainer.removeAllViews()
        
        categories.forEach { category ->
            val tv = TextView(requireContext())
            tv.text = "• $category"
            tv.textSize = 16f
            tv.setPadding(48, 16, 16, 16)
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            listContainer.addView(tv)
            
            // Divider
            val divider = View(requireContext())
            divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background_grey))
            val params = divider.layoutParams as LinearLayout.LayoutParams
            params.setMargins(48, 0, 0, 0)
            listContainer.addView(divider)
        }

        header.setOnClickListener {
            if (listContainer.visibility == View.VISIBLE) {
                listContainer.visibility = View.GONE
                arrow.animate().rotation(0f).start()
            } else {
                listContainer.visibility = View.VISIBLE
                arrow.animate().rotation(180f).start()
            }
        }
    }
}
