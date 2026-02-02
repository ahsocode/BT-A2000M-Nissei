package com.example.bt_a2000m_nissei

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bt_a2000m_nissei.data.db.AppDatabase
import com.example.bt_a2000m_nissei.data.db.InspectionPeriod
import com.example.bt_a2000m_nissei.databinding.ActivityChecklistPreviewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChecklistPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChecklistPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val machineId = intent.getStringExtra("machineId")
        val periodsRaw = intent.getStringExtra("periods") ?: ""
        val periods = periodsRaw.split(",").map { InspectionPeriod.valueOf(it) }

        if (machineId == null || periods.isEmpty()) {
            finish()
            return
        }

        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                AppDatabase.get(this@ChecklistPreviewActivity).runnerDao().getChecklistItems(machineId, periods)
            }

            binding.tvChecklistInfo.text = "Checklist: ${periods.joinToString(" + ") { it.name }}\nTổng mục: ${items.size}"

            items.forEachIndexed { idx, it ->
                binding.itemPreviewContainer.addView(TextView(this@ChecklistPreviewActivity).apply {
                    text = "${idx + 1}. [${it.period.name}] ${it.title}"
                    setPadding(0, 8, 0, 8)
                    textSize = 20f
                })
            }
        }

        binding.btnStartChecklist.setOnClickListener {
            val intent = Intent(this, ChecklistRunnerActivity::class.java).apply {
                putExtra("machineId", machineId)
                putExtra("periods", periodsRaw)
            }
            startActivity(intent)
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
