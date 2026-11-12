package com.example

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object CallSoundManager {
    private const val TAG = "CallSoundManager"
    private var toneGenerator: ToneGenerator? = null
    private var isDialingTonePlaying = false
    private var dialingThread: Thread? = null
    private var incomingMediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    fun startDialingTone() {
        stopAll()
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 80)
            isDialingTonePlaying = true
            dialingThread = Thread {
                try {
                    while (isDialingTonePlaying) {
                        // European / standard PSTN Ringback tone: 1.5s tone, 3s pause
                        toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 1500)
                        Thread.sleep(3500)
                    }
                } catch (e: InterruptedException) {
                    // Normal stop
                } catch (e: Exception) {
                    Log.w(TAG, "Dialing tone loop error: ${e.message}")
                }
            }.apply {
                isDaemon = true
                start()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start dialing tone: ${e.message}")
        }
    }

    fun startIncomingRingtone(context: Context) {
        stopAll()
        try {
            val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            incomingMediaPlayer = MediaPlayer().apply {
                setDataSource(context, ringtoneUri)
                setAudioStreamType(AudioManager.STREAM_RING)
                isLooping = true
                prepare()
                start()
            }

            // Start vibration
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            val pattern = longArrayOf(0, 1000, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start incoming ringtone: ${e.message}")
        }
    }

    fun stopAll() {
        isDialingTonePlaying = false
        dialingThread?.interrupt()
        dialingThread = null

        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
        } catch (e: Exception) {}
        toneGenerator = null

        try {
            incomingMediaPlayer?.stop()
            incomingMediaPlayer?.release()
        } catch (e: Exception) {}
        incomingMediaPlayer = null

        try {
            vibrator?.cancel()
        } catch (e: Exception) {}
        vibrator = null
    }
}
