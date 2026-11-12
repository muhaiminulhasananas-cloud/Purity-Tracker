package com.example

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.concurrent.TimeUnit

/**
 * Manages Facebook-style real-time user presence (Online / Last Active status).
 */
object UserPresenceManager {
    private const val TAG = "UserPresenceManager"
    private const val HEARTBEAT_INTERVAL_MS = 30_000L // Heartbeat every 30 seconds
    private const val ONLINE_THRESHOLD_MS = 75_000L // 75 seconds threshold to be considered actively online

    private val handler = Handler(Looper.getMainLooper())
    private var isHeartbeatRunning = false
    private var isAppInForeground = false
    @Volatile
    var isDeletingAccount = false

    private val heartbeatRunnable = object : Runnable {
        override fun run() {
            if (isAppInForeground && !isDeletingAccount) {
                updatePresence(isOnline = true)
                handler.postDelayed(this, HEARTBEAT_INTERVAL_MS)
            }
        }
    }

    fun onAppForeground() {
        try {
            if (isDeletingAccount) return
            isAppInForeground = true
            updatePresence(isOnline = true)
            if (!isHeartbeatRunning) {
                isHeartbeatRunning = true
                handler.removeCallbacks(heartbeatRunnable)
                handler.postDelayed(heartbeatRunnable, HEARTBEAT_INTERVAL_MS)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "onAppForeground error: ${e.message}")
        }
    }

    fun onAppBackground() {
        try {
            isAppInForeground = false
            isHeartbeatRunning = false
            handler.removeCallbacks(heartbeatRunnable)
            if (!isDeletingAccount) {
                updatePresence(isOnline = false)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "onAppBackground error: ${e.message}")
        }
    }

    fun updatePresence(isOnline: Boolean) {
        try {
            if (isDeletingAccount) return
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser ?: return
            if (!AccountDeletionManager.isUserVerified(user)) return
            val db = FirebaseFirestore.getInstance()

            val data = hashMapOf<String, Any>(
                "isOnline" to isOnline,
                "lastSeen" to System.currentTimeMillis()
            )

            // Use update() rather than set(merge) so deleted user documents are NEVER recreated
            db.collection("users").document(user.uid)
                .update(data)
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to update presence: ${e.message}")
                }
        } catch (e: Throwable) {
            Log.w(TAG, "updatePresence safe catch: ${e.message}")
        }
    }

    /**
     * Formats status into Facebook-style text:
     * - "Active now" / "সক্রিয় আছেন" (if online or last seen within threshold)
     * - "Active 2m ago" / "২ মিনিট আগে সক্রিয় ছিলেন"
     * - "Active 1h ago" / "১ ঘণ্টা আগে সক্রিয় ছিলেন"
     * - "Active yesterday" / "গতকাল সক্রিয় ছিলেন"
     * - etc.
     */
    fun formatPresenceStatus(isOnline: Boolean, lastSeen: Long, isBn: Boolean): PresenceDisplay {
        val now = System.currentTimeMillis()
        val diff = if (now > lastSeen && lastSeen > 0) now - lastSeen else 0L

        // Consider online if explicitly marked online AND lastSeen was within threshold
        // OR if lastSeen is within threshold (handles lag in status update)
        val isActivelyOnline = (isOnline && diff < ONLINE_THRESHOLD_MS) || (lastSeen > 0 && diff < ONLINE_THRESHOLD_MS)

        if (isActivelyOnline) {
            return PresenceDisplay(
                isOnline = true,
                statusText = if (isBn) "সক্রিয় আছেন" else "Active now"
            )
        }

        if (lastSeen <= 0L) {
            return PresenceDisplay(
                isOnline = false,
                statusText = if (isBn) "অফলাইন" else "Offline"
            )
        }

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        val statusText = when {
            minutes < 1 -> {
                if (isBn) "এইমাত্র সক্রিয় ছিলেন" else "Active just now"
            }
            minutes < 60 -> {
                if (isBn) "${convertDigitsToBn(minutes)} মিনিট আগে" else "Active ${minutes}m ago"
            }
            hours < 24 -> {
                if (isBn) "${convertDigitsToBn(hours)} ঘণ্টা আগে" else "Active ${hours}h ago"
            }
            days == 1L -> {
                if (isBn) "গতকাল সক্রিয় ছিলেন" else "Active yesterday"
            }
            days < 7 -> {
                if (isBn) "${convertDigitsToBn(days)} দিন আগে" else "Active ${days}d ago"
            }
            else -> {
                if (isBn) "অনেক আগে সক্রিয় ছিলেন" else "Active long ago"
            }
        }

        return PresenceDisplay(
            isOnline = false,
            statusText = statusText
        )
    }

    private fun convertDigitsToBn(number: Long): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val str = number.toString()
        val sb = StringBuilder()
        for (ch in str) {
            if (ch in '0'..'9') {
                sb.append(bnDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}

data class PresenceDisplay(
    val isOnline: Boolean,
    val statusText: String
)
