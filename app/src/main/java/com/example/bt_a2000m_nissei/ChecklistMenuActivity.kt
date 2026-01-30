package com.example.bt_a2000m_nissei

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bt_a2000m_nissei.data.db.AppDatabase
import com.example.bt_a2000m_nissei.data.db.ChecksheetEntity
import com.example.bt_a2000m_nissei.data.db.InspectionPeriod
import com.example.bt_a2000m_nissei.databinding.ActivityChecklistMenuBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ChecklistMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChecklistMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val machineId = intent.getStringExtra("machineId")
        if (machineId == null) {
            finish()
            return
        }

        // Hiển thị tên máy cho đẹp
        lifecycleScope.launch {
            val machine = withContext(Dispatchers.IO) {
                AppDatabase.get(this@ChecklistMenuActivity).machineDao().findById(machineId)
            }
            binding.tvMachine.text = "Máy: ${machine?.machineName ?: "-"}"
        }

        binding.btnStart.setOnClickListener {
            val selected = buildList {
                if (binding.cbDaily.isChecked) add(InspectionPeriod.DAILY)
                if (binding.cbWeekly.isChecked) add(InspectionPeriod.WEEKLY)
                if (binding.cbMonthly.isChecked) add(InspectionPeriod.MONTHLY)
            }

            if (selected.isEmpty()) {
                binding.tvStatus.text = "Vui lòng chọn ít nhất 1 checklist"
                return@setOnClickListener
            }

            startSession(machineId, selected)
        }
    }

    private fun startSession(machineId: String, periods: List<InspectionPeriod>) {
        binding.tvStatus.text = "Đang tạo phiên kiểm tra..."
        binding.btnStart.isEnabled = false

        lifecycleScope.launch {
            val sessionId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val createdChecksheetIds = withContext(Dispatchers.IO) {
                val db = AppDatabase.get(this@ChecklistMenuActivity)
                val dao = db.checklistDao()

                val userId = dao.findUserIdByUsername("admin")
                    ?: return@withContext emptyList<String>()

                val ids = mutableListOf<String>()

                for (p in periods) {
                    val templateId = dao.getTemplateId(machineId, p) ?: continue

                    val checksheetId = UUID.randomUUID().toString()
                    dao.insertChecksheet(
                        ChecksheetEntity(
                            id = checksheetId,
                            sessionId = sessionId,
                            machineId = machineId,
                            templateId = templateId,
                            period = p,
                            performedByUserId = userId,
                            performedAt = now,
                            status = "PENDING",
                            synced = false,
                            createdAt = now
                        )
                    )
                    ids.add(checksheetId)
                }

                ids
            }

            if (createdChecksheetIds.isEmpty()) {
                binding.tvStatus.text = "Không tạo được checksheet (thiếu binding template)"
                binding.btnStart.isEnabled = true
                return@launch
            }

            startActivity(
                Intent(this@ChecklistMenuActivity, ChecklistRunnerActivity::class.java).apply {
                    putExtra("sessionId", sessionId)
                }
            )

            finish()
        }
    }
}
