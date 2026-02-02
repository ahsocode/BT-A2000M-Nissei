package com.example.bt_a2000m_nissei

import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bt_a2000m_nissei.data.db.AppDatabase
import com.example.bt_a2000m_nissei.data.db.ChecklistItemType
import com.example.bt_a2000m_nissei.data.db.HistoryDetailItem
import com.example.bt_a2000m_nissei.databinding.ActivityHistoryDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailBinding

    private var sessionId: String? = null
    private var cachedRows: List<HistoryDetailItem> = emptyList()

    private val createCsvLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult

            if (cachedRows.isEmpty()) {
                toast("Chưa có dữ liệu để xuất")
                return@registerForActivityResult
            }

            lifecycleScope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        contentResolver.openOutputStream(uri)?.use { out ->
                            writeCsv(out, cachedRows)
                        } ?: throw IllegalStateException("Không mở được OutputStream")
                    }.isSuccess
                }

                if (ok) toast("Đã xuất CSV ✅")
                else toast("Xuất CSV thất bại ❌")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionId = intent.getStringExtra("sessionId")
        if (sessionId.isNullOrBlank()) {
            finish()
            return
        }

        // Nút export CSV (layout của bạn cần có btnExport)
        binding.btnExport.setOnClickListener {
            if (cachedRows.isEmpty()) {
                toast("Chưa có dữ liệu để xuất")
                return@setOnClickListener
            }
            val name = buildFileName()
            createCsvLauncher.launch(name)
        }

        loadAndRender(sessionId!!)
    }

    private fun loadAndRender(sessionId: String) {
        lifecycleScope.launch {
            val sessionDetails = withContext(Dispatchers.IO) {
                AppDatabase.get(this@HistoryDetailActivity)
                    .historyDao()
                    .getHistorySessionDetails(sessionId)
            }

            cachedRows = sessionDetails

            if (sessionDetails.isEmpty()) {
                binding.tvSessionDetails.text = "Không có dữ liệu"
                binding.resultsContainer.removeAllViews()
                return@launch
            }

            val first = sessionDetails.first()
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            binding.tvSessionDetails.text =
                "Máy: ${first.machineName} (${first.machineCode})\n" +
                        "Lúc: ${sdf.format(first.performedAt)}\n" +
                        "Phiên: $sessionId"

            binding.resultsContainer.removeAllViews()

            sessionDetails.forEach { item ->
                val show = when (item.itemType) {
                    ChecklistItemType.BOOLEAN -> when (item.resultBoolean) {
                        true -> "Đạt"
                        false -> "Không đạt"
                        null -> "(chưa chọn)"
                    }

                    ChecklistItemType.INPUT_TEXT ->
                        item.resultText?.ifBlank { "(trống)" } ?: "(trống)"

                    ChecklistItemType.INPUT_NUMBER ->
                        item.resultNumber?.toString() ?: "(chưa nhập)"
                }

                val fullText = "- [${item.period.name}] ${item.content}\n→ $show"
                val spannable = SpannableStringBuilder(fullText)
                val lineBreak = fullText.indexOf('\n')
                if (lineBreak > 0) {
                    spannable.setSpan(
                        StyleSpan(android.graphics.Typeface.BOLD),
                        0,
                        lineBreak,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }

                binding.resultsContainer.addView(TextView(this@HistoryDetailActivity).apply {
                    text = spannable
                    setPadding(0, 8, 0, 8)
                })
            }
        }
    }

    private fun buildFileName(): String {
        val sid = sessionId ?: "session"
        return "nissei_${sid.take(8)}.csv"
    }

    private fun writeCsv(out: OutputStream, rows: List<HistoryDetailItem>) {
        // UTF-8 + BOM để Excel mở tiếng Việt ít lỗi
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        out.write(bom)

        fun csvEscape(s: String): String {
            val needs = s.contains(',') || s.contains('"') || s.contains('\n') || s.contains('\r')
            val escaped = s.replace("\"", "\"\"")
            return if (needs) "\"$escaped\"" else escaped
        }

        val header = listOf(
            "Order", "SessionId", "PerformedAt", "MachineName", "MachineCode",
            "Period",  "Content", "Type", "Result"
        ).joinToString(",")

        out.write((header + "\n").toByteArray(Charsets.UTF_8))

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val sid = sessionId ?: ""

        for (r in rows) {
            val result = when (r.itemType) {
                ChecklistItemType.BOOLEAN -> when (r.resultBoolean) {
                    true -> "OK"
                    false -> "NG"
                    null -> ""
                }

                ChecklistItemType.INPUT_TEXT -> r.resultText ?: ""

                ChecklistItemType.INPUT_NUMBER -> r.resultNumber?.toString() ?: ""
            }

            val line = listOf(
                r.displayOrder.toString(),
                sid,
                sdf.format(r.performedAt),
                r.machineName,
                r.machineCode,
                r.period.name,
                r.content,
                r.itemType.name,
                result
            ).joinToString(",") { csvEscape(it) }


            out.write((line + "\n").toByteArray(Charsets.UTF_8))
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
