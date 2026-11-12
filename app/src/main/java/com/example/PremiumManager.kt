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

    data class PremiumTimeInfo(
        val isPremium: Boolean,
        val isLifetime: Boolean,
        val daysLeft: Long,
        val hoursLeft: Long,
        val minsLeft: Long,
        val isExpiringSoon: Boolean,
        val formattedExpiryDate: String,
        val displayTextBn: String,
        val displayTextEn: String
    )

    fun getPremiumTimeInfo(isPremium: Boolean, expiryTimestamp: Long): PremiumTimeInfo {
        if (!isPremium) {
            return PremiumTimeInfo(
                isPremium = false,
                isLifetime = false,
                daysLeft = 0,
                hoursLeft = 0,
                minsLeft = 0,
                isExpiringSoon = false,
                formattedExpiryDate = "",
                displayTextBn = "ফ্রি মোড",
                displayTextEn = "Free Mode"
            )
        }

        val now = System.currentTimeMillis()
        if (expiryTimestamp <= 0L || (expiryTimestamp - now) > 3650L * 24 * 60 * 60 * 1000L) {
            return PremiumTimeInfo(
                isPremium = true,
                isLifetime = true,
                daysLeft = 9999,
                hoursLeft = 9999,
                minsLeft = 9999,
                isExpiringSoon = false,
                formattedExpiryDate = "Lifetime",
                displayTextBn = "👑 আজীবন প্রো মেম্বারশিপ সক্রিয়",
                displayTextEn = "👑 Lifetime Pro Membership Active"
            )
        }

        val diffMs = expiryTimestamp - now
        if (diffMs <= 0L) {
            return PremiumTimeInfo(
                isPremium = false,
                isLifetime = false,
                daysLeft = 0,
                hoursLeft = 0,
                minsLeft = 0,
                isExpiringSoon = false,
                formattedExpiryDate = "",
                displayTextBn = "মেয়াদ শেষ হয়েছে",
                displayTextEn = "Subscription Expired"
            )
        }

        val daysLeft = diffMs / (24 * 60 * 60 * 1000L)
        val hoursLeft = (diffMs % (24 * 60 * 60 * 1000L)) / (60 * 60 * 1000L)
        val minsLeft = (diffMs % (60 * 60 * 1000L)) / (60 * 1000L)
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        val expiryDateStr = try {
            sdf.format(java.util.Date(expiryTimestamp))
        } catch (e: Exception) {
            ""
        }

        val isExpiringSoon = daysLeft <= 3

        val bnText = when {
            isExpiringSoon && daysLeft == 0L -> "⚠️ মেয়াদের বাকি মাত্র $hoursLeft ঘণ্টা $minsLeft মিনিট (শেষ: $expiryDateStr)"
            isExpiringSoon -> "⚠️ মেয়াদের বাকি মাত্র $daysLeft দিন (মেয়াদ শেষ: $expiryDateStr)"
            else -> "👑 প্রো অ্যাক্টিভ (বাকি আছে: $daysLeft দিন / মেয়াদ: $expiryDateStr)"
        }

        val enText = when {
            isExpiringSoon && daysLeft == 0L -> "⚠️ Expiring in $hoursLeft hours $minsLeft mins (Expires: $expiryDateStr)"
            isExpiringSoon -> "⚠️ Expiring in $daysLeft days (Expires: $expiryDateStr)"
            else -> "👑 Pro Active ($daysLeft days remaining / Expires: $expiryDateStr)"
        }

        return PremiumTimeInfo(
            isPremium = true,
            isLifetime = false,
            daysLeft = daysLeft,
            hoursLeft = hoursLeft,
            minsLeft = minsLeft,
            isExpiringSoon = isExpiringSoon,
            formattedExpiryDate = expiryDateStr,
            displayTextBn = bnText,
            displayTextEn = enText
        )
    }
}
