package com.example.bt_a2000m_nissei

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bt_a2000m_nissei.data.DbSeeder
import com.example.bt_a2000m_nissei.data.db.AppDatabase
import com.example.bt_a2000m_nissei.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Seed admin lần đầu (admin/admin123)
        lifecycleScope.launch {
            try {
                DbSeeder.seedIfNeeded(this@LoginActivity)
            } catch (_: Throwable) {
                // ignore
            }
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                showError("Vui lòng nhập đủ thông tin")
                return@setOnClickListener
            }

            doLoginOffline(username, password)
        }
    }

    private fun doLoginOffline(username: String, password: String) {
        setLoading(true, "Đang đăng nhập...")

        lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) {
                val db = AppDatabase.get(this@LoginActivity)
                val user = db.userDao().findByUsername(username) ?: return@withContext false
                if (!user.isActive) return@withContext false
                DbSeeder.verifyPasswordSimple(password, user.passwordHash)
            }

            if (!ok) {
                setLoading(false, "")
                showError("Sai tài khoản hoặc mật khẩu")
                return@launch
            }

            // Login OK → đi menu chính
            setLoading(false, "")
            binding.tvStatus.text = ""

            startActivity(Intent(this@LoginActivity, MainMenuActivity::class.java))
            finish()
        }
    }

    private fun showError(msg: String) {
        binding.tvStatus.text = msg
    }

    private fun setLoading(loading: Boolean, msg: String) {
        binding.btnLogin.isEnabled = !loading
        binding.etUsername.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.tvStatus.text = msg
    }
}
