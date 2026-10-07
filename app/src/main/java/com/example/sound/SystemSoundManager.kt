package com.example.sound

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SystemSoundManager(private val context: Context) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var toneGenerator: ToneGenerator? = null

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (_: Exception) {
            // Audio stream initialization fallback
        }
    }

    fun playRepSound() {
        coroutineScope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
            } catch (_: Exception) {}
            vibrate(40)
        }
    }

    fun playBossHitSound() {
        coroutineScope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
            } catch (_: Exception) {}
            vibrate(75)
        }
    }

    fun playLevelUpSound() {
        coroutineScope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 120)
                delay(130)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_0, 140)
                delay(150)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 300)
            } catch (_: Exception) {}
            vibratePattern(longArrayOf(0, 100, 80, 200))
        }
    }

    fun playQuestCompleteSound() {
        coroutineScope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
                delay(150)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 250)
            } catch (_: Exception) {}
            vibratePattern(longArrayOf(0, 80, 50, 120))
        }
    }

    fun playSystemAlertSound() {
        coroutineScope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 300)
            } catch (_: Exception) {}
            vibratePattern(longArrayOf(0, 150, 100, 150))
        }
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(pattern: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
