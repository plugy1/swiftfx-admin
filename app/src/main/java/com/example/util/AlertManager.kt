package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.data.storage.AdminPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class AlertManager(
    private val context: Context,
    private val preferences: AdminPreferences
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Plays a pleasant dual-tone crystal chime and triggers haptic vibration
     */
    fun triggerAlert(urgent: Boolean = false) {
        if (preferences.soundEnabled) {
            scope.launch {
                playChimeSound(urgent)
            }
        }

        if (preferences.vibrationEnabled) {
            triggerVibration(urgent)
        }
    }

    /**
     * Synthesizes a clean audio alert chime using AudioTrack or ToneGenerator.
     * Generates a 2-stage melodic chime:
     * Note 1 (880 Hz - A5) -> Note 2 (1320 Hz - E6)
     */
    private fun playChimeSound(urgent: Boolean) {
        try {
            val sampleRate = 44100
            val durationMs = if (urgent) 220 else 160
            val numSamples = sampleRate * durationMs / 1000
            val buffer = ShortArray(numSamples * 2)

            // First note (880 Hz)
            val freq1 = 880.0
            val freq2 = if (urgent) 1760.0 else 1318.51 // E6

            for (i in 0 until numSamples) {
                // Envelope decay to make it chime/bell-like
                val envelope = (1.0 - (i.toDouble() / numSamples))
                val wave = sin(2.0 * Math.PI * freq1 * i / sampleRate) * envelope
                buffer[i] = (wave * Short.MAX_VALUE * 0.7).toInt().toShort()
            }

            // Second note
            for (i in 0 until numSamples) {
                val envelope = (1.0 - (i.toDouble() / numSamples))
                val wave = sin(2.0 * Math.PI * freq2 * i / sampleRate) * envelope
                buffer[numSamples + i] = (wave * Short.MAX_VALUE * 0.7).toInt().toShort()
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            // Release after playback
            scope.launch {
                kotlinx.coroutines.delay((durationMs * 2 + 100).toLong())
                audioTrack.stop()
                audioTrack.release()
            }
        } catch (e: Exception) {
            // Fallback to standard system tone generator
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 250)
                scope.launch {
                    kotlinx.coroutines.delay(300)
                    toneGen.release()
                }
            } catch (err: Exception) {
                Log.e("AlertManager", "Audio playback error: ${err.message}")
            }
        }
    }

    private fun triggerVibration(urgent: Boolean) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val timings = if (urgent) {
                            longArrayOf(0, 150, 100, 200, 100, 300)
                        } else {
                            longArrayOf(0, 120, 80, 180)
                        }
                        val amplitudes = if (urgent) {
                            intArrayOf(0, 255, 0, 255, 0, 255)
                        } else {
                            intArrayOf(0, 200, 0, 240)
                        }
                        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                        v.vibrate(effect)
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(if (urgent) 400 else 250)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AlertManager", "Vibrate error: ${e.message}")
        }
    }
}
