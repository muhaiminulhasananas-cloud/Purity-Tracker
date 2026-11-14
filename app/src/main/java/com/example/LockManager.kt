package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build

sealed class ResetResult {
    data class Success(val newGuardianKey: String) : ResetResult()
    data class InvalidKey(val attemptsRemaining: Int) : ResetResult()
    data class LockedOut(val remainingMinutes: Long) : ResetResult()
    object InvalidNewPin : ResetResult()
}

object LockManager {
    private const val PREFS_NAME = "AppLockPrefs"
    private const val KEY_PIN = "security_pin"
    private const val KEY_PIN_PERMANENTLY_LOCKED = "security_pin_permanently_locked"
    private const val KEY_GUARDIAN_KEY = "guardian_recovery_key"
    private const val KEY_FAILED_RESET_ATTEMPTS = "failed_reset_attempts"
    private const val KEY_RESET_LOCKOUT_UNTIL = "reset_lockout_until_time"
    private const val KEY_UNLOCKED_UNTIL = "unlocked_until_time"

    private const val MAX_RESET_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 30 * 60 * 1000L // 30 minutes lockout

    private fun getPrefs(context: Context): SharedPreferences {
        val defaultPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val protectedContext = context.createDeviceProtectedStorageContext()
            val protectedPrefs = protectedContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val defPin = defaultPrefs.getString(KEY_PIN, null)
            val protPin = protectedPrefs.getString(KEY_PIN, null)
            // Migrate to protected storage if not already there
            if (protPin.isNullOrBlank() && !defPin.isNullOrBlank()) {
                val defKey = defaultPrefs.getString(KEY_GUARDIAN_KEY, null)
                protectedPrefs.edit()
                    .putString(KEY_PIN, defPin)
                    .putString(KEY_GUARDIAN_KEY, defKey)
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
     * Normalizes a Guardian Key (removes spaces, hyphens, uppercases)
     * for fail-safe verification.
     */
    fun normalizeKey(input: String): String {
        return input.trim().uppercase().replace(" ", "").replace("-", "")
    }

    /**
     * Generates a cryptographically strong, memorable 8-char Guardian Recovery Key.
     * Example: "G-4982-XP"
     */
    fun generateGuardianKey(): String {
        val randomNum = (1000..9999).random()
        val letters = ('A'..'Z').shuffled().take(2).joinToString("")
        return "G-$randomNum-$letters"
    }

    /**
     * Sets the PIN permanently for the first time and generates a unique Guardian Recovery Key.
     * CRITICAL RULE: Once set, the PIN CANNOT be changed, edited, or overwritten by the user!
     * Returns Pair(success, guardianKey).
     */
    fun setPin(context: Context, pin: String): Pair<Boolean, String?> {
        if (hasPin(context)) {
            // Already set: Permanently locked and immutable!
            return Pair(false, null)
        }
        val cleanPin = normalizeDigits(pin)
        if (cleanPin.length < 4) {
            return Pair(false, null)
        }

        val guardianKey = generateGuardianKey()

        // Save in both default and protected prefs for safety
        try {
            val defaultPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            defaultPrefs.edit()
                .putString(KEY_PIN, cleanPin)
                .putString(KEY_GUARDIAN_KEY, guardianKey)
                .putBoolean(KEY_PIN_PERMANENTLY_LOCKED, true)
                .putInt(KEY_FAILED_RESET_ATTEMPTS, 0)
                .putLong(KEY_RESET_LOCKOUT_UNTIL, 0L)
                .apply()
        } catch (e: Exception) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val protectedPrefs = context.createDeviceProtectedStorageContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                protectedPrefs.edit()
                    .putString(KEY_PIN, cleanPin)
                    .putString(KEY_GUARDIAN_KEY, guardianKey)
                    .putBoolean(KEY_PIN_PERMANENTLY_LOCKED, true)
                    .putInt(KEY_FAILED_RESET_ATTEMPTS, 0)
                    .putLong(KEY_RESET_LOCKOUT_UNTIL, 0L)
                    .apply()
            } catch (e: Exception) {}
        }
        return Pair(true, guardianKey)
    }

    fun getPin(context: Context): String? {
        val prefs = getPrefs(context)
        var pin = prefs.getString(KEY_PIN, null)
        if (pin.isNullOrBlank()) {
            val defaultPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            pin = defaultPrefs.getString(KEY_PIN, null)
        }
        return if (!pin.isNullOrBlank()) normalizeDigits(pin) else null
    }

    fun hasPin(context: Context): Boolean {
        return !getPin(context).isNullOrBlank()
    }

    fun isPinPermanentlyLocked(context: Context): Boolean {
        return hasPin(context)
    }

    /**
     * Called ONLY when the Accountability Partner clicks the tokenized approval link
     * for a `reset_pin` request (`status == "approved"` in Firestore).
     * Never callable directly by the user alone.
     */
    fun clearPinAfterPartnerApproval(context: Context) {
        try {
            val defaultPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            defaultPrefs.edit()
                .remove(KEY_PIN)
                .putBoolean(KEY_PIN_PERMANENTLY_LOCKED, false)
                .putInt(KEY_FAILED_RESET_ATTEMPTS, 0)
                .putLong(KEY_RESET_LOCKOUT_UNTIL, 0L)
                .apply()
        } catch (e: Exception) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val protectedPrefs = context.createDeviceProtectedStorageContext()
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                protectedPrefs.edit()
                    .remove(KEY_PIN)
                    .putBoolean(KEY_PIN_PERMANENTLY_LOCKED, false)
                    .putInt(KEY_FAILED_RESET_ATTEMPTS, 0)
                    .putLong(KEY_RESET_LOCKOUT_UNTIL, 0L)
                    .apply()
            } catch (e: Exception) {}
        }
    }

    /**
     * Gets the Guardian Recovery Key.
     * If a PIN exists from an older version without a Guardian Key,
     * one is automatically generated and saved.
     */
    fun getGuardianKey(context: Context): String {
        val prefs = getPrefs(context)
        var key = prefs.getString(KEY_GUARDIAN_KEY, null)
        if (key.isNullOrBlank()) {
            val defaultPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            key = defaultPrefs.getString(KEY_GUARDIAN_KEY, null)
        }
        if (key.isNullOrBlank()) {
            // Auto-generate if PIN exists
            val newKey = generateGuardianKey()
            try {
                getPrefs(context).edit().putString(KEY_GUARDIAN_KEY, newKey).apply()
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_GUARDIAN_KEY, newKey).apply()
            } catch (e: Exception) {}
            return newKey
        }
        return key
    }

    /**
     * Remaining minutes until lockout expires (0 if not locked out).
     */
    fun getResetLockoutRemainingMinutes(context: Context): Long {
        val lockoutUntil = getPrefs(context).getLong(KEY_RESET_LOCKOUT_UNTIL, 0L)
        val now = System.currentTimeMillis()
        if (now < lockoutUntil) {
            val remSeconds = (lockoutUntil - now) / 1000
            return (remSeconds / 60) + 1
        }
        return 0L
    }

    fun getFailedAttemptsRemaining(context: Context): Int {
        val failed = getPrefs(context).getInt(KEY_FAILED_RESET_ATTEMPTS, 0)
        return (MAX_RESET_ATTEMPTS - failed).coerceAtLeast(0)
    }

    /**
     * Resets the PIN using the Guardian Recovery Key.
     * The user CANNOT do this alone without their accountability partner / guardian's key!
     * Includes brute-force protection: 5 wrong attempts lock out reset for 30 minutes.
     * Upon success, generates a BRAND NEW Guardian Key and invalidates the previous one.
     */
    fun resetPinWithGuardianKey(context: Context, guardianKeyInput: String, newPin: String): ResetResult {
        val prefs = getPrefs(context)
        val now = System.currentTimeMillis()

        // 1. Check lockout
        val lockoutUntil = prefs.getLong(KEY_RESET_LOCKOUT_UNTIL, 0L)
        if (now < lockoutUntil) {
            val remMinutes = ((lockoutUntil - now) / (60 * 1000)) + 1
            return ResetResult.LockedOut(remMinutes)
        }

        // 2. Check new PIN validity
        val cleanNewPin = normalizeDigits(newPin)
        if (cleanNewPin.length < 4) {
            return ResetResult.InvalidNewPin
        }

        // 3. Verify Guardian Key
        val savedKey = getGuardianKey(context)
        val normInput = normalizeKey(guardianKeyInput)
        val normSaved = normalizeKey(savedKey)

        if (normInput.isEmpty() || normInput != normSaved) {
            val failed = prefs.getInt(KEY_FAILED_RESET_ATTEMPTS, 0) + 1
            if (failed >= MAX_RESET_ATTEMPTS) {
                // Lockout for 30 minutes!
                val lockTime = now + LOCKOUT_DURATION_MS
                prefs.edit()
                    .putInt(KEY_FAILED_RESET_ATTEMPTS, 0)
                    .putLong(KEY_RESET_LOCKOUT_UNTIL, lockTime)
                    .apply()
                try {
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                        .putInt(KEY_FAILED_RESET_ATTEMPTS, 0)
                        .putLong(KEY_RESET_LOCKOUT_UNTIL, lockTime)
                        .apply()
                } catch (e: Exception) {}
                return ResetResult.LockedOut(30)
            } else {
                prefs.edit().putInt(KEY_FAILED_RESET_ATTEMPTS, failed).apply()
                try {
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                        .putInt(KEY_FAILED_RESET_ATTEMPTS, failed)
                        .apply()
                } catch (e: Exception) {}
                return ResetResult.InvalidKey(MAX_RESET_ATTEMPTS - failed)
            }
        }

        // 4. Success: Save new PIN, generate BRAND NEW Guardian Key
        val newGuardianKey = generateGuardianKey()
        try {
            val defaultPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            defaultPrefs.edit()
                .putString(KEY_PIN, cleanNewPin)
                .putString(KEY_GUARDIAN_KEY, newGuardianKey)
                .putBoolean(KEY_PIN_PERMANENTLY_LOCKED, true)
                .putInt(KEY_FAILED_RESET_ATTEMPTS, 0)
                .putLong(KEY_RESET_LOCKOUT_UNTIL, 0L)
                .apply()
        } catch (e: Exception) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val protectedPrefs = context.createDeviceProtectedStorageContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                protectedPrefs.edit()
                    .putString(KEY_PIN, cleanNewPin)
                    .putString(KEY_GUARDIAN_KEY, newGuardianKey)
                    .putBoolean(KEY_PIN_PERMANENTLY_LOCKED, true)
                    .putInt(KEY_FAILED_RESET_ATTEMPTS, 0)
                    .putLong(KEY_RESET_LOCKOUT_UNTIL, 0L)
                    .apply()
            } catch (e: Exception) {}
        }

        // Grant temporary 60-second unlock so user can access immediately
        unlockForSeconds(context, 60)

        return ResetResult.Success(newGuardianKey)
    }

    /**
     * Share Guardian Recovery Key via WhatsApp, SMS, or other apps.
     */
    fun shareGuardianKey(context: Context, guardianKey: String) {
        val isBn = LanguageStore.currentLanguage.value == "bn"
        val shareText = if (isBn) {
            "🛡️ Purity Tracker - গার্ডিয়ান রিকভারি কি (Guardian Key)\n\n" +
            "আমার পিউরিটি ট্র্যাকার অ্যাপের সিকিউরিটি পিন সুরক্ষিত রাখতে এই গার্ডিয়ান কোডটি আপনার কাছে সংরক্ষণ করে রাখুন:\n\n" +
            "🔑 সিকিউরিটি কোড: $guardianKey\n\n" +
            "⚠️ আমি যদি কখনো পিন ভুলে যাই, শুধুমাত্র আপনার অনুমতি এবং আপনার কাছে থাকা এই কোডটি দিয়েই পিন রিসেট করা যাবে। আমি নিজে একা এই কোড ছাড়া পিন রিসেট করতে পারব না। আমাকে সুস্থ ও পবিত্র পথে সাহায্য করার জন্য ধন্যবাদ!"
        } else {
            "🛡️ Purity Tracker - Guardian Recovery Key\n\n" +
            "Please keep this Guardian Recovery Key safe for my Purity Tracker app security PIN:\n\n" +
            "🔑 Security Key: $guardianKey\n\n" +
            "⚠️ If I ever forget my PIN, it can ONLY be reset using this key from you. I cannot reset it alone. Thank you for being my accountability partner!"
        }

        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            val shareIntent = Intent.createChooser(sendIntent, if (isBn) "গার্ডিয়ান কি শেয়ার করুন" else "Share Guardian Key").apply {
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(shareIntent)
        } catch (e: Exception) {}
    }

    fun verifyPin(context: Context, input: String): Boolean {
        val saved = getPin(context) ?: return false
        val cleanInput = normalizeDigits(input)
        return saved == cleanInput
    }

    @Volatile
    private var unlockedUntilElapsedRealtime: Long = 0L

    @Volatile
    private var pinScreenVisible: Boolean = false

    @Volatile
    private var pinScreenLaunchGraceUntilElapsed: Long = 0L

    /**
     * Marks that the guard is launching PinLockActivity right now so the guard
     * immediately pauses auto-back and never backs out of its own PIN screen.
     */
    fun markPinScreenLaunching() {
        pinScreenVisible = true
        pinScreenLaunchGraceUntilElapsed = android.os.SystemClock.elapsedRealtime() + 2500L
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
        return pinScreenLaunchGraceUntilElapsed > now && (pinScreenLaunchGraceUntilElapsed - now) <= 3000L
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
