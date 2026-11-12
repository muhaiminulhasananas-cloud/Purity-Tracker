package com.example

import android.app.AlarmManager
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
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat

/**
 * SettingsGuardWatchdogService:
 * True 24/7 Immortal Foreground Service (START_STICKY) sharing the unified single
 * Notification ID (1001) with ProtectionService so there is NEVER a duplicate notification,
 * while guaranteeing that Android LMKD, Doze Mode, and OEM freezers (Realme UI, ColorOS,
 * MIUI, Vivo, Samsung) NEVER freeze or kill the Purity Tracker process after 1-2 days.
 */
class SettingsGuardWatchdogService : Service() {

    companion object {
        const val UNIFIED_NOTIFICATION_ID = 1001
        const val UNIFIED_CHANNEL_ID = "purity_guard_24_7_silent_channel"

        @Volatile
        var isRunning: Boolean = false
            private set

        fun buildUnifiedGuardNotification(context: Context): Notification {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            // Remove legacy duplicate channels/IDs
            nm?.cancel(1009)
            nm?.cancel(1012)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    nm?.deleteNotificationChannel("protection_guard_service_channel")
                    nm?.deleteNotificationChannel("settings_guard_watchdog_channel")
                } catch (e: Exception) {}

                val channel = NotificationChannel(
                    UNIFIED_CHANNEL_ID,
                    "Purity Guard 24/7 Protection",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Keeps Settings Guard & Anti-Tamper active 24/7"
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

            return NotificationCompat.Builder(context, UNIFIED_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("🛡️ Settings Guard Active 24/7")
                .setContentText("Permanent Anti-Tamper & DNS Protection running.")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .setSilent(true)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setContentIntent(pendingIntent)
                .build()
        }

        fun promoteServiceToForeground(service: Service) {
            try {
                val notification = buildUnifiedGuardNotification(service)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    service.startForeground(
                        UNIFIED_NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } else {
                    service.startForeground(UNIFIED_NOTIFICATION_ID, notification)
                }
            } catch (e: Exception) {
                try {
                    val notification = buildUnifiedGuardNotification(service)
                    service.startForeground(UNIFIED_NOTIFICATION_ID, notification)
                } catch (e2: Exception) {}
            }
        }

        fun start(context: Context) {
            try {
                SettingsGuardJobService.schedule(context)
                val intent = Intent(context, SettingsGuardWatchdogService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                try {
                    val intent = Intent(context, SettingsGuardWatchdogService::class.java)
                    context.startService(intent)
                } catch (e2: Exception) {}
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var dynamicReceiver: BroadcastReceiver? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val periodicCheckRunnable = object : Runnable {
        override fun run() {
            try {
                promoteServiceToForeground(this@SettingsGuardWatchdogService)
                ProtectionService.pokeFromWatchdog(this@SettingsGuardWatchdogService)
                pulseShortWakeLock()
            } catch (e: Exception) {}
            scheduleNextHeartbeat()
            handler.postDelayed(this, 10000L) // check every 10 seconds
        }
    }

    private fun pulseShortWakeLock() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
            if (wakeLock == null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PurityTracker::WatchdogPulseLock").apply {
                    setReferenceCounted(false)
                }
            }
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(2500L)
            }
        } catch (e: Exception) {}
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
                        // Invalidate any temporary PIN unlock immediately if system clock was tampered with
                        LockManager.lockImmediately(context)
                    }
                    pulseShortWakeLock()
                    promoteServiceToForeground(this@SettingsGuardWatchdogService)
                    ProtectionService.pokeFromWatchdog(context)
                }
            }
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_DATE_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(dynamicReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                registerReceiver(dynamicReceiver, filter)
            }
        } catch (e: Exception) {}
    }

    private fun scheduleNextHeartbeat() {
        try {
            SettingsGuardJobService.schedule(applicationContext)
            val restartIntent = Intent(applicationContext, BootReceiver::class.java).apply {
                action = "com.example.ACTION_RESTART_WATCHDOG"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                1010,
                restartIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val am = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am?.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + (2 * 60 * 1000L), // 2 minutes exact heartbeat
                    pendingIntent
                )
            } else {
                am?.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + (2 * 60 * 1000L),
                    pendingIntent
                )
            }
        } catch (e: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        promoteServiceToForeground(this)
        registerDynamicSystemReceiver()
        handler.removeCallbacks(periodicCheckRunnable)
        handler.post(periodicCheckRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        promoteServiceToForeground(this)
        registerDynamicSystemReceiver()
        ProtectionService.pokeFromWatchdog(this)
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        resurrectService()
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
        resurrectService()
        super.onDestroy()
    }

    private fun resurrectService() {
        try {
            SettingsGuardJobService.schedule(applicationContext)
            val restartIntent = Intent(applicationContext, BootReceiver::class.java).apply {
                action = "com.example.ACTION_RESTART_WATCHDOG"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                1010,
                restartIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val am = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am?.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 1500L,
                    pendingIntent
                )
            } else {
                am?.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 1500L,
                    pendingIntent
                )
            }
        } catch (e: Exception) {}
    }
}
