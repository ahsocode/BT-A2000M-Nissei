package com.example.bt_a2000m_nissei

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bt_a2000m_nissei.data.db.AppDatabase
import com.example.bt_a2000m_nissei.data.db.ChecklistItemInfo
import com.example.bt_a2000m_nissei.data.db.ChecklistItemType
import com.example.bt_a2000m_nissei.data.db.ChecksheetItemResultEntity
import com.example.bt_a2000m_nissei.databinding.ActivityChecklistRunnerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ChecklistRunnerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChecklistRunnerBinding

    private var items: List<ChecklistItemInfo> = emptyList()
    private var currentItemIndex = -1

    private val results = mutableMapOf<String, Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistRunnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionId = intent.getStringExtra("sessionId")
        if (sessionId == null) {
            finish()
            return
        }

        binding.btnNext.setOnClickListener { onNext() }
        binding.btnPrev.setOnClickListener { onPrev() }

        loadChecklistItems(sessionId)
    }

    private fun loadChecklistItems(sessionId: String) {
        lifecycleScope.launch {
            items = withContext(Dispatchers.IO) {
                AppDatabase.get(this@ChecklistRunnerActivity)
                    .runnerDao()
                    .getChecklistItemsForSession(sessionId)
            }
            if (items.isNotEmpty()) {
                currentItemIndex = 0
                showCurrentItem()
            }
        }
    }

    private fun onNext() {
        if (!saveCurrentValue()) return
        if (currentItemIndex < items.size - 1) {
            currentItemIndex++
            showCurrentItem()
        } else {
            finishChecklist()
        }
    }

    private fun onPrev() {
        if (currentItemIndex > 0) {
            currentItemIndex--
            showCurrentItem()
        }
    }

    private fun showCurrentItem() {
        val item = items[currentItemIndex]
        binding.tvProgress.text = "${currentItemIndex + 1} / ${items.size}"
        binding.itemContainer.removeAllViews()

        val view = when (item.type) {
            ChecklistItemType.BOOLEAN -> createBooleanView(item)
            ChecklistItemType.INPUT_TEXT -> createInputTextView(item)
            ChecklistItemType.INPUT_NUMBER -> createInputNumberView(item)
        }
        binding.itemContainer.addView(view)
    }

    private fun createBooleanView(item: ChecklistItemInfo): View {
        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
        }
        val rbYes = RadioButton(this).apply { text = "Đạt" }
        val rbNo = RadioButton(this).apply { text = "Không đạt" }
        radioGroup.addView(rbYes)
        radioGroup.addView(rbNo)

        results[item.checklistItemId]?.let {
            if (it as Boolean) rbYes.isChecked = true else rbNo.isChecked = true
        }
        return radioGroup
    }

    private fun createInputTextView(item: ChecklistItemInfo): View {
        return EditText(this).apply {
            hint = item.title
            setText(results[item.checklistItemId] as? String ?: "")
        }
    }

    private fun createInputNumberView(item: ChecklistItemInfo): View {
        return EditText(this).apply {
            hint = item.title
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(results[item.checklistItemId]?.toString() ?: "")
        }
    }

    private fun saveCurrentValue(): Boolean {
        val item = items[currentItemIndex]
        val view = binding.itemContainer.getChildAt(0)
        val value = when (item.type) {
            ChecklistItemType.BOOLEAN -> {
                val radioGroup = view as RadioGroup
                if (radioGroup.checkedRadioButtonId == -1) return false
                val radioButton = radioGroup.findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
                radioButton.text == "Đạt"
            }
            ChecklistItemType.INPUT_TEXT -> {
                val editText = view as EditText
                editText.text.toString()
            }
            ChecklistItemType.INPUT_NUMBER -> {
                val editText = view as EditText
                editText.text.toString().toDoubleOrNull()
            }
        }
        if (value != null) {
            results[item.checklistItemId] = value
        }
        return true
    }

    private fun finishChecklist() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.get(this@ChecklistRunnerActivity)
                val dao = db.runnerDao()

                for ((checklistItemId, value) in results) {
                    val checksheetId = items.first { it.checklistItemId == checklistItemId }.checksheetId
                    val type = items.first { it.checklistItemId == checklistItemId }.type

                    val resultEntity = when (type) {
                        ChecklistItemType.BOOLEAN -> ChecksheetItemResultEntity(
                            id = UUID.randomUUID().toString(),
                            checksheetId = checksheetId,
                            checklistItemId = checklistItemId,
                            resultBoolean = value as Boolean,
                            resultText = null,
                            resultNumber = null
                        )
                        ChecklistItemType.INPUT_TEXT -> ChecksheetItemResultEntity(
                            id = UUID.randomUUID().toString(),
                            checksheetId = checksheetId,
                            checklistItemId = checklistItemId,
                            resultBoolean = null,
                            resultText = value as String,
                            resultNumber = null
                        )
                        ChecklistItemType.INPUT_NUMBER -> ChecksheetItemResultEntity(
                            id = UUID.randomUUID().toString(),
                            checksheetId = checksheetId,
                            checklistItemId = checklistItemId,
                            resultBoolean = null,
                            resultText = null,
                            resultNumber = value as Double
                        )
                    }
                    // dao.insertResult(resultEntity) // You need to create this method in your DAO
                }
            }
            finish()
        }
    }
}
