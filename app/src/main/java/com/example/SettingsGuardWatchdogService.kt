package com.example

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

/**
 * SettingsGuardWatchdogService:
 * Uses its own dedicated Notification ID (1009) and channel (`settings_guard_watchdog_channel`),
 * completely separate from `ProtectionService` and `GuardHealthWorker` (1005).
 *
 * IMPORTANT (Android 12+ / targetSdk 36 compliance):
 * - Never started from the background, `onUnbind()`, `onDestroy()`, or alarm broadcasts.
 * - Started ONLY when the app is in the foreground (`startIfInForeground`) or from a direct user action.
 * - Periodic 15-minute health checks are handled by WorkManager (`GuardHealthWorker`).
 */
class SettingsGuardWatchdogService : Service() {

    companion object {
        const val WATCHDOG_NOTIFICATION_ID = 1009
        const val WATCHDOG_CHANNEL_ID = "settings_guard_watchdog_channel"

        @Volatile
        var isRunning: Boolean = false
            private set

        private fun isAppInForeground(context: Context): Boolean {
            return try {
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
                val processes = am.runningAppProcesses ?: return false
                val pkg = context.packageName
                processes.any {
                    it. processName == pkg &&
                        it.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                }
            } catch (e: Exception) {
                false
            }
        }

        fun buildWatchdogNotification(context: Context): Notification {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    WATCHDOG_CHANNEL_ID,
                    "Settings Guard Monitor",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Monitors Settings Guard health while active"
                    setShowBadge(false)
                    enableVibration(false)
                    setSound(null, null)
                    lockscreenVisibility = Notification.VISIBILITY_SECRET
                }
                nm?.createNotificationChannel(channel)
            }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            return NotificationCompat.Builder(context, WATCHDOG_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("🛡️ Settings Guard Monitor")
                .setContentText("Monitoring Settings Guard & Anti-Tamper health.")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .setSilent(true)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setContentIntent(pendingIntent)
                .build()
        }

        private fun promoteWatchdogToForeground(service: Service) {
            try {
                val notification = buildWatchdogNotification(service)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    service.startForeground(
                        WATCHDOG_NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } else {
                    service.startForeground(WATCHDOG_NOTIFICATION_ID, notification)
                }
            } catch (e: Exception) {}
        }

        /**
         * Starts the watchdog ONLY when the app is currently in the foreground (visible to the user).
         * Always schedules the WorkManager 15-minute `GuardHealthWorker` regardless.
         */
        fun startIfInForeground(context: Context) {
            GuardHealthWorker.schedulePeriodicWork(context)
            if (!isAppInForeground(context)) {
                return
            }
            try {
                val intent = Intent(context, SettingsGuardWatchdogService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Never crash if OS rejects foreground service start
            }
        }

        fun start(context: Context) {
            startIfInForeground(context)
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var dynamicReceiver: BroadcastReceiver? = null

    private val periodicCheckRunnable = object : Runnable {
        override fun run() {
            try {
                ProtectionService.pokeFromWatchdog(this@SettingsGuardWatchdogService)
                GuardHealthWorker.checkNowAndNotify(this@SettingsGuardWatchdogService)
            } catch (e: Exception) {}
            handler.postDelayed(this, 60_000L) // check every 60 seconds while alive
        }
    }

    private fun registerDynamicSystemReceiver() {
        if (dynamicReceiver != null) return
        try {
            dynamicReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action ?: return
                    if (action == Intent.ACTION_TIME_CHANGED ||
                        action == Intent.ACTION_DATE_CHANGED ||
                        action == Intent.ACTION_TIMEZONE_CHANGED
                    ) {
                        LockManager.lockImmediately(context)
                    }
                    ProtectionService.pokeFromWatchdog(context)
                    GuardHealthWorker.checkNowAndNotify(context)
                }
            }
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_DATE_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(dynamicReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                registerReceiver(dynamicReceiver, filter)
            }
        } catch (e: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        promoteWatchdogToForeground(this)
        registerDynamicSystemReceiver()
        GuardHealthWorker.schedulePeriodicWork(this)
        handler.removeCallbacks(periodicCheckRunnable)
        handler.post(periodicCheckRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        promoteWatchdogToForeground(this)
        registerDynamicSystemReceiver()
        ProtectionService.pokeFromWatchdog(this)
        GuardHealthWorker.schedulePeriodicWork(this)
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Do NOT call startForegroundService or schedule exact alarms from onTaskRemoved.
        GuardHealthWorker.schedulePeriodicWork(applicationContext)
        GuardHealthWorker.checkNowAndNotify(applicationContext)
    }

    override fun onDestroy() {
        isRunning = false
        try {
            dynamicReceiver?.let {
                unregisterReceiver(it)
                dynamicReceiver = null
            }
        } catch (e: Exception) {}
        handler.removeCallbacks(periodicCheckRunnable)
        // Do NOT call startForegroundService from onDestroy.
        GuardHealthWorker.schedulePeriodicWork(applicationContext)
        super.onDestroy()
    }
}
