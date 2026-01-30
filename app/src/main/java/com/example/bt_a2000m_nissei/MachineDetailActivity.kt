package com.example.bt_a2000m_nissei

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bt_a2000m_nissei.data.db.AppDatabase
import com.example.bt_a2000m_nissei.databinding.ActivityMachineDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MachineDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMachineDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMachineDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val machineId = intent.getStringExtra("machineId")
        if (machineId == null) {
            finish()
            return
        }

        lifecycleScope.launch {
            val machine = withContext(Dispatchers.IO) {
                AppDatabase.get(this@MachineDetailActivity)
                    .machineDao()
                    .findById(machineId)
            }

            if (machine == null) {
                finish()
                return@launch
            }

            // Hiển thị thông tin dây chuyền
            binding.tvMachineCode.text = "Mã dây chuyền: ${machine.machineCode}"
            binding.tvMachineName.text = "Tên dây chuyền: ${machine.machineName}"
            binding.tvLocation.text = "Vị trí: ${machine.location ?: "-"}"
            binding.tvDescription.text = "Mô tả: ${machine.description ?: "-"}"
            binding.tvNote.text = "Ghi chú: ${machine.note ?: "-"}"

            binding.btnStartChecklist.setOnClickListener {
                // bước sau: chọn checklist ngày / tuần / tháng
                startActivity(
                    Intent(this@MachineDetailActivity, ChecklistMenuActivity::class.java)
                        .putExtra("machineId", machine.id)
                )
            }
        }
    }
}
