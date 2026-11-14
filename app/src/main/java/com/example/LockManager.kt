package com.example

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import java.security.MessageDigest

sealed class ResetResult {
    data class Success(val newGuardianKey: String) : ResetResult()
    data class InvalidKey(val attemptsRemaining: Int) : ResetResult()
    data class LockedOut(val remainingMinutes: Long) : ResetResult()
    object InvalidNewPin : ResetResult()
}

object LockManager {
    private const val PREFS_NAME = "AppLockPrefs"
    private const val KEY_PIN = "security_pin"
    private const val KEY_PIN_HASH = "security_pin_hash"
    private const val KEY_PIN_SALT = "security_pin_salt"
    private const val KEY_PIN_PERMANENTLY_LOCKED = "security_pin_permanently_locked"
    private const val KEY_GUARDIAN_KEY = "guardian_recovery_key"
    private const val KEY_FAILED_RESET_ATTEMPTS = "failed_reset_attempts"
    private const val KEY_RESET_LOCKOUT_UNTIL = "reset_lockout_until_time"
    private const val KEY_UNLOCKED_UNTIL = "unlocked_until_time"

    private fun getPrefs(context: Context): SharedPreferences {
        val defaultPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val protectedContext = context.createDeviceProtectedStorageContext()
            val protectedPrefs = protectedContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val defHash = defaultPrefs.getString(KEY_PIN_HASH, null)
            val protHash = protectedPrefs.getString(KEY_PIN_HASH, null)
            if (protHash.isNullOrBlank() && !defHash.isNullOrBlank()) {
                val defSalt = defaultPrefs.getString(KEY_PIN_SALT, null)
                protectedPrefs.edit()
                    .putString(KEY_PIN_HASH, defHash)
                    .putString(KEY_PIN_SALT, defSalt)
                    .putBoolean(KEY_PIN_PERMANENTLY_LOCKED, true)
                    .apply()
            }
            return protectedPrefs
        }
        return defaultPrefs
    }

    /**
     * Converts any Bangla digits (০-৯) to standard English digits (0-9)
     * and trims whitespace.
     */
    fun normalizeDigits(input: String): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        var res = input.trim()
        for (i in 0..9) {
            res = res.replace(bnDigits[i], ('0' + i))
        }
        return res
    }

    /**
     * Computes SHA-256 hex digest of "<saltToken>:<cleanPin>".
     * Matches the exact Web Crypto SHA-256 computation in approve.html.
     */
    fun computeSaltedPinHash(pin: String, saltToken: String): String {
        val cleanPin = normalizeDigits(pin)
        val cleanSalt = saltToken.trim()
        val payload = "$cleanSalt:$cleanPin"
        val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Applies the partner-submitted SHA-256 PIN hash and one-time request token salt.
     * Immediately invalidates any previous PIN hash or legacy plain-text PIN.
     * The plain-text PIN is NEVER stored or visible anywhere in the app.
     */
    fun applyPartnerPinHash(context: Context, pinHash: String, saltToken: String): Boolean {
        val cleanHash = pinHash.trim().lowercase()
        val cleanSalt = saltToken.trim()
        if (cleanHash.length != 64 || !cleanHash.all { it in '0'..'9' || it in 'a'..'f' } || cleanSalt.isEmpty()) {
            return false
        }

        try {
            val defaultPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            defaultPrefs.edit()
                .putString(KEY_PIN_HASH, cleanHash)
                .putString(KEY_PIN_SALT, cleanSalt)
                .remove(KEY_PIN)
                .remove(KEY_GUARDIAN_KEY)
                .putBoolean(KEY_PIN_PERMANENTLY_LOCKED, true)
                .putInt(KEY_FAILED_RESET_ATTEMPTS, 0)
                .putLong(KEY_RESET_LOCKOUT_UNTIL, 0L)
                .commit()
        } catch (e: Exception) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val protectedPrefs = context.createDeviceProtectedStorageContext()
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                protectedPrefs.edit()
                    .putString(KEY_PIN_HASH, cleanHash)
                    .putString(KEY_PIN_SALT, cleanSalt)
                    .remove(KEY_PIN)
                    .remove(KEY_GUARDIAN_KEY)
                    .putBoolean(KEY_PIN_PERMANENTLY_LOCKED, true)
                    .putInt(KEY_FAILED_RESET_ATTEMPTS, 0)
                    .putLong(KEY_RESET_LOCKOUT_UNTIL, 0L)
                    .commit()
            } catch (e: Exception) {}
        }
        return true
    }

    /**
     * Self-setting or overwriting the PIN in the app is strictly forbidden in v1.3.6+.
     * Only the accountability partner can set or reset the PIN via the tokenized approval link.
     */
    fun setPin(context: Context, pin: String): Pair<Boolean, String?> {
        return Pair(false, null)
    }

    /**
     * Never exposes the PIN in plain text.
     */
    fun getPin(context: Context): String? {
        return null
    }

    fun getPinHash(context: Context): String? {
        val prefs = getPrefs(context)
        var hash = prefs.getString(KEY_PIN_HASH, null)
        if (hash.isNullOrBlank()) {
            val defaultPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            hash = defaultPrefs.getString(KEY_PIN_HASH, null)
        }
        return hash?.trim()?.takeIf { it.length == 64 }
    }

    fun getPinSalt(context: Context): String? {
        val prefs = getPrefs(context)
        var salt = prefs.getString(KEY_PIN_SALT, null)
        if (salt.isNullOrBlank()) {
            val defaultPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            salt = defaultPrefs.getString(KEY_PIN_SALT, null)
        }
        return salt?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun getLegacyPlainPin(context: Context): String? {
        val prefs = getPrefs(context)
        var pin = prefs.getString(KEY_PIN, null)
        if (pin.isNullOrBlank()) {
            val defaultPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            pin = defaultPrefs.getString(KEY_PIN, null)
        }
        return if (!pin.isNullOrBlank()) normalizeDigits(pin) else null
    }

    fun hasPin(context: Context): Boolean {
        if (!getPinHash(context).isNullOrBlank() && !getPinSalt(context).isNullOrBlank()) {
            return true
        }
        return !getLegacyPlainPin(context).isNullOrBlank()
    }

    fun isPinPermanentlyLocked(context: Context): Boolean {
        return hasPin(context)
    }

    /**
     * Disabled: the user can never clear the PIN to set one themselves.
     * Only applyPartnerPinHash() updates the PIN when the partner submits pinHash.
     */
    fun clearPinAfterPartnerApproval(context: Context) {
        // No-op: self-setting a PIN after clearing is closed in v1.3.6+.
    }

    fun getGuardianKey(context: Context): String {
        return ""
    }

    fun getResetLockoutRemainingMinutes(context: Context): Long {
        return 0L
    }

    fun getFailedAttemptsRemaining(context: Context): Int {
        return 0
    }

    /**
     * Disabled: in-app Guardian Key reset is permanently closed so the user can never reset the PIN alone.
     */
    fun resetPinWithGuardianKey(context: Context, guardianKeyInput: String, newPin: String): ResetResult {
        return ResetResult.InvalidKey(0)
    }

    fun shareGuardianKey(context: Context, guardianKey: String) {
        // No-op in v1.3.6+
    }

    fun verifyPin(context: Context, input: String): Boolean {
        val cleanInput = normalizeDigits(input)
        if (cleanInput.length !in 4..6 || !cleanInput.all { it.isDigit() }) {
            return false
        }

        val savedHash = getPinHash(context)
        val savedSalt = getPinSalt(context)
        if (!savedHash.isNullOrBlank() && !savedSalt.isNullOrBlank()) {
            val candidateHash = computeSaltedPinHash(cleanInput, savedSalt)
            return candidateHash.equals(savedHash, ignoreCase = true)
        }

        val legacyPin = getLegacyPlainPin(context)
        if (!legacyPin.isNullOrBlank()) {
            return legacyPin == cleanInput
        }
        return false
    }

    @Volatile
    private var unlockedUntilElapsedRealtime: Long = 0L

    @Volatile
    private var pinScreenVisible: Boolean = false

    @Volatile
    private var pinScreenLaunchGraceUntilElapsed: Long = 0L

    /**
     * Marks that the guard is launching PinLockActivity right now so the guard
     * immediately pauses auto-back during the launch transition and never backs out of its own PIN screen.
     */
    fun markPinScreenLaunching() {
        pinScreenLaunchGraceUntilElapsed = android.os.SystemClock.elapsedRealtime() + 1800L
    }

    /**
     * Updates whether PinLockActivity is actively displayed on screen.
     */
    fun markPinScreenActive(active: Boolean) {
        pinScreenVisible = active
        if (!active) {
            pinScreenLaunchGraceUntilElapsed = 0L
        }
    }

    /**
     * Returns true whenever PinLockActivity is launching or visible on screen.
     * While true, ProtectionService must pause auto-back completely.
     */
    fun isPinScreenCurrentlyActive(): Boolean {
        if (pinScreenVisible) return true
        val now = android.os.SystemClock.elapsedRealtime()
        return pinScreenLaunchGraceUntilElapsed > now && (pinScreenLaunchGraceUntilElapsed - now) <= 2000L
    }

    fun unlockForMinutes(context: Context, minutes: Int) {
        val clampedSeconds = (minutes * 60).coerceIn(1, 60)
        unlockForSeconds(context, clampedSeconds)
    }

    fun unlockForSeconds(context: Context, seconds: Int) {
        val safeSeconds = seconds.coerceIn(1, 60)
        val nowWall = System.currentTimeMillis()
        val unlockWallTime = nowWall + (safeSeconds * 1000L)
        unlockedUntilElapsedRealtime = android.os.SystemClock.elapsedRealtime() + (safeSeconds * 1000L)
        pinScreenVisible = false
        pinScreenLaunchGraceUntilElapsed = 0L
        try {
            getPrefs(context).edit().putLong(KEY_UNLOCKED_UNTIL, unlockWallTime).apply()
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putLong(KEY_UNLOCKED_UNTIL, unlockWallTime).apply()
        } catch (e: Exception) {}
    }

    fun isCurrentlyUnlocked(context: Context): Boolean {
        val nowElapsed = android.os.SystemClock.elapsedRealtime()
        if (unlockedUntilElapsedRealtime > 0L) {
            val diffElapsed = unlockedUntilElapsedRealtime - nowElapsed
            if (diffElapsed in 1L..65_000L) {
                return true
            } else {
                unlockedUntilElapsedRealtime = 0L
            }
        }

        val prefs = getPrefs(context)
        val unlockedUntilWall = prefs.getLong(KEY_UNLOCKED_UNTIL, 0L)
        if (unlockedUntilWall <= 0L) return false

        val nowWall = System.currentTimeMillis()
        val remainingWallMs = unlockedUntilWall - nowWall

        // If clock was moved backwards or remaining time exceeds 65 seconds, immediately invalidate!
        if (remainingWallMs <= 0L || remainingWallMs > 65_000L) {
            lockImmediately(context)
            return false
        }
        return true
    }

    fun lockImmediately(context: Context) {
        unlockedUntilElapsedRealtime = 0L
        try {
            getPrefs(context).edit().putLong(KEY_UNLOCKED_UNTIL, 0L).apply()
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putLong(KEY_UNLOCKED_UNTIL, 0L).apply()
        } catch (e: Exception) {}
    }
}
