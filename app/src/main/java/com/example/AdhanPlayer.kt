package com.example

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdhanPlayer {
    private const val TAG = "AdhanPlayer"
    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var _isPlaying: Boolean = false
    val isPlaying: Boolean
        get() = _isPlaying

    private val _isPlayingFlow = MutableStateFlow(false)
    val isPlayingFlow: StateFlow<Boolean> = _isPlayingFlow.asStateFlow()

    private val _isEnabledFlow = MutableStateFlow(true)
    val isEnabledFlow: StateFlow<Boolean> = _isEnabledFlow.asStateFlow()

    private val _selectedAdhanFlow = MutableStateFlow("madinah")
    val selectedAdhanFlow: StateFlow<String> = _selectedAdhanFlow.asStateFlow()

    private var isInitialized = false

    private fun getPrefs(context: Context) =
        context.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)

    fun initialize(context: Context) {
        if (!isInitialized) {
            _isEnabledFlow.value = isAdhanEnabled(context)
            _selectedAdhanFlow.value = getSelectedAdhanSource(context)
            isInitialized = true
        }
    }

    fun isAdhanEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean("adhan_sound_enabled", true)

    fun setAdhanEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("adhan_sound_enabled", enabled).apply()
        _isEnabledFlow.value = enabled
    }

    fun getSelectedAdhanSource(context: Context): String =
        getPrefs(context).getString("selected_adhan_source", "madinah") ?: "madinah"

    fun setSelectedAdhanSource(context: Context, source: String) {
        getPrefs(context).edit().putString("selected_adhan_source", source).apply()
        _selectedAdhanFlow.value = source
    }

    fun getSelectedAdhanRes(context: Context): Int {
        return when (getSelectedAdhanSource(context).lowercase()) {
            "makkah" -> R.raw.adhan_makkah
            else -> R.raw.adhan_madinah
        }
    }

    /**
     * Checks if the given prayer should play Adhan.
     * STRICT RULE: Sunrise and Sunset must NEVER play Adhan!
     * Fajr, Dhuhr, Asr, Maghrib, Isha, and Tahajjud are allowed.
     */
    fun shouldPlayAdhanForPrayer(prayerName: String): Boolean {
        val normalized = prayerName.trim().lowercase()
        return when (normalized) {
            "fajr", "ফজর", "dhuhr", "যোহর", "জোহর", "asr", "আসর", "maghrib", "মাগরিব", "isha", "ইশা", "এশা",
            "tahajjud", "তাহাজ্জুদ" -> true
            // Sunrise (সূর্যোদয়) and Sunset (সূর্যাস্ত) are strictly excluded from Adhan audio
            "sunrise", "সূর্যোদয়", "সূর্যোদয়", "sunset", "সূর্যাস্ত" -> false
            else -> false
        }
    }

    fun notifyPlayingState(playing: Boolean) {
        _isPlaying = playing
        _isPlayingFlow.value = playing
    }

    /**
     * Plays the authentic vocal Adhan audio (Masjid an-Nabawi / Makkah vocal Adhan) for the specified prayer.
     * Starts AdhanService to guarantee continuous background playback and wake locks across all Android OS versions.
     */
    @Synchronized
    fun playAdhan(context: Context, prayerName: String, reminderStep: Int = 0, onCompletion: (() -> Unit)? = null) {
        if (!isAdhanEnabled(context)) {
            Log.d(TAG, "Adhan sound is disabled in settings.")
            return
        }

        if (!shouldPlayAdhanForPrayer(prayerName)) {
            Log.d(TAG, "Adhan not scheduled for $prayerName (Sunrise/Sunset or excluded).")
            return
        }

        // Delegate entire audio playback to AdhanService (foreground service)
        // This ensures solid background & foreground audio without duplicate MediaPlayer instances
        AdhanService.startAdhan(context, prayerName, reminderStep)
        _isPlaying = true
        _isPlayingFlow.value = true
    }

    /**
     * Manually plays or pauses the Adhan audio anytime the user wishes to listen.
     */
    @Synchronized
    fun toggleManualPlayAdhan(context: Context, onCompletion: (() -> Unit)? = null) {
        if (_isPlaying) {
            stopAdhan(context)
        } else {
            playAudioInternal(context, onCompletion)
        }
    }

    private fun acquireWakeLock(context: Context) {
        try {
            if (wakeLock == null) {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FaithShield:AdhanWakeLock")
            }
            wakeLock?.let {
                if (!it.isHeld) {
                    it.acquire(5 * 60 * 1000L) // 5 minutes max
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring WakeLock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing WakeLock", e)
        }
    }

    /**
     * Creates and prepares a MediaPlayer for the selected Adhan audio with robust fallbacks.
     */
    fun createMediaPlayer(context: Context, resId: Int = getSelectedAdhanRes(context)): MediaPlayer? {
        // Method 1: Standard MediaPlayer.create (Android's official way to play res/raw audio)
        // NOTE: MediaPlayer.create already prepares the player and sets default audio attributes.
        // Never call setAudioAttributes() after MediaPlayer.create() as it throws IllegalStateException.
        try {
            val mp = MediaPlayer.create(context, resId)
            if (mp != null) {
                Log.d(TAG, "MediaPlayer created successfully via MediaPlayer.create(resId: $resId)")
                return mp
            }
        } catch (e: Throwable) {
            Log.w(TAG, "MediaPlayer.create(resId: $resId) failed: ${e.message}")
        }

        // Method 2: Open Raw Resource File Descriptor directly
        try {
            val afd = context.resources.openRawResourceFd(resId)
            if (afd != null) {
                val mp = MediaPlayer()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val audioAttr = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                    mp.setAudioAttributes(audioAttr)
                } else {
                    @Suppress("DEPRECATION")
                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC)
                }
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                mp.prepare()
                try { afd.close() } catch (_: Throwable) {}
                Log.d(TAG, "MediaPlayer created successfully via openRawResourceFd")
                return mp
            }
        } catch (e: Throwable) {
            Log.w(TAG, "openRawResourceFd fallback failed: ${e.message}")
        }

        // Method 3: Copy to app internal cache directory (100% resilient across all Android devices/OEMs)
        try {
            val cacheFile = java.io.File(context.cacheDir, "adhan_audio_${resId}.mp3")
            if (!cacheFile.exists() || cacheFile.length() < 1024L) {
                context.resources.openRawResource(resId).use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            if (cacheFile.exists() && cacheFile.length() > 0L) {
                val mp = MediaPlayer()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val audioAttr = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                    mp.setAudioAttributes(audioAttr)
                } else {
                    @Suppress("DEPRECATION")
                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC)
                }
                mp.setDataSource(cacheFile.absolutePath)
                mp.prepare()
                Log.d(TAG, "MediaPlayer created successfully via cache file: ${cacheFile.absolutePath}")
                return mp
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Cache file playback fallback failed: ${e.message}")
        }

        // Ultimate fallback to adhan_madinah default
        try {
            val fallbackMp = MediaPlayer.create(context, R.raw.adhan_madinah)
            if (fallbackMp != null) {
                Log.d(TAG, "MediaPlayer created via fallback to R.raw.adhan_madinah")
                return fallbackMp
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Fallback to R.raw.adhan_madinah failed", e)
        }

        return null
    }

    private fun playAudioInternal(context: Context, onCompletion: (() -> Unit)? = null) {
        stopAdhan()
        acquireWakeLock(context)

        try {
            Log.d(TAG, "Starting authentic vocal Adhan audio playback with active audio focus...")
            
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val focusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
                Log.d(TAG, "AdhanPlayer focus changed: $focusChange")
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                    val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .setAudioAttributes(audioAttributes)
                        .setOnAudioFocusChangeListener(focusListener)
                        .build()
                    audioManager?.requestAudioFocus(focusRequest)
                } else {
                    @Suppress("DEPRECATION")
                    audioManager?.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Audio focus request failed: ${e.message}")
            }

            // Ensure media volume is audible
            try {
                audioManager?.let { am ->
                    val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val currentVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                    if (currentVol < (maxVol * 0.5f).toInt()) {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, (maxVol * 0.85f).toInt(), 0)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not adjust media volume", e)
            }

            val player = createMediaPlayer(context)

            player?.let { mp ->
                mp.setVolume(1.0f, 1.0f)
                mp.setOnCompletionListener {
                    _isPlaying = false
                    _isPlayingFlow.value = false
                    releaseWakeLock()
                    it.release()
                    mediaPlayer = null
                    onCompletion?.invoke()
                    Log.d(TAG, "Authentic vocal Adhan playback completed.")
                }
                mp.setOnErrorListener { itMp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    _isPlaying = false
                    _isPlayingFlow.value = false
                    releaseWakeLock()
                    itMp.release()
                    mediaPlayer = null
                    onCompletion?.invoke()
                    true
                }
                mp.start()
                _isPlaying = true
                _isPlayingFlow.value = true
                mediaPlayer = mp
                Log.d(TAG, "Authentic vocal Adhan started playing successfully!")
            } ?: run {
                Log.e(TAG, "Failed to instantiate MediaPlayer with R.raw.adhan_audio")
                _isPlaying = false
                _isPlayingFlow.value = false
                releaseWakeLock()
                onCompletion?.invoke()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating Adhan MediaPlayer", e)
            _isPlaying = false
            _isPlayingFlow.value = false
            releaseWakeLock()
            onCompletion?.invoke()
        }
    }

    @Synchronized
    fun stopAdhan(context: Context? = null) {
        try {
            context?.let { AdhanService.stopAdhan(it) }
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
            _isPlaying = false
            _isPlayingFlow.value = false
            releaseWakeLock()
            Log.d(TAG, "Adhan audio playback stopped.")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping Adhan audio", e)
            _isPlaying = false
            _isPlayingFlow.value = false
            releaseWakeLock()
        }
    }
}
