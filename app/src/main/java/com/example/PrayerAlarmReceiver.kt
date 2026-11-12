package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Calendar

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == "STOP_ADHAN_ACTION") {
            Log.d("PrayerAlarmReceiver", "Stopping Adhan on user action.")
            AdhanPlayer.stopAdhan(context)
            val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)
            if (notificationId != -1) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(notificationId)
            }
            return
        }

        if (action == "PRAYER_TIME_ACTION" || action == "PRAYER_REMINDER_ACTION") {
            val pendingResult = goAsync()
            try {
                processPrayerAlarm(context, intent)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun processPrayerAlarm(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Prayer"
        val reminderStep = intent.getIntExtra("REMINDER_STEP", 0)
        val isReminder = action == "PRAYER_REMINDER_ACTION"
        
        val isSunrise = prayerName.equals("Sunrise", ignoreCase = true)
        val isSunset = prayerName.equals("Sunset", ignoreCase = true)
        val isTahajjud = prayerName.equals("Tahajjud", ignoreCase = true)

        // Map prayer name to index in SalahViewModel bitmask
        val prayerIndex = when (prayerName.lowercase()) {
            "fajr" -> 0
            "dhuhr" -> 1
            "asr" -> 2
            "maghrib" -> 3
            "isha" -> 4
            "tahajjud" -> 5
            else -> -1
        }

        val prayerNameBn = when (prayerName.lowercase()) {
            "fajr" -> "ফজর"
            "dhuhr" -> "যোহর"
            "asr" -> "আসর"
            "maghrib" -> "মাগরিব"
            "isha" -> "ইশা"
            "tahajjud" -> "তাহাজ্জুদ"
            "sunrise" -> "সূর্যোদয়"
            "sunset" -> "সূর্যাস্ত"
            else -> prayerName
        }
        
        // Check if user already marked it as done in today's Salah tracker
        val prefs = context.getSharedPreferences("salah_prefs", Context.MODE_PRIVATE)
        val cal = Calendar.getInstance()
        val dayKey = "salah_" + "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.DAY_OF_YEAR)}"
        val bitmask = prefs.getInt(dayKey, 0)
        
        val isChecked = if (prayerIndex != -1) (bitmask and (1 shl prayerIndex)) != 0 else false
        
        // If already checked and this is a reminder notification, skip showing reminder
        if (isChecked && isReminder) {
            Log.d("PrayerAlarmReceiver", "$prayerName already completed by user. Skipping reminder.")
            return
        }
        
        val notificationId = prayerName.hashCode() * 31 + reminderStep
        
        // Customize Title & Content based on event type
        val contentTitle: String
        val contentText: String

        if (isTahajjud) {
            contentTitle = "🌙 শেষ রাতের তাহাজ্জুদ নামাজের সময় হয়েছে"
            contentText = "শেষ রাতের অত্যন্ত বরকতময় সময়! আল্লাহর নৈকট্য লাভ ও দোয়া কবুলের উদ্দেশ্যে তাহাজ্জুদ সালাত আদায় করুন।"
        } else if (isSunrise) {
            contentTitle = "🌅 সূর্যোদয় — সুবহে সাদিক সমাপ্ত ও সূর্য উদিত হচ্ছে"
            contentText = "সূর্য উদিত হওয়ার সময় এখন কোনো নামাজ আদায় করা নিষেধ। সূর্যোদয়ের ১৫ মিনিট পর সালাতুদ দুহা / ইশরাক নামাজ আদায় করতে পারবেন।"
        } else if (isSunset) {
            contentTitle = "🌇 সূর্যাস্ত — সূর্য অস্ত যাচ্ছে ও মাগরিবের প্রস্তুতি"
            contentText = "পশ্চিমাকাশে সূর্য অস্ত গিয়েছে। রোজাদারদের ইফতারের মুহূর্ত এবং মাগরিবের ফরজ নামাজের প্রস্তুতি নিন।"
        } else if (!isReminder || reminderStep == 0) {
            contentTitle = "🕌 $prayerNameBn নামাজের ওয়াক্ত হয়েছে (নামাজের সময়)"
            contentText = "পবিত্র $prayerNameBn নামাজের ওয়াক্ত শুরু হয়েছে। মধুর সুরে আজান শুনুন ও নামাজের প্রস্তুতি নিন।"
        } else {
            when (reminderStep) {
                25 -> {
                    contentTitle = "🔔 $prayerNameBn নামাজের তাগিদ (২৫ মিনিট পার)"
                    contentText = "আপনি কি আজকের $prayerNameBn নামাজ আদায় করেছেন? দেরি না করে জামাত বা ওয়াক্তের নামাজ পড়ে নিন।"
                }
                35 -> {
                    contentTitle = "⚠️ $prayerNameBn নামাজের ওয়াক্ত পার হচ্ছে (৩৫ মিনিট পার)"
                    contentText = "ওয়াক্তের ৩৫ মিনিট পার হয়ে গেছে! দ্রুত অজু করে $prayerNameBn নামাজ আদায় করে নিন।"
                }
                50 -> {
                    contentTitle = "⏳ $prayerNameBn শেষ সময় ঘনিয়ে এসেছে (৫০ মিনিট পার)"
                    contentText = "ওয়াক্তের শেষ সময় ঘনিয়ে এসেছে (৫০ মিনিট অতিবাহিত)! সব কাজ রেখে এখনই $prayerNameBn নামাজ সম্পন্ন করুন।"
                }
                else -> {
                    contentTitle = "🕌 $prayerNameBn নামাজের রিমাইন্ডার"
                    contentText = "ওয়াক্ত থাকতে থাকতে $prayerNameBn নামাজটি দ্রুত সম্পন্ন করুন।"
                }
            }
        }
        
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("stop_adhan", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            this.action = "STOP_ADHAN_ACTION"
            putExtra("NOTIFICATION_ID", notificationId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 9999, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Check if Adhan sound should be played for this notification:
        // Only the initial prayer notification (Step 0) plays the Adhan sound for the 5 daily prayers + Tahajjud
        val isEligiblePrayer = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha", "Tahajjud")
            .any { it.equals(prayerName, ignoreCase = true) }
        val shouldPlayAdhanSound = isEligiblePrayer && 
            (reminderStep == 0) && 
            AdhanPlayer.shouldPlayAdhanForPrayer(prayerName)

        val channelId = if (shouldPlayAdhanSound) "adhan_playback_channel_v4" else "prayer_reminder_channel_v4"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (shouldPlayAdhanSound) {
                val adhanChannel = NotificationChannel(
                    channelId,
                    "নামাজের আযান ও সালাত এলার্ট",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "নামাজের ওয়াক্তের সাথে সাথে রিয়েল আজান বাজানো"
                    setSound(null, null) // Let MediaPlayer handle the audio
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                nm.createNotificationChannel(adhanChannel)
            } else {
                val reminderChannel = NotificationChannel(
                    channelId,
                    "নামাজের রিমাইন্ডার নোটিফিকেশন",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "নামাজের পরবর্তী তাগিদ ও অনুস্মারক"
                    enableVibration(true)
                }
                nm.createNotificationChannel(reminderChannel)
            }
        }

        val notifBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setColor(0xFF0284C7.toInt())
            .setLargeIcon(NotificationHelper.getAppLogoBitmap(context))
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(stopPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        if (shouldPlayAdhanSound) {
            notifBuilder.addAction(
                android.R.drawable.ic_media_pause,
                "আযান থামান",
                stopPendingIntent
            )
            // Play authentic vocal Adhan sound through background foreground service
            AdhanPlayer.playAdhan(context, prayerName, reminderStep)
        }
            
        nm.notify(notificationId, notifBuilder.build())
        
        // Re-schedule upcoming daily prayers to keep alarms fresh
        PrayerScheduler.scheduleDailyPrayers(context)
    }
}
