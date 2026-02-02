package com.example.bt_a2000m_nissei

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bt_a2000m_nissei.data.db.*
import com.example.bt_a2000m_nissei.databinding.ActivityChecklistRunnerBinding
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChecklistRunnerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChecklistRunnerBinding

    private var machineId: String? = null
    private var periods: List<InspectionPeriod> = emptyList()

    private var items: List<ChecklistItemInfo> = emptyList()
    private var currentItemIndex = 0

    private val answers = mutableMapOf<String, Any?>()

    private val reviewLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

            val action = result.data?.getStringExtra("action") ?: ""

            when (action) {
                "REDO_TO_MENU" -> {
                    // ✅ quay về màn chọn checklist (ChecklistMenu)
                    // do Preview đã finish rồi nên finish Runner là về Menu
                    finish()
                }
                "SAVED" -> {
                    toast("Đã lưu ✅")
                    // ✅ lưu xong thì thoát luôn về Menu
                    finish()
                }
                else -> {
                    // fallback cũ (nếu bạn muốn giữ)
                    currentItemIndex = 0
                    answers.clear()
                    showCurrentItem()
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistRunnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        machineId = intent.getStringExtra("machineId")
        val periodsRaw = intent.getStringExtra("periods") ?: ""

        periods = periodsRaw
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { runCatching { InspectionPeriod.valueOf(it) }.getOrNull() }

        if (machineId == null || periods.isEmpty()) {
            finish()
            return
        }

        binding.btnPrev.setOnClickListener { onPrev() }
        binding.btnNext.setOnClickListener { onNext() }

        loadItems()
    }

    private fun loadItems() {
        lifecycleScope.launch {
            items = withContext(Dispatchers.IO) {
                AppDatabase.get(this@ChecklistRunnerActivity)
                    .runnerDao()
                    .getChecklistItems(machineId!!, periods)
            }
            showCurrentItem()
        }
    }

    private fun showCurrentItem() {
        val item = items[currentItemIndex]

        binding.tvProgress.text = "${currentItemIndex + 1} / ${items.size}  •  [${item.period.name}]"
        binding.itemContainer.removeAllViews()

        binding.itemContainer.addView(TextView(this).apply {
            text = "${currentItemIndex + 1}. ${item.title}"
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 12)
        })

        val inputView = when (item.type) {
            ChecklistItemType.BOOLEAN -> createBooleanView(item)
            ChecklistItemType.INPUT_TEXT -> createInputTextView(item)
            ChecklistItemType.INPUT_NUMBER -> createInputNumberView(item)
        }
        binding.itemContainer.addView(inputView)

        binding.btnNext.text = if (currentItemIndex == items.size - 1) "XEM TỔNG KẾT" else "TIẾP"
    }

    private fun onNext() {
        if (!saveCurrentValue()) {
            toast("Vui lòng nhập/ chọn giá trị")
            return
        }
        if (currentItemIndex < items.size - 1) {
            currentItemIndex++
            showCurrentItem()
        } else {
            val intent = Intent(this, ChecklistReviewActivity::class.java).apply {
                putExtra("machineId", machineId)
                putExtra("periods", periods.joinToString(",") { it.name })
                putExtra("answers", Gson().toJson(answers))
            }
            reviewLauncher.launch(intent)
        }
    }

    private fun onPrev() {
        if (currentItemIndex > 0) {
            currentItemIndex--
            showCurrentItem()
        }
    }

    private fun createBooleanView(item: ChecklistItemInfo): View {
        val radioGroup = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        val rbOk = RadioButton(this).apply { text = "Đạt" }
        val rbNg = RadioButton(this).apply { text = "Không đạt" }
        radioGroup.addView(rbOk)
        radioGroup.addView(rbNg)
        val existing = answers[item.checklistItemId] as? Boolean
        if (existing != null) {
            if (existing) rbOk.isChecked = true else rbNg.isChecked = true
        }
        return radioGroup
    }

    private fun createInputTextView(item: ChecklistItemInfo): View = 
        EditText(this).apply {
            hint = "Nhập nội dung"
            setText(answers[item.checklistItemId] as? String ?: "")
        }

    private fun createInputNumberView(item: ChecklistItemInfo): View = 
        EditText(this).apply {
            hint = "Nhập số"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            val v = answers[item.checklistItemId] as? Double
            setText(v?.toString() ?: "")
        }

    private fun saveCurrentValue(): Boolean {
        if (currentItemIndex < 0 || currentItemIndex >= items.size) return false

        val item = items[currentItemIndex]
        val inputView = binding.itemContainer.getChildAt(1)

        val value: Any? = when (item.type) {
            ChecklistItemType.BOOLEAN -> {
                val rg = inputView as RadioGroup
                if (rg.checkedRadioButtonId == -1) {
                    return false
                }
                val rb = rg.findViewById<RadioButton>(rg.checkedRadioButtonId)
                rb.text == "Đạt"
            }
            ChecklistItemType.INPUT_TEXT -> {
                val text = (inputView as EditText).text.toString()
                if (text.isBlank()) {
                    return false
                }
                text
            }
            ChecklistItemType.INPUT_NUMBER -> {
                val text = (inputView as EditText).text.toString()
                if (text.isBlank()) {
                    return false
                }
                text.toDoubleOrNull()
            }
        }

        if (value == null && item.type == ChecklistItemType.INPUT_NUMBER) {
            return false
        }

        answers[item.checklistItemId] = value
        return true
    }

    private fun toast(msg: String) = 
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
