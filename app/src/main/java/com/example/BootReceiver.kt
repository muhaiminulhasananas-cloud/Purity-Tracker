package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

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

        val safeContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createDeviceProtectedStorageContext()
        } else {
            context
        }
        val prefs = safeContext.getSharedPreferences("VpnPrefs", Context.MODE_PRIVATE)
        val isVpnRunning = prefs.getBoolean("vpn_is_running", false)

        if (isVpnRunning && !LocalDnsVpnService.isVpnInterfaceUp()) {
            val vpnIntent = android.net.VpnService.prepare(context)
            if (vpnIntent == null) {
                try {
                    val serviceIntent = Intent(context, LocalDnsVpnService::class.java).apply {
                        this.action = LocalDnsVpnService.ACTION_START
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {}
            }
        }

        // Always start the permanent 24/7 Watchdog & JobScheduler on boot/unlock/package-replace
        try {
            SettingsGuardWatchdogService.start(context)
            SettingsGuardJobService.schedule(context)
            ProtectionService.pokeFromWatchdog(context)
        } catch (e: Exception) {}

        PrayerScheduler.scheduleDailyPrayers(context)
        WinterArcAlarmReceiver.rescheduleAll(context)
    }
}
