package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: ""

        // If system clock/date was changed manually, immediately lock any temporary PIN unlock
        if (action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            LockManager.lockImmediately(context)
        }

        // Schedule 15-minute WorkManager health checks and immediately check if Settings Guard is alive.
        // Never call startForegroundService() from background broadcasts on Android 12+.
        try {
            GuardHealthWorker.schedulePeriodicWork(context)
            ProtectionService.pokeFromWatchdog(context)
            GuardHealthWorker.checkNowAndNotify(context)
        } catch (e: Exception) {}

        PrayerScheduler.scheduleDailyPrayers(context)
        WinterArcAlarmReceiver.rescheduleAll(context)
    }
}
