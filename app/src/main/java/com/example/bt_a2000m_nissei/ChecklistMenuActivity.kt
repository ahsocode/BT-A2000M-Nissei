package com.example.bt_a2000m_nissei

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bt_a2000m_nissei.data.db.AppDatabase
import com.example.bt_a2000m_nissei.data.db.InspectionPeriod
import com.example.bt_a2000m_nissei.databinding.ActivityChecklistMenuBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        lifecycleScope.launch {
            val machine = withContext(Dispatchers.IO) {
                AppDatabase.get(this@ChecklistMenuActivity).machineDao().findById(machineId)
            }
            binding.tvMachine.text = "Máy: ${machine?.machineName ?: "-"}"
        }

        binding.btnStart.setOnClickListener {
            val selectedPeriods = buildList {
                if (binding.cbDaily.isChecked) add(InspectionPeriod.DAILY)
                if (binding.cbWeekly.isChecked) add(InspectionPeriod.WEEKLY)
                if (binding.cbMonthly.isChecked) add(InspectionPeriod.MONTHLY)
            }

            if (selectedPeriods.isEmpty()) {
                binding.tvStatus.text = "Vui lòng chọn ít nhất 1 checklist"
                return@setOnClickListener
            }

            val intent = Intent(this, ChecklistPreviewActivity::class.java).apply {
                putExtra("machineId", machineId)
                putExtra("periods", selectedPeriods.joinToString(",") { it.name })
            }
            startActivity(intent)
        }
    }
}
