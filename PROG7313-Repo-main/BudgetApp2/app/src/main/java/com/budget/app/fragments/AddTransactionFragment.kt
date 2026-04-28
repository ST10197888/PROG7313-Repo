package com.budget.app.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.budget.app.R
import com.budget.app.activities.MainActivity
import com.budget.app.models.TransactionType
import com.budget.app.utils.AppData

class AddTransactionFragment : Fragment() {

    private var selectedAttachmentUri: Uri? = null
    private var selectedAttachmentName: String? = null

    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var etNotes: EditText
    private lateinit var rgType: RadioGroup
    private lateinit var spinnerCat: Spinner
    private lateinit var btnSave: Button
    private lateinit var btnAttach: Button
    private lateinit var tvAttachmentName: TextView
    private lateinit var layoutAttachment: View

    // Overlay Views
    private lateinit var overlayContainer: View
    private lateinit var progressBarCard: CardView
    private lateinit var tvProgressBar: TextView
    private lateinit var tvProcessingLabel: TextView

    private var countDownTimer: CountDownTimer? = null

    // Configuration
    private val TOTAL_DURATION_MS   = 3000L
    private val TICK_INTERVAL_MS    = 30L
    private val PROGRESS_BAR_LENGTH = 20
    private val FILLED_CHAR         = '█'
    private val EMPTY_CHAR          = '░'

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                val fileSize = getFileSize(uri)
                if (fileSize > 5 * 1024 * 1024) {
                    Toast.makeText(requireContext(), "File size exceeds 5MB limit", Toast.LENGTH_LONG).show()
                } else {
                    selectedAttachmentUri = uri
                    selectedAttachmentName = getFileName(uri)
                    tvAttachmentName.text = selectedAttachmentName
                    tvAttachmentName.visibility = View.VISIBLE
                    btnAttach.text = "Change Document"
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_add_transaction, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etTitle = view.findViewById(R.id.etTitle)
        etAmount = view.findViewById(R.id.etAmount)
        etNotes = view.findViewById(R.id.etNotes)
        rgType = view.findViewById(R.id.rgTransactionType)
        spinnerCat = view.findViewById(R.id.spinnerCategory)
        btnSave = view.findViewById(R.id.btnSave)
        btnAttach = view.findViewById(R.id.btnAttach)
        tvAttachmentName = view.findViewById(R.id.tvAttachmentName)
        layoutAttachment = view.findViewById(R.id.layoutAttachment)

        // Overlay UI
        overlayContainer = view.findViewById(R.id.overlayContainer)
        progressBarCard = view.findViewById(R.id.progressBarCard)
        tvProgressBar = view.findViewById(R.id.tvProgressBar)
        tvProcessingLabel = view.findViewById(R.id.tvProcessingLabel)

        fun updateCategories(type: TransactionType) {
            val categories = AppData.getCategoriesForType(type)
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCat.adapter = adapter
            layoutAttachment.visibility = if (type == TransactionType.EXPENSE) View.VISIBLE else View.GONE
        }

        updateCategories(TransactionType.EXPENSE)

        rgType.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                R.id.rbIncome -> TransactionType.INCOME
                R.id.rbSavings -> TransactionType.SAVINGS
                else -> TransactionType.EXPENSE
            }
            updateCategories(type)
        }

        btnAttach.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            filePickerLauncher.launch(intent)
        }

        btnSave.setOnClickListener {
            if (validateInputs()) {
                startTransactionProcessing()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val title = etTitle.text.toString().trim()
        val amtStr = etAmount.text.toString().trim()

        if (title.isEmpty()) {
            etTitle.error = "Enter a title"
            return false
        }
        if (amtStr.isEmpty() || amtStr.toDoubleOrNull() == null || amtStr.toDouble() <= 0) {
            etAmount.error = "Enter a valid amount"
            return false
        }
        return true
    }

    private fun startTransactionProcessing() {
        showOverlay()
        disableAllInputs()
        startProgressBar()
    }

    private fun startProgressBar() {
        var elapsed = 0L
        countDownTimer = object : CountDownTimer(TOTAL_DURATION_MS, TICK_INTERVAL_MS) {
            override fun onTick(millisUntilFinished: Long) {
                elapsed += TICK_INTERVAL_MS
                val progress = (elapsed.toFloat() / TOTAL_DURATION_MS.toFloat()).coerceIn(0f, 1f)
                updateProgressBarText(progress)
            }

            override fun onFinish() {
                updateProgressBarText(1f)
                hideOverlay()
                enableAllInputs()
                onTransactionComplete()
            }
        }.start()
    }

    private fun updateProgressBarText(progress: Float) {
        val filledCount = (progress * PROGRESS_BAR_LENGTH).toInt()
        val emptyCount  = PROGRESS_BAR_LENGTH - filledCount
        val percentage  = (progress * 100).toInt()

        val bar = buildString {
            append("[")
            repeat(filledCount) { append(FILLED_CHAR) }
            repeat(emptyCount)  { append(EMPTY_CHAR)  }
            append("] $percentage%")
        }
        tvProgressBar.text = bar
    }

    private fun showOverlay() {
        overlayContainer.visibility = View.VISIBLE
        progressBarCard.visibility  = View.VISIBLE
        tvProcessingLabel.text      = "Processing Transaction..."
        updateProgressBarText(0f)
    }

    private fun hideOverlay() {
        overlayContainer.visibility = View.GONE
        progressBarCard.visibility  = View.GONE
    }

    private fun disableAllInputs() {
        etTitle.isEnabled = false
        etAmount.isEnabled = false
        etNotes.isEnabled = false
        rgType.isEnabled = false
        for (i in 0 until rgType.childCount) rgType.getChildAt(i).isEnabled = false
        spinnerCat.isEnabled = false
        btnSave.isEnabled = false
        btnAttach.isEnabled = false
    }

    private fun enableAllInputs() {
        etTitle.isEnabled = true
        etAmount.isEnabled = true
        etNotes.isEnabled = true
        rgType.isEnabled = true
        for (i in 0 until rgType.childCount) rgType.getChildAt(i).isEnabled = true
        spinnerCat.isEnabled = true
        btnSave.isEnabled = true
        btnAttach.isEnabled = true
    }

    private fun onTransactionComplete() {
        val type = when (rgType.checkedRadioButtonId) {
            R.id.rbIncome -> TransactionType.INCOME
            R.id.rbSavings -> TransactionType.SAVINGS
            else -> TransactionType.EXPENSE
        }

        AppData.addTransaction(
            title = etTitle.text.toString().trim(),
            amount = etAmount.text.toString().trim().toDouble(),
            type = type,
            category = spinnerCat.selectedItem.toString(),
            notes = etNotes.text.toString().trim(),
            attachmentUri = selectedAttachmentUri?.toString(),
            attachmentName = selectedAttachmentName
        )

        Toast.makeText(requireContext(), "Transaction added!", Toast.LENGTH_SHORT).show()
        (activity as? MainActivity)?.navigateTo(R.id.nav_dashboard)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }

    private fun getFileSize(uri: Uri): Long {
        return requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            cursor.getLong(sizeIndex)
        } ?: 0L
    }

    private fun getFileName(uri: Uri): String {
        return requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "Unknown File"
    }
}
