package com.example.bt_a2000m_nissei

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bt_a2000m_nissei.data.db.AppDatabase
import com.example.bt_a2000m_nissei.databinding.ActivityHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = HistoryAdapter(emptyList()) { item ->
            val intent = Intent(this, HistoryDetailActivity::class.java).apply {
                putExtra("sessionId", item.sessionId)
            }
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            try {
                val historyItems = withContext(Dispatchers.IO) {
                    AppDatabase.get(this@HistoryActivity).historyDao().getHistory()
                }
                adapter.updateData(historyItems)

                if (historyItems.isEmpty()) {
                    // Ít nhất biết là không có data chứ không phải “đơ”
                    title = "Lịch sử (0)"
                } else {
                    title = "Lịch sử (${historyItems.size})"
                }
            } catch (e: Exception) {
                title = "Lịch sử (lỗi)"
                android.widget.Toast.makeText(
                    this@HistoryActivity,
                    "Lỗi load lịch sử: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}
