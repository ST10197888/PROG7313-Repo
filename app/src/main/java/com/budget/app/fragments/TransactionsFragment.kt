package com.budget.app.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.budget.app.R
import com.budget.app.activities.MainActivity
import com.budget.app.adapters.TransactionAdapter
import com.budget.app.models.Transaction
import com.budget.app.utils.AppData

class TransactionsFragment : Fragment(), MainActivity.OnBackPressedListener {

    private lateinit var adapter: TransactionAdapter
    private lateinit var tvEmpty: TextView
    private lateinit var rv: RecyclerView

    // Overlay Views
    private lateinit var overlayContainer: View
    private lateinit var progressBarCard: CardView
    private lateinit var tvProgressBar: TextView
    private lateinit var btnCancelDelete: Button

    private var deletionTimer: CountDownTimer? = null
    private var transactionToDelete: Transaction? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_transactions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.rvTransactions)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        overlayContainer = view.findViewById(R.id.overlayContainer)
        progressBarCard = view.findViewById(R.id.progressBarCard)
        tvProgressBar = view.findViewById(R.id.tvProgressBar)
        btnCancelDelete = view.findViewById(R.id.btnCancelDelete)

        adapter = TransactionAdapter(
            initialData = AppData.getAllTransactions(),
            onDelete = { transaction ->
                startDeletionProcess(transaction)
            },
            onDetails = { transaction ->
                (activity as? MainActivity)?.loadFragment(TransactionDetailsFragment.newInstance(transaction.id))
            }
        )

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btnCancelDelete.setOnClickListener {
            cancelDeletion()
        }

        refreshList()
    }

    override fun onBackPressed(): Boolean {
        if (::rv.isInitialized && rv.computeVerticalScrollOffset() > 0) {
            rv.smoothScrollToPosition(0)
            return true
        }
        return false
    }

    private fun startDeletionProcess(transaction: Transaction) {
        transactionToDelete = transaction
        showOverlay()
        
        var elapsed = 0L
        val totalDuration = 3500L
        val tickInterval = 50L

        deletionTimer = object : CountDownTimer(totalDuration, tickInterval) {
            override fun onTick(millisUntilFinished: Long) {
                elapsed += tickInterval
                val progress = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
                updateProgressBarText(progress)
            }

            override fun onFinish() {
                updateProgressBarText(1f)
                completeDeletion()
            }
        }.start()
    }

    private fun updateProgressBarText(progress: Float) {
        val length = 20
        val filledCount = (progress * length).toInt()
        val emptyCount = length - filledCount
        val percentage = (progress * 100).toInt()

        val bar = buildString {
            append("[")
            repeat(filledCount) { append("█") }
            repeat(emptyCount) { append("░") }
            append("] $percentage%")
        }
        tvProgressBar.text = bar
    }

    private fun showOverlay() {
        overlayContainer.visibility = View.VISIBLE
        progressBarCard.visibility = View.VISIBLE
    }

    private fun hideOverlay() {
        overlayContainer.visibility = View.GONE
        progressBarCard.visibility = View.GONE
    }

    private fun cancelDeletion() {
        deletionTimer?.cancel()
        hideOverlay()
        transactionToDelete = null
        Toast.makeText(requireContext(), "Deletion cancelled", Toast.LENGTH_SHORT).show()
    }

    private fun completeDeletion() {
        transactionToDelete?.let {
            AppData.removeTransaction(it.id)
            refreshList()
            Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show()
        }
        hideOverlay()
        transactionToDelete = null
    }

    private fun refreshList() {
        val list = AppData.getAllTransactions()
        adapter.updateData(list)
        tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deletionTimer?.cancel()
    }
}
