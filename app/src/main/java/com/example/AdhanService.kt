package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class AdhanService : Service() {

    companion object {
        private const val TAG = "AdhanService"
        const val ACTION_START_ADHAN = "com.example.ACTION_START_ADHAN"
        const val ACTION_STOP_ADHAN = "com.example.ACTION_STOP_ADHAN"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_REMINDER_STEP = "extra_reminder_step"
        const val NOTIFICATION_ID = 78601
        const val CHANNEL_ID = "adhan_playback_channel_v4"

        fun startAdhan(context: Context, prayerName: String, reminderStep: Int = 0) {
            val intent = Intent(context, AdhanService::class.java).apply {
                action = ACTION_START_ADHAN
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_REMINDER_STEP, reminderStep)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start AdhanService foreground, starting directly", e)
                try {
                    context.startService(intent)
                } catch (err: Exception) {
                    Log.e(TAG, "Direct startService also failed", err)
                }
            }
        }

        fun stopAdhan(context: Context) {
            val intent = Intent(context, AdhanService::class.java).apply {
                action = ACTION_STOP_ADHAN
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop AdhanService", e)
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var focusRequest: AudioFocusRequest? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_ADHAN
        if (action == ACTION_STOP_ADHAN) {
            stopAdhanPlayback()
            stopSelf()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "সালাত"
        val reminderStep = intent?.getIntExtra(EXTRA_REMINDER_STEP, 0) ?: 0

        val prayerNameBn = when (prayerName.lowercase()) {
            "fajr" -> "ফজর"
            "dhuhr" -> "যোহর"
            "asr" -> "আসর"
            "maghrib" -> "মাগরিব"
            "isha" -> "ইশা"
            "tahajjud" -> "তাহাজ্জুদ"
            else -> prayerName
        }

        val notificationTitle = if (reminderStep == 35) {
            "⚠️ $prayerNameBn নামাজের আজান (৩৫ মিনিট পার)"
        } else if (prayerName.equals("Tahajjud", ignoreCase = true)) {
            "🌙 শেষ রাতের তাহাজ্জুদ সালাত — আজান ধ্বনি"
        } else {
            "🕌 $prayerNameBn নামাজের ওয়াক্ত হয়েছে — আজান"
        }

        val notificationText = if (reminderStep == 35) {
            "ওয়াক্তের ৩৫ মিনিট পার হয়েছে! এখনই সালাত আদায় করুন।"
        } else {
            "পবিত্র $prayerNameBn নামাজের জন্য আজান বাজছে। সালাতের প্রস্তুতি নিন।"
        }

        val notification = buildForegroundNotification(notificationTitle, notificationText)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed startForeground with media type, falling back", e)
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (err: Throwable) {
                Log.e(TAG, "Fatal startForeground fallback failed", err)
            }
        }

        playAdhanAudio()

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                CHANNEL_ID,
                "নামাজের আযান ও সালাত এলার্ট",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "নামাজের ওয়াক্তের সাথে সাথে ও ৩৫ মিনিট পর রিয়েল আজান বাজানো"
                setSound(null, null)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(title: String, text: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            101,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AdhanService::class.java).apply {
            action = ACTION_STOP_ADHAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            102,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setLargeIcon(NotificationHelper.getAppLogoBitmap(this))
            .setContentTitle(title)
            .setContentText(text)
            .setColor(0xFF0284C7.toInt())
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(android.R.drawable.ic_media_pause, "আযান থামান", stopPendingIntent)
            .build()
    }

    private fun createMediaPlayer(): MediaPlayer? {
        return AdhanPlayer.createMediaPlayer(this)
    }

    private fun playAdhanAudio() {
        stopAdhanPlayback()

        // 1. Acquire WakeLock
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FaithShield:AdhanServiceLock").apply {
                acquire(4 * 60 * 1000L) // 4 mins
            }
        } catch (e: Exception) {
            Log.e(TAG, "WakeLock error", e)
        }

        // 2. Request Audio Focus
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            Log.d(TAG, "Adhan audio focus changed: $focusChange")
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttr = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(audioAttr)
                    .setOnAudioFocusChangeListener(focusChangeListener)
                    .build()
                focusRequest?.let { am.requestAudioFocus(it) }
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Audio focus request exception: ${e.message}")
        }

        // 3. Make sure media volume is audible
        try {
            val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (currentVol < (maxVol * 0.5f).toInt()) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, (maxVol * 0.85f).toInt(), 0)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Volume adjust error", e)
        }

        // 4. Initialize and start MediaPlayer
        try {
            val player = createMediaPlayer()

            if (player != null) {
                player.setVolume(1.0f, 1.0f)
                player.setOnCompletionListener {
                    Log.d(TAG, "Adhan playback completed naturally.")
                    stopAdhanPlayback()
                    stopSelf()
                }
                player.setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: $what, $extra")
                    stopAdhanPlayback()
                    stopSelf()
                    true
                }
                player.start()
                mediaPlayer = player
                AdhanPlayer.notifyPlayingState(true)
                Log.d(TAG, "Adhan started playing successfully in AdhanService!")
            } else {
                Log.e(TAG, "Failed to create MediaPlayer for Adhan raw sound.")
                stopSelf()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting Adhan MediaPlayer in service", e)
            stopSelf()
        }
    }

    private fun stopAdhanPlayback() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaPlayer", e)
        }

        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing WakeLock", e)
        }

        try {
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { am.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio focus", e)
        }

        AdhanPlayer.notifyPlayingState(false)
    }

    override fun onDestroy() {
        stopAdhanPlayback()
        super.onDestroy()
    }
}
