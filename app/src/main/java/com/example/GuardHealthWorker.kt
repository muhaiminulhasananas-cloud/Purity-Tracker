package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat

/**
 * GuardHealthWorker:
 * Periodic 15-minute OS-persisted JobScheduler health monitor for Settings Guard (ProtectionService).
 *
 * Checks BOTH:
 * 1. Whether the service toggle is enabled in Android Accessibility Settings
 *    (`AntiUninstallManager.isAccessibilityConfiguredInSystem(context)`).
 * 2. Whether the service process is ACTUALLY ALIVE (`AntiUninstallManager.isAccessibilityServiceAlive(context)`),
 *    verified via `ProtectionService.isServiceRunning` and `ProtectionService.lastActiveElapsedRealtime`
 *    (stale > 5 minutes while enabled in Settings == dead-but-enabled state).
 *
 * Posts a high-priority ongoing notification (ID 1005) on channel `guard_health_alert_channel`:
 * - In the dead-but-enabled state: instructs the user to toggle Purity Tracker OFF and ON again
 *   in Accessibility Settings (the only action that forces Android/ColorOS to rebind a killed AccessibilityService).
 * - When Settings Guard is OFF while expected to be ON: alerts "Settings Guard is OFF — tap to re-enable".
 */
object GuardHealthWorker {

    private const val JOB_ID_HEALTH_MONITOR = 4091
    const val ALERT_NOTIFICATION_ID = 1005
    const val ALERT_CHANNEL_ID = "guard_health_alert_channel"

    fun schedulePeriodicWork(context: Context) {
        try {
            val appCtx = context.applicationContext
            val scheduler = appCtx.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler ?: return
            val component = ComponentName(appCtx, SettingsGuardJobService::class.java)

            val periodicExists = scheduler.allPendingJobs.any { it.id == JOB_ID_HEALTH_MONITOR }
            if (!periodicExists) {
                val periodicBuilder = JobInfo.Builder(JOB_ID_HEALTH_MONITOR, component)
                    .setPeriodic(15 * 60 * 1000L) // 15-minute periodic OS job (survives reboots)
                    .setPersisted(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                scheduler.schedule(periodicBuilder.build())
            }
        } catch (e: Exception) {}
    }

    fun checkNowAndNotify(context: Context) {
        try {
            val appCtx = context.applicationContext
            val configuredInSettings = AntiUninstallManager.isAccessibilityConfiguredInSystem(appCtx)
            val isAlive = AntiUninstallManager.isAccessibilityServiceAlive(appCtx)
            val expectedOn = AntiUninstallManager.isGuardExpectedToBeOn(appCtx) || LockManager.hasPin(appCtx)

            if (configuredInSettings && isAlive) {
                // Guard is both enabled in Settings and actively alive in process
                AntiUninstallManager.setGuardExpectedToBeOn(appCtx, true)
                cancelHealthAlertNotification(appCtx)
                return
            }

            if (configuredInSettings && !isAlive) {
                // Dead-but-enabled state: Settings toggle is ON, but OEM killed the service process / unbound it
                AntiUninstallManager.setGuardExpectedToBeOn(appCtx, true)
                postHealthAlertNotification(
                    context = appCtx,
                    isDeadButEnabled = true
                )
                return
            }

            if (!configuredInSettings && expectedOn) {
                // Guard was enabled by user (or PIN protection is set) but Accessibility is now OFF
                postHealthAlertNotification(
                    context = appCtx,
                    isDeadButEnabled = false
                )
            }
        } catch (e: Exception) {}
    }

    fun cancelHealthAlertNotification(context: Context) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.cancel(ALERT_NOTIFICATION_ID)
        } catch (e: Exception) {}
    }

    private fun postHealthAlertNotification(
        context: Context,
        isDeadButEnabled: Boolean
    ) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val isBn = LanguageStore.currentLanguage.value == "bn"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    ALERT_CHANNEL_ID,
                    "Settings Guard Health Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Critical alerts when Settings Guard stops or needs re-enabling"
                    enableVibration(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                nm.createNotificationChannel(channel)
            }

            val accessibilityIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                ALERT_NOTIFICATION_ID,
                accessibilityIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val title = if (isDeadButEnabled) {
                if (isBn) "⚠️ সেটিংস গার্ড থেমে গেছে — এখনই রিস্টার্ট করুন"
                else "⚠️ Settings Guard Stopped — Tap to Rebind"
            } else {
                if (isBn) "⚠️ সেটিংস গার্ড বন্ধ আছে — চালু করতে ট্যাপ করুন"
                else "Settings Guard is OFF — tap to re-enable"
            }

            val body = if (isDeadButEnabled) {
                if (isBn) "ফোন সিস্টেম সেটিংস গার্ড থামিয়ে দিয়েছে (সেটিংসে ON দেখালেও কাজ করছে না)। ট্যাপ করে Accessibility-তে যান এবং Purity Tracker একবার OFF করে আবার ON করুন।"
                else "Your phone stopped the guard process while leaving the toggle ON. Tap here to open Accessibility Settings, then toggle Purity Tracker OFF and ON again."
            } else {
                if (isBn) "আপনার সুরক্ষা বর্তমানে নিষ্ক্রিয়। ট্যাপ করে Accessibility Settings থেকে Purity Tracker পুনরায় চালু করুন।"
                else "Protection is currently inactive. Tap here to open Accessibility Settings and enable Purity Tracker."
            }

            val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .build()

            nm.notify(ALERT_NOTIFICATION_ID, notification)
        } catch (e: Exception) {}
    }
}
