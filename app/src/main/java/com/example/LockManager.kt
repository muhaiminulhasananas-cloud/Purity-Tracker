package com.example

import android.content.Context
import android.content.SharedPreferences

object LockManager {
    private const val PREFS_NAME = "AppLockPrefs"
    private const val KEY_PIN = "security_pin"
    private const val KEY_UNLOCKED_UNTIL = "unlocked_until_time"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setPin(context: Context, pin: String) {
        getPrefs(context).edit().putString(KEY_PIN, pin).apply()
    }

    fun getPin(context: Context): String? {
        return getPrefs(context).getString(KEY_PIN, null)
    }

    fun hasPin(context: Context): Boolean {
        return !getPin(context).isNullOrEmpty()
    }

    fun unlockForMinutes(context: Context, minutes: Int) {
        val unlockTime = System.currentTimeMillis() + (minutes * 60 * 1000)
        getPrefs(context).edit().putLong(KEY_UNLOCKED_UNTIL, unlockTime).apply()
    }

    fun unlockForSeconds(context: Context, seconds: Int) {
        val unlockTime = System.currentTimeMillis() + (seconds * 1000)
        getPrefs(context).edit().putLong(KEY_UNLOCKED_UNTIL, unlockTime).apply()
    }

    fun isCurrentlyUnlocked(context: Context): Boolean {
        val unlockedUntil = getPrefs(context).getLong(KEY_UNLOCKED_UNTIL, 0L)
        return System.currentTimeMillis() < unlockedUntil
    }

    fun lockImmediately(context: Context) {
        getPrefs(context).edit().putLong(KEY_UNLOCKED_UNTIL, 0L).apply()
    }
}
