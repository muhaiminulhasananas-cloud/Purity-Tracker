package com.example

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WinterArcAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "winter_arc_reminders"
        const val CHANNEL_NAME = "Winter Arc Task & Water Reminders"

        const val EXTRA_REMINDER_TYPE = "reminder_type" // "task", "workout", "water"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
        const val EXTRA_TASK_TIME = "task_time"
        const val EXTRA_WORKOUT_SETS = "workout_sets"
        const val EXTRA_WORKOUT_REPS = "workout_reps"
        const val EXTRA_WATER_AMOUNT = "water_amount"
        const val EXTRA_WATER_INTERVAL = "water_interval"

        fun rescheduleAll(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = context.getSharedPreferences("winter_arc_prefs", Context.MODE_PRIVATE)
                    val isArcEnabled = prefs.getBoolean("arc_notifications_enabled", true)
                    if (!isArcEnabled) return@launch

                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return@launch
                    val nowMs = System.currentTimeMillis()

                    // 1. Reschedule Smart Water Slots
                    val isWaterEnabled = prefs.getBoolean("water_reminder_enabled", true)
                    if (isWaterEnabled) {
                        smartWaterSlots.forEach { slot ->
                            val targetCal = java.util.Calendar.getInstance().apply {
                                set(java.util.Calendar.HOUR_OF_DAY, slot.hour)
                                set(java.util.Calendar.MINUTE, slot.minute)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                                if (timeInMillis <= nowMs) {
                                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                                }
                            }

                            val intent = Intent(context, WinterArcAlarmReceiver::class.java).apply {
                                putExtra(EXTRA_REMINDER_TYPE, "water")
                                putExtra(EXTRA_WATER_AMOUNT, slot.amountMl)
                                putExtra(EXTRA_TASK_TITLE, "${slot.timeLabel} - ${slot.note}")
                                putExtra(EXTRA_TASK_ID, 9000 + slot.id)
                            }

                            val pendingIntent = PendingIntent.getBroadcast(
                                context,
                                9000 + slot.id,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    if (alarmManager.canScheduleExactAlarms()) {
                                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
                                    } else {
                                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
                                    }
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
                                } else {
                                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
                                }
                            } catch (e: Exception) {
                                try {
                                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
                                } catch (ex: Exception) {}
                            }
                        }
                    }

                    // 2. Reschedule Workouts & Day Tasks from Database
                    try {
                        val dao = AppDatabase.getDatabase(context).winterArcDao()
                        // Workouts
                        val workouts = dao.getWorkoutsList()
                        workouts.forEach { workout ->
                            if (!workout.isCompleted && workout.notificationScheduled) {
                                val cal = parseTimeToCalendar(workout.targetTime)
                                if (cal != null) {
                                    if (cal.timeInMillis <= nowMs) {
                                        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                                    }
                                    val wIntent = Intent(context, WinterArcAlarmReceiver::class.java).apply {
                                        putExtra(EXTRA_REMINDER_TYPE, "workout")
                                        putExtra(EXTRA_TASK_ID, workout.notificationId)
                                        putExtra(EXTRA_TASK_TITLE, workout.name)
                                        putExtra(EXTRA_TASK_TIME, workout.targetTime)
                                        putExtra(EXTRA_WORKOUT_SETS, workout.sets)
                                        putExtra(EXTRA_WORKOUT_REPS, workout.repsOrDuration)
                                    }
                                    val pIntent = PendingIntent.getBroadcast(
                                        context,
                                        workout.notificationId,
                                        wIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                    )
                                    scheduleExactAlarm(alarmManager, cal.timeInMillis, pIntent)
                                }
                            }
                        }

                        // Day Tasks
                        val tasks = dao.getDayTasksList()
                        tasks.forEach { task ->
                            if (!task.isCompleted && task.notificationScheduled) {
                                val cal = parseTimeToCalendar(task.scheduledTime)
                                if (cal != null) {
                                    val parts = task.targetDate.split("-")
                                    if (parts.size == 3) {
                                        cal.set(java.util.Calendar.YEAR, parts[0].toInt())
                                        cal.set(java.util.Calendar.MONTH, parts[1].toInt() - 1)
                                        cal.set(java.util.Calendar.DAY_OF_MONTH, parts[2].toInt())
                                    }
                                    if (cal.timeInMillis > nowMs) {
                                        val tIntent = Intent(context, WinterArcAlarmReceiver::class.java).apply {
                                            putExtra(EXTRA_REMINDER_TYPE, "task")
                                            putExtra(EXTRA_TASK_ID, task.notificationId)
                                            putExtra(EXTRA_TASK_TITLE, task.title)
                                            putExtra(EXTRA_TASK_TIME, task.scheduledTime)
                                        }
                                        val pIntent = PendingIntent.getBroadcast(
                                            context,
                                            task.notificationId,
                                            tIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                        )
                                        scheduleExactAlarm(alarmManager, cal.timeInMillis, pIntent)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("WinterArc", "DB reschedule failed", e)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("WinterArc", "rescheduleAll failed", e)
                }
            }
        }

        private fun scheduleExactAlarm(alarmManager: AlarmManager, triggerMs: Long, pendingIntent: PendingIntent) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
                }
            } catch (e: Exception) {
                try {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
                } catch (ex: Exception) {}
            }
        }

        private fun convertBanglaDigitsToEnglish(input: String): String {
            val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
            var res = input
            for (i in 0..9) {
                res = res.replace(bnDigits[i], ('0' + i))
            }
            return res
        }

        private fun parseTimeToCalendar(timeStr: String): java.util.Calendar? {
            return try {
                val cal = java.util.Calendar.getInstance()
                val engStr = convertBanglaDigitsToEnglish(timeStr)
                val clean = engStr.trim().uppercase(java.util.Locale.US)
                val isPm = clean.endsWith("PM")
                val isAm = clean.endsWith("AM")
                val rawTime = clean.replace("AM", "").replace("PM", "").trim()
                val parts = rawTime.split(":")
                if (parts.size >= 2) {
                    var hour = parts[0].trim().toInt()
                    val min = parts[1].trim().toInt()
                    if (isPm && hour < 12) hour += 12
                    if (isAm && hour == 12) hour = 0
                    cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
                    cal.set(java.util.Calendar.MINUTE, min)
                    cal.set(java.util.Calendar.SECOND, 0)
                    cal.set(java.util.Calendar.MILLISECOND, 0)
                    cal
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Acquire short partial wake lock so screen-off CPU doesn't sleep immediately
        var wakeLock: PowerManager.WakeLock? = null
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "winterarc:alarm_receiver")
            wakeLock?.acquire(4000L)
        } catch (e: Exception) {}

        try {
            val isArcNotificationsEnabled = context.getSharedPreferences("winter_arc_prefs", Context.MODE_PRIVATE)
                .getBoolean("arc_notifications_enabled", true)
            if (!isArcNotificationsEnabled) {
                return
            }

            val reminderType = intent.getStringExtra(EXTRA_REMINDER_TYPE) ?: "task"
            val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Winter Arc Task"
            val time = intent.getStringExtra(EXTRA_TASK_TIME) ?: ""
            val taskId = intent.getIntExtra(EXTRA_TASK_ID, (System.currentTimeMillis() % 100000).toInt())

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()

                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Urgent notifications for Winter Arc daily tasks, workouts and smart water reminders"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 250, 500)
                    setSound(defaultSound, audioAttributes)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    setShowBadge(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_tab", "winter_arc")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                taskId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val isBn = LanguageStore.currentLanguage.value == "bn"

            val (notifTitle, notifBody) = when (reminderType) {
                "water" -> {
                    val amount = intent.getIntExtra(EXTRA_WATER_AMOUNT, 350)
                    val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: ""
                    val t = if (isBn) {
                        if (taskTitle.isNotBlank()) "💧 পানি পান: $taskTitle" else "💧 পানি পানের সময় হয়েছে!"
                    } else {
                        if (taskTitle.isNotBlank()) "💧 Hydration: $taskTitle" else "💧 Time to Drink Water!"
                    }
                    val b = if (isBn) {
                        "সুস্থ ও চাঙ্গা থাকতে এখনই $amount ml পানি পান করুন। (সকাল ৫:০৫ - রাত ১০:০০ স্মার্ট হাইড্রেশন রুটিন)"
                    } else {
                        "Stay hydrated and sharp! Drink $amount ml water now. (5:05 AM - 10:00 PM Smart Hydration Routine)"
                    }
                    // Reschedule next water reminder if still enabled
                    rescheduleNextWaterAlarm(context, intent, taskId)
                    Pair(t, b)
                }
                "workout" -> {
                    val sets = intent.getIntExtra(EXTRA_WORKOUT_SETS, 3)
                    val reps = intent.getStringExtra(EXTRA_WORKOUT_REPS) ?: "15 reps"
                    val t = if (isBn) "💪 ব্যায়ামের সময়: $title" else "💪 Workout Time: $title"
                    val b = if (isBn) {
                        "নির্ধারিত $sets সেট ($reps) সম্পন্ন করার সময় হয়েছে! অলসতা ছেড়ে শুরু করুন, কোনো অজউহাত নয়।"
                    } else {
                        "Time for your workout: $sets sets ($reps)! Let's get moving, no excuses."
                    }
                    Pair(t, b)
                }
                else -> {
                    val t = if (isBn) "❄️ Winter Arc রুটিন অ্যালার্ট! [$time]" else "❄️ Winter Arc Protocol Alert! [$time]"
                    val b = if (isBn) {
                        "সময় হয়ে গেছে: $title! নিজের সাথে করা ওয়াদা রক্ষা করুন।"
                    } else {
                        "Time now: $title! Lock in, no excuses, conquer your day."
                    }
                    Pair(t, b)
                }
            }

            val logoBitmap = try {
                NotificationHelper.getAppLogoBitmap(context)
            } catch (e: Exception) {
                null
            }

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setColor(0xFF00B4D8.toInt())
                .setContentTitle(notifTitle)
                .setContentText(notifBody)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notifBody))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(defaultSound)
                .setVibrate(longArrayOf(0, 500, 250, 500))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            if (logoBitmap != null) {
                notificationBuilder.setLargeIcon(logoBitmap)
            }

            notificationManager.notify(taskId, notificationBuilder.build())
        } finally {
            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock.release()
                }
            } catch (e: Exception) {}
        }
    }

    private fun rescheduleNextWaterAlarm(context: Context, originalIntent: Intent, currentTaskId: Int) {
        val prefs = context.getSharedPreferences("winter_arc_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("water_reminder_enabled", true)
        val isArcEnabled = prefs.getBoolean("arc_notifications_enabled", true)
        if (!isEnabled || !isArcEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        if (currentTaskId in 9001..9020) {
            // Smart Slot: Reschedule for tomorrow same time (+24 hours)
            val nextTriggerTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
            val newIntent = Intent(context, WinterArcAlarmReceiver::class.java).apply {
                putExtra(EXTRA_REMINDER_TYPE, "water")
                putExtra(EXTRA_WATER_AMOUNT, originalIntent.getIntExtra(EXTRA_WATER_AMOUNT, 350))
                putExtra(EXTRA_TASK_TITLE, originalIntent.getStringExtra(EXTRA_TASK_TITLE))
                putExtra(EXTRA_TASK_ID, currentTaskId)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                currentTaskId,
                newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerTime, pending)
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerTime, pending)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerTime, pending)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTriggerTime, pending)
                }
            } catch (e: Exception) {
                try {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerTime, pending)
                } catch (ex: Exception) {
                    android.util.Log.e("WinterArc", "Reschedule smart slot alarm failed", ex)
                }
            }
        } else {
            // Manual Interval Reminder
            val intervalMins = prefs.getInt("water_reminder_interval_mins", 90).coerceAtLeast(30)
            val nextTriggerTime = System.currentTimeMillis() + (intervalMins * 60 * 1000L)
            val newIntent = Intent(context, WinterArcAlarmReceiver::class.java).apply {
                putExtra(EXTRA_REMINDER_TYPE, "water")
                putExtra(EXTRA_WATER_AMOUNT, originalIntent.getIntExtra(EXTRA_WATER_AMOUNT, 250))
                putExtra(EXTRA_WATER_INTERVAL, intervalMins)
                putExtra(EXTRA_TASK_ID, 9999)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                9999,
                newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerTime, pending)
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerTime, pending)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerTime, pending)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTriggerTime, pending)
                }
            } catch (e: Exception) {
                try {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerTime, pending)
                } catch (ex: Exception) {
                    android.util.Log.e("WinterArc", "Reschedule water alarm failed", ex)
                }
            }
        }
    }
}
