package com.example.bt_a2000m_nissei

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bt_a2000m_nissei.data.db.AppDatabase
import com.example.bt_a2000m_nissei.databinding.ActivityScanMachineBinding
import com.keyence.autoid.sdk.scan.DecodeResult
import com.keyence.autoid.sdk.scan.ScanManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanMachineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanMachineBinding

    private var scanManager: ScanManager? = null
    private var dataListener: ScanManager.DataListener? = null

    private var isInvalidState = false

    private val vibrator: Vibrator by lazy {
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanMachineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initScannerListener()

        binding.btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        scanManager = ScanManager.createScanManager(this)
        dataListener?.let { scanManager?.addDataListener(it) }
        setReadyUI()
    }

    override fun onPause() {
        super.onPause()
        vibrator.cancel()
        dataListener?.let { scanManager?.removeDataListener(it) }
        scanManager?.releaseScanManager()
        scanManager = null
    }

    private fun initScannerListener() {
        dataListener = object : ScanManager.DataListener {
            override fun onDataReceived(data: DecodeResult) {
                vibrator.cancel()

                val code = data.data?.trim().orEmpty()
                if (code.isEmpty()) return

                scanManager?.stopRead()

                lifecycleScope.launch {
                    val machine = withContext(Dispatchers.IO) {
                        AppDatabase.get(this@ScanMachineActivity).machineDao().findByCode(code)
                    }

                    if (machine == null) {
                        showInvalid(code)
                    } else {
                        setReadyUI()
                        val intent = Intent(this@ScanMachineActivity, MachineDetailActivity::class.java)
                        intent.putExtra("machineId", machine.id)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Handle F1 key press for going back
        if (event.keyCode == KeyEvent.KEYCODE_F1 && event.action == KeyEvent.ACTION_DOWN) {
            finish()
            return true
        }

        val isTriggerKey = (event.keyCode == KeyEvent.KEYCODE_FOCUS
                || event.keyCode == KeyEvent.KEYCODE_CAMERA)

        if (!isTriggerKey) return super.dispatchKeyEvent(event)

        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (isInvalidState) {
                    setReadyUI()
                } else {
                    setScanningUI()
                    scanManager?.startRead()
                }
                return true
            }
            KeyEvent.ACTION_UP -> {
                if (!isInvalidState) {
                    scanManager?.stopRead()
                    setReadyUI()
                }
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun setReadyUI() {
        isInvalidState = false
        binding.ngOverlay.visibility = View.GONE
        binding.root.setBackgroundColor(0xFFFFFFFF.toInt())
        binding.tvStatus.text = "Ready"
        vibrator.cancel()
    }

    private fun setScanningUI() {
        binding.ngOverlay.visibility = View.GONE
        binding.root.setBackgroundColor(0xFFFFFFFF.toInt())
        binding.tvStatus.text = "Scanning..."
    }

    private fun showInvalid(scanned: String) {
        isInvalidState = true
        binding.ngOverlay.visibility = View.VISIBLE
        binding.ngOverlayMessage.text = "MÃ KHÔNG HỢP LỆ: $scanned"

        beepError()
        vibrateError()
    }

    private fun beepError() {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            .startTone(ToneGenerator.TONE_PROP_NACK, 250)
    }

    private fun vibrateError() {
        val pattern = longArrayOf(0, 400, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vibrator.cancel()
    }
}
