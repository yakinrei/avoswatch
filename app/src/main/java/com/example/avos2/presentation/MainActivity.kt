package com.example.avos2.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.compose.material.MaterialTheme.colors
import com.example.avos2.R
import com.example.avos2.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val periods = arrayOf("Madrugada", "Manhã", "Tarde", "Noite")
    private var mediaPlayer: MediaPlayer? = null

    private val totalMillisInDay = getTotalMillisInDay()
    private var currentPeriod = setupPeriod(totalMillisInDay)
    private var currentAvo = setupAvo((totalMillisInDay))
    private var currentCentésimo = setupCentésimo((totalMillisInDay))
    private var currentSexto = setupSexto((totalMillisInDay))
    private val handler = Handler()

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 500) // Update every 500 ms

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateFullscreenMode()
        handler.post(updateRunnable) // Start time updates
        val bateriaReceiver: BroadcastReceiver =object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    val nivel: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 20)
                    binding.bateriaTextView.text = nivel.toString()
                    when (currentPeriod) {
                        0 -> binding.bateriaTextView.setTextColor(getColorStateList(R.color.raposa))
                        1 -> binding.bateriaTextView.setTextColor(getColorStateList(R.color.eagle))
                        2 -> binding.bateriaTextView.setTextColor(getColorStateList(R.color.badger))
                        3 -> binding.bateriaTextView.setTextColor(getColorStateList(R.color.wolf))
                    }
                }
            }
        }
        registerReceiver(bateriaReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        updateTela(currentPeriod)
    }

    private fun updateFullscreenMode() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // Keep screen on
    }

    private fun updateTela(currentPeriod: Int) {
        when (currentPeriod) {
            0 -> {
                playMusic(R.raw.fox)
                binding.periodoTextView.text = "Madrugada"
                binding.background.background = getDrawable(R.drawable.raposa)
            }
            1 -> {
                playMusic(R.raw.eagle)
                binding.periodoTextView.text = "Manhã"
                binding.background.background = getDrawable(R.drawable.eagle)
            }
            2 -> {
                binding.periodoTextView.text = "Tarde"
                playMusic(R.raw.badger)
                binding.background.background = getDrawable(R.drawable.badger)
            }
            3 -> {
                binding.periodoTextView.text = "Noite"
                playMusic(R.raw.wolf)
                binding.background.background = getDrawable(R.drawable.wolf)
            }
            else -> {
                playMusic(R.raw.fox)
                binding.periodoTextView.text = "Madrugada"
                binding.background.background = getDrawable(R.drawable.raposa)
            }
        }
    }

    private fun updateTime() {
        currentSexto = (currentSexto + 1) % 6
        if (currentSexto == 0) {
            currentCentésimo = (currentCentésimo + 1) % 100
            if (currentCentésimo == 0) {
                currentAvo = (currentAvo + 1) % 72
                if (currentAvo == 0) {
                    currentPeriod++
                    updateTela(currentPeriod)
                }
            }
        }
        binding.avoTextView.text = String.format("%02d", currentAvo)
        binding.centesimoTextView.text = String.format("%02d", currentCentésimo)
        binding.sextoTextView.text = currentSexto.toString()
    }

    private fun playMusic(soundResId: Int) {
        try {
            // Para o MediaPlayer atual, se houver
            mediaPlayer?.stop()
            mediaPlayer?.release()

            // Cria uma nova instância do MediaPlayer
            mediaPlayer = MediaPlayer.create(this, soundResId)
            mediaPlayer?.setOnErrorListener { mp, what, extra ->
                Log.e("MediaPlayer Error", "Error occurred: what=$what, extra=$extra")
                mp.release()
                mediaPlayer = null
                true
            }

            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                it.release()
                mediaPlayer = null // Limpa a referência após a liberação
            }
        } catch (e: Exception) {
            Log.e("Sound Play Error", "Error occurred while playing sound: ${e.message}")
        }
    }

    private fun setupPeriod(totalMillisInDay: Int): Int {
        return calculateCurrentTimeSection(21600000, totalMillisInDay)
    }

    private fun setupAvo(totalMillisInDay: Int): Int {
        var miliavos = totalMillisInDay % 21600000
        return calculateCurrentTimeSection(300000, miliavos)
    }

    private fun setupCentésimo(totalMillisInDay: Int): Int {
        var milicents = totalMillisInDay % 300000
        return calculateCurrentTimeSection(3000, milicents)
    }

    private fun setupSexto(totalMillisInDay: Int): Int {
        return (totalMillisInDay % 3000) / 500
    }

    private fun calculateCurrentTimeSection(threshold: Int, initialValue: Int): Int {
        var count = 0
        var initialValue = initialValue
        while (initialValue >= threshold) {
            count++
            initialValue -= threshold
        }
        return count
    }

    private fun getTotalMillisInDay(): Int {
        val calendar = Calendar.getInstance()
        return (calendar.get(Calendar.HOUR_OF_DAY) * 3600 * 1000 +
                calendar.get(Calendar.MINUTE) * 60 * 1000 +
                calendar.get(Calendar.SECOND) * 1000 +
                calendar.get(Calendar.MILLISECOND))
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable) // Stop updates when activity is destroyed
    }
}
