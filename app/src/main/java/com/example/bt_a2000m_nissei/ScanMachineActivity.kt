package com.example.bt_a2000m_nissei

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.example.bt_a2000m_nissei.data.db.AppDatabase
import com.example.bt_a2000m_nissei.data.db.MachineEntity
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

    private var selectedMachineId: String? = null

    private var isInvalidState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanMachineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, ChecklistMenuActivity::class.java).apply {
                putExtra("machineId", selectedMachineId)
            })
        }

        initScanner()
        setReadyUI()
    }

    private fun initScanner() {
        scanManager = ScanManager.createScanManager(this)

        dataListener = object : ScanManager.DataListener {
            override fun onDataReceived(data: DecodeResult) {
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
                        showValid(machine)
                    }
                }
            }
        }

        scanManager?.addDataListener(dataListener)
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        val isTriggerKey = (event.keyCode == android.view.KeyEvent.KEYCODE_FOCUS
                || event.keyCode == android.view.KeyEvent.KEYCODE_CAMERA)

        if (!isTriggerKey) return super.dispatchKeyEvent(event)

        when (event.action) {
            android.view.KeyEvent.ACTION_DOWN -> {
                if (isInvalidState) {
                    setReadyUI()
                    return true
                }
                setScanningUI()
                scanManager?.startRead()
                return true
            }
            android.view.KeyEvent.ACTION_UP -> {
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

        // Stop vibration
        val vibrator = getSystemService<Vibrator>()
        vibrator?.cancel()
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

        binding.btnNext.isEnabled = false
        selectedMachineId = null

        binding.tvMachineCode.text = "Mã: -"
        binding.tvMachineName.text = "Tên: -"
        binding.tvMachineLocation.text = "Vị trí: -"
        binding.tvMachineNote.text = "Ghi chú: -"

        beepError()
        vibrateError()
    }

    private fun showValid(machine: MachineEntity) {
        val vibrator = getSystemService<Vibrator>()
        vibrator?.cancel()

        isInvalidState = false
        binding.ngOverlay.visibility = View.GONE
        binding.root.setBackgroundColor(0xFFC8E6C9.toInt())
        binding.tvStatus.text = "OK: Mã hợp lệ"

        binding.tvMachineCode.text = "Mã: ${machine.machineCode}"
        binding.tvMachineName.text = "Tên: ${machine.machineName}"
        binding.tvMachineLocation.text = "Vị trí: ${machine.location ?: "-"}"
        binding.tvMachineNote.text = "Ghi chú: ${machine.note ?: "-"}"

        selectedMachineId = machine.id
        binding.btnNext.isEnabled = true
    }

    private fun beepError() {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            .startTone(ToneGenerator.TONE_PROP_NACK, 250)
    }

    private fun vibrateError() {
        val vibrator = getSystemService<Vibrator>() ?: return
        // Vibrate for 400ms, pause for 400ms, repeat
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
        dataListener?.let { scanManager?.removeDataListener(it) }
        scanManager?.releaseScanManager()
        scanManager = null

        // Stop vibration
        val vibrator = getSystemService<Vibrator>()
        vibrator?.cancel()
    }
}
