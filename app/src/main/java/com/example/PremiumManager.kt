package com.example

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PremiumManager {
    private const val PREFS_NAME = "warrior_premium_prefs"
    private const val KEY_IS_PREMIUM = "is_warrior_premium"
    private const val KEY_EXPIRY_TIMESTAMP = "warrior_premium_expiry_v1"
    private const val KEY_MSG_COUNT = "daily_msg_count_v1"
    private const val KEY_LAST_TRACK_DATE = "last_track_date"

    private val _isPremium = MutableStateFlow(false)
    val isPremium = _isPremium.asStateFlow()

    private val _premiumExpiryTimestamp = MutableStateFlow(0L)
    val premiumExpiryTimestamp = _premiumExpiryTimestamp.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isPrem = prefs.getBoolean(KEY_IS_PREMIUM, false)
        val expiry = prefs.getLong(KEY_EXPIRY_TIMESTAMP, 0L)
        if (isPrem && expiry != 0L && System.currentTimeMillis() > expiry) {
            prefs.edit().putBoolean(KEY_IS_PREMIUM, false).putLong(KEY_EXPIRY_TIMESTAMP, 0L).apply()
            _isPremium.value = false
            _premiumExpiryTimestamp.value = 0L
        } else {
            _isPremium.value = isPrem
            _premiumExpiryTimestamp.value = expiry
        }
    }

    fun setPremiumStatus(context: Context, enabled: Boolean, expiry: Long = 0L) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_IS_PREMIUM, enabled)
            .putLong(KEY_EXPIRY_TIMESTAMP, expiry)
            .apply()
        _isPremium.value = enabled
        _premiumExpiryTimestamp.value = expiry
    }

    fun getDailyMsgCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        checkAndResetDailyMsgCount(context)
        return prefs.getInt(KEY_MSG_COUNT, 0)
    }

    fun incrementDailyMsgCount(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        checkAndResetDailyMsgCount(context)
        val current = prefs.getInt(KEY_MSG_COUNT, 0)
        prefs.edit().putInt(KEY_MSG_COUNT, current + 1).apply()
    }

    private fun checkAndResetDailyMsgCount(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val lastDate = prefs.getString(KEY_LAST_TRACK_DATE, "")
        if (todayStr != lastDate) {
            prefs.edit()
                .putString(KEY_LAST_TRACK_DATE, todayStr)
                .putInt(KEY_MSG_COUNT, 0)
                .apply()
        }
    }
}
