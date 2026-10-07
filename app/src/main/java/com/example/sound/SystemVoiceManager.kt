package com.example.sound

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.data.model.HunterProfile
import com.example.data.model.HunterRank
import java.util.Locale

private const val TAG = "SystemVoiceManager"

class SystemVoiceManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    var isEnabled: Boolean = true

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "TTS Language not supported, fallback to default")
                tts?.setLanguage(Locale.getDefault())
            }
            // Tuning for sharp, authoritative Solo Leveling System voice
            tts?.setPitch(0.92f)
            tts?.setSpeechRate(1.05f)
            isInitialized = true
        } else {
            Log.e(TAG, "TTS Initialization failed with status: $status")
        }
    }

    fun speak(message: String) {
        if (!isEnabled || !isInitialized || tts == null) return
        try {
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "SYS_VOICE_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking message", e)
        }
    }

    fun announceRankAdvancement(hunterName: String, newRank: HunterRank) {
        val announcement = "System Alert! Attention Hunter $hunterName. You have broken through your physical limits and achieved promotion to ${newRank.title}. Your aura surges with newfound power."
        speak(announcement)
    }

    fun announceMilestone(title: String, description: String) {
        val announcement = "System Milestone reached: $title. $description. All systems operational."
        speak(announcement)
    }

    fun announceWorkoutSummary(exerciseName: String, reps: Int, calories: Float, durationSec: Long) {
        val mins = (durationSec / 60).coerceAtLeast(1)
        val announcement = "Training session concluded. $reps reps of $exerciseName completed in $mins minutes. ${calories.toInt()} calories burned. Great form, Hunter."
        speak(announcement)
    }

    fun announceStatusBriefing(profile: HunterProfile, rank: HunterRank, questsRemaining: Int) {
        val questStatus = if (questsRemaining == 0) {
            "All daily commissions are fully accomplished today."
        } else {
            "You have $questsRemaining daily commissions pending."
        }
        val briefing = "System Briefing for Hunter ${profile.hunterName}. Level ${profile.level}, ${rank.title}. Total repetitions logged: ${profile.totalReps}. Dungeons conquered: ${profile.dungeonsCleared}. $questStatus Arise, and continue your ascension."
        speak(briefing)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
    }
}
