package com.example

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Date
import java.util.Calendar

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == "PRAYER_TIME_ACTION" || action == "PRAYER_REMINDER_ACTION") {
            val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Prayer"
            val isReminder = action == "PRAYER_REMINDER_ACTION"
            
            // Map prayer name to index
            val prayerIndex = when (prayerName.lowercase()) {
                "fajr" -> 0
                "dhuhr" -> 1
                "asr" -> 2
                "maghrib" -> 3
                "isha" -> 4
                else -> -1
            }
            
            // Check if user already marked it as done
            val prefs = context.getSharedPreferences("salah_prefs", Context.MODE_PRIVATE)
            val cal = Calendar.getInstance()
            val dayKey = "salah_" + "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.DAY_OF_YEAR)}"
            val bitmask = prefs.getInt(dayKey, 0)
            
            val isChecked = if (prayerIndex != -1) (bitmask and (1 shl prayerIndex)) != 0 else false
            
            if (isChecked && isReminder) {
                // If already checked, don't show reminder
                return
            }
            
            val notificationId = prayerName.hashCode() + (if (isReminder) 1 else 0)
            
            val contentTitle = if (isReminder) {
                "$prayerName Reminder"
            } else {
                "Time for $prayerName"
            }
            
            val contentText = if (isReminder) {
                "35 minutes have passed since $prayerName time started. Please try to pray."
            } else {
                "It is now time for $prayerName prayer."
            }
            
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(context, notificationId, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            
            val notif = NotificationCompat.Builder(context, "prayer_notifications")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
                
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(notificationId, notif)
            
            // If this was the main prayer alarm, schedule the reminder alarm
            if (!isReminder) {
                PrayerScheduler.schedulePrayerReminder(context, prayerName, 35)
            }
            
            // Also re-schedule the next day's prayers 
            // We can do this safely here since it's cheap
            PrayerScheduler.scheduleDailyPrayers(context)
        }
    }
}
