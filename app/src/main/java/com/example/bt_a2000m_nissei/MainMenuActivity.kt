package com.example.bt_a2000m_nissei

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bt_a2000m_nissei.databinding.ActivityMainMenuBinding

class MainMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnScanMachine.setOnClickListener {
            startActivity(Intent(this, ScanMachineActivity::class.java))
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            // TODO: clear session / user (nếu có)
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        binding.btnExit.setOnClickListener {
            finishAffinity() // đóng toàn bộ app
        }

    }
}
