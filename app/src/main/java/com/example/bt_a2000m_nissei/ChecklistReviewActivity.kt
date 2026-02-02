package com.example.bt_a2000m_nissei

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bt_a2000m_nissei.data.db.*
import com.example.bt_a2000m_nissei.databinding.ActivityChecklistReviewBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChecklistReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChecklistReviewBinding

    private var machineId: String? = null
    private var periods: List<InspectionPeriod> = emptyList()
    private var answers: Map<String, Any?> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        machineId = intent.getStringExtra("machineId")
        val periodsRaw = intent.getStringExtra("periods") ?: ""
        val answersJson = intent.getStringExtra("answers") ?: ""

        periods = periodsRaw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { runCatching { InspectionPeriod.valueOf(it) }.getOrNull() }

        answers = Gson().fromJson(answersJson, object : TypeToken<Map<String, Any?>>() {}.type)
            ?: emptyMap()

        if (machineId.isNullOrBlank() || periods.isEmpty()) {
            finish()
            return
        }

        renderReview()

        binding.btnRedo.setOnClickListener { confirmRedoToMenu() }
        binding.btnSave.setOnClickListener { confirmSave() }
    }

    private fun renderReview() {
        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                AppDatabase.get(this@ChecklistReviewActivity)
                    .runnerDao()
                    .getChecklistItems(machineId!!, periods)
            }

            binding.reviewContainer.removeAllViews()

            items.forEachIndexed { idx, it ->
                val v = answers[it.checklistItemId]
                val show = when (it.type) {
                    ChecklistItemType.BOOLEAN -> when (v as? Boolean) {
                        true -> "Đạt"
                        false -> "Không đạt"
                        null -> "(chưa chọn)"
                    }
                    ChecklistItemType.INPUT_TEXT -> (v as? String)?.ifBlank { "(trống)" } ?: "(trống)"
                    ChecklistItemType.INPUT_NUMBER -> {
                        when (v) {
                            is Number -> v.toDouble().toString()
                            is String -> v.toDoubleOrNull()?.toString() ?: "(chưa nhập)"
                            else -> "(chưa nhập)"
                        }
                    }
                }

                val fullText = "${idx + 1}. [${it.period.name}] ${it.title}\n→ $show"

                binding.reviewContainer.addView(TextView(this@ChecklistReviewActivity).apply {
                    text = fullText
                    setPadding(0, 10, 0, 10)
                    textSize = 18f
                })

                binding.reviewContainer.addView(View(this@ChecklistReviewActivity).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1
                    )
                    setBackgroundColor(0xFFDDDDDD.toInt())
                })
            }
        }
    }

    // ---------------- MODALS ----------------

    private fun confirmRedoToMenu() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận làm lại")
            .setMessage("Làm lại sẽ bỏ toàn bộ dữ liệu vừa nhập (chưa lưu) và quay về màn chọn checklist. Tiếp tục?")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Làm lại") { _, _ ->
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra("action", "REDO_TO_MENU")
                )
                finish()
            }
            .show()
    }

    private fun confirmSave() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận lưu")
            .setMessage("Bạn chắc chắn muốn LƯU phiên kiểm tra này?\nKhông thể hoàn tác.")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Lưu") { _, _ ->
                saveToDb()
            }
            .show()
    }

    // ---------------- SAVE ----------------

    private fun saveToDb() {
        // khóa nút tránh bấm 2 lần
        binding.btnSave.isEnabled = false
        binding.btnRedo.isEnabled = false

        lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    val db = AppDatabase.get(this@ChecklistReviewActivity)
                    val runnerDao = db.runnerDao()
                    val checklistDao = db.checklistDao()

                    // TODO: lấy user đăng nhập thật; tạm admin như bạn đang làm
                    val userId = checklistDao.findUserIdByUsername("admin") ?: return@withContext false

                    // map templateId theo period
                    val templateIdByPeriod = mutableMapOf<InspectionPeriod, String>()
                    for (p in periods) {
                        val t = checklistDao.getTemplateId(machineId!!, p) ?: continue
                        templateIdByPeriod[p] = t
                    }

                    if (templateIdByPeriod.isEmpty()) return@withContext false

                    // Load items để convert answers đúng kiểu
                    val items = runnerDao.getChecklistItems(machineId!!, periods)

                    val answerRows = items.mapNotNull { itInfo ->
                        val raw = answers[itInfo.checklistItemId]
                        when (itInfo.type) {
                            ChecklistItemType.BOOLEAN -> AnswerRow(
                                period = itInfo.period,
                                checklistItemId = itInfo.checklistItemId,
                                resultBoolean = raw as? Boolean,
                                resultText = null,
                                resultNumber = null
                            )

                            ChecklistItemType.INPUT_TEXT -> AnswerRow(
                                period = itInfo.period,
                                checklistItemId = itInfo.checklistItemId,
                                resultBoolean = null,
                                resultText = raw as? String,
                                resultNumber = null
                            )

                            ChecklistItemType.INPUT_NUMBER -> {
                                val num: Double? = when (raw) {
                                    is Number -> raw.toDouble()
                                    is String -> raw.toDoubleOrNull()
                                    else -> null
                                }
                                AnswerRow(
                                    period = itInfo.period,
                                    checklistItemId = itInfo.checklistItemId,
                                    resultBoolean = null,
                                    resultText = null,
                                    resultNumber = num
                                )
                            }
                        }
                    }

                    runnerDao.saveSession(
                        machineId = machineId!!,
                        periods = periods,
                        templateIdByPeriod = templateIdByPeriod,
                        performedByUserId = userId,
                        answers = answerRows
                    )

                    true
                } catch (_: Exception) {
                    false
                }
            }

            if (ok) {
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra("action", "SAVED")
                )
                finish()
            } else {
                Toast.makeText(this@ChecklistReviewActivity, "Lưu thất bại", Toast.LENGTH_LONG).show()
                binding.btnSave.isEnabled = true
                binding.btnRedo.isEnabled = true
            }
        }
    }
}
