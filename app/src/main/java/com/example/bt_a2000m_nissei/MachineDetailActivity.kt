package com.example.bt_a2000m_nissei

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
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
                AppDatabase.get(this@MachineDetailActivity).machineDao().findById(machineId)
            }
            if (machine != null) {
                binding.tvMachineCode.text = "Mã dây chuyền: ${machine.machineCode}"
                binding.tvMachineName.text = "Tên dây chuyền: ${machine.machineName}"
                binding.tvLocation.text = "Vị trí: ${machine.location ?: "-"}"
                binding.tvDescription.text = "Mô tả: ${machine.description ?: "-"}"
                binding.tvNote.text = "Ghi chú: ${machine.note ?: "-"}"
            }
        }

        binding.btnStartChecklist.setOnClickListener {
            val intent = Intent(this, ChecklistMenuActivity::class.java)
            intent.putExtra("machineId", machineId)
            startActivity(intent)
        }

        binding.btnScanAgain.setOnClickListener {
            finish()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
