package com.example.tictactoe

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Simple SoundManager using ToneGenerator (no sound files needed).
 * If you want real sounds, put click.mp3, win.mp3, draw.mp3 in res/raw/ and use MediaPlayer.
 */
class SoundManager(private val context: Context) {

    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    var soundEnabled = true
    var vibrationEnabled = true

    fun playClick() {
        if (!soundEnabled) return
        try {
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (_: Exception) { }
    }

    fun playWin() {
        if (!soundEnabled) return
        try {
            toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300)
        } catch (_: Exception) { }
    }

    fun playDraw() {
        if (!soundEnabled) return
        try {
            toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 200)
        } catch (_: Exception) { }
    }

    fun vibrate(duration: Long) {
        if (!vibrationEnabled) return
        val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            mgr.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    fun release() {
        try { toneGen.release() } catch (_: Exception) { }
    }
}