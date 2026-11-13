package com.example

import android.content.Context
import android.util.Log

object GuardianManager {
    private var lastAlertTime = 0L

    fun triggerGuardianAlert(context: Context, reason: String) {
        val prefs = context.applicationContext.getSharedPreferences("GuardianPrefs", Context.MODE_PRIVATE)
        val isShieldActive = prefs.getBoolean("isShieldActive", false)
        if (!isShieldActive) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAlertTime < 30_000) return
        lastAlertTime = currentTime

        val userName = prefs.getString("userName", "User") ?: "User"
        val guardianEmail = prefs.getString("guardianEmail", "") ?: ""

        if (guardianEmail.isNotEmpty()) {
            // Logs to Firestore. You can configure Firebase Trigger Email Extension later to listen to the "mail" collection.
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val alertData = hashMapOf(
                    "userName" to userName,
                    "guardianEmail" to guardianEmail,
                    "timestamp" to System.currentTimeMillis(),
                    "reason" to reason,
                    "status" to "pending"
                )
                db.collection("guardian_alerts").add(alertData)
                
                val mailData = hashMapOf(
                    "to" to guardianEmail,
                    "message" to hashMapOf(
                        "subject" to "⚠️ URGENT: $userName Needs Your Help!",
                        "html" to "Hello,<br><br>This is an automated alert from Purity Tracker. <b>$userName</b> has just $reason.<br><br>Please check in with them immediately to offer support and ensure they stay on the right path.<br><br>Thank you,<br>Guardian Protection Shield"
                    )
                )
                db.collection("mail").add(mailData)
                Log.d("GuardianManager", "Alert queued to Firestore 'mail' collection for future processing.")
            } catch (e: Exception) {
                Log.e("GuardianManager", "Firestore error: ${e.message}")
            }
        }
    }

    fun sendGuardianRecoveryKeyEmail(context: Context, guardianKey: String) {
        val prefs = context.applicationContext.getSharedPreferences("GuardianPrefs", Context.MODE_PRIVATE)
        val userName = prefs.getString("userName", "User") ?: "User"
        val guardianEmail = prefs.getString("guardianEmail", "") ?: ""
        if (guardianEmail.isEmpty()) return
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val mailData = hashMapOf(
                "to" to guardianEmail,
                "message" to hashMapOf(
                    "subject" to "🔑 Purity Tracker — Guardian Recovery Key for $userName",
                    "html" to "Hello,<br><br><b>$userName</b>'s 6-digit Guardian Recovery Key for Purity Tracker PIN reset is:<br><br><h2 style='letter-spacing:4px;'>$guardianKey</h2><br>Keep this key safe and only share it if you approve resetting their PIN.<br><br>Thank you,<br>Purity Tracker"
                )
            )
            db.collection("mail").add(mailData)
        } catch (e: Exception) {}
    }
}
