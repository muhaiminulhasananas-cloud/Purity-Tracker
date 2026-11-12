package com.example

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Persistent OS-level JobService that wakes the Purity Tracker process even across
 * deep Doze cycles, multi-day idle windows, and OEM freezer sweeps.
 */
class SettingsGuardJobService : JobService() {

    companion object {
        private const val JOB_ID_PERIODIC = 4091
        private const val JOB_ID_FAST_RESCHEDULE = 4092

        fun schedule(context: Context) {
            try {
                val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler ?: return
                val component = ComponentName(context, SettingsGuardJobService::class.java)

                // 1. Periodic persisted job (survives device reboots forever)
                val periodicExists = scheduler.allPendingJobs.any { it.id == JOB_ID_PERIODIC }
                if (!periodicExists) {
                    val periodicBuilder = JobInfo.Builder(JOB_ID_PERIODIC, component)
                        .setPeriodic(15 * 60 * 1000L)
                        .setPersisted(true)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                    scheduler.schedule(periodicBuilder.build())
                }

                // 2. Fast 90-second heartbeat job
                val fastBuilder = JobInfo.Builder(JOB_ID_FAST_RESCHEDULE, component)
                    .setMinimumLatency(90 * 1000L)
                    .setOverrideDeadline(150 * 1000L)
                    .setPersisted(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                scheduler.schedule(fastBuilder.build())
            } catch (e: Exception) {}
        }
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        try {
            SettingsGuardWatchdogService.start(applicationContext)
            ProtectionService.pokeFromWatchdog(applicationContext)
            val safeCtx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                applicationContext.createDeviceProtectedStorageContext()
            } else {
                applicationContext
            }
            val vpnPrefs = safeCtx.getSharedPreferences("VpnPrefs", Context.MODE_PRIVATE)
            if (vpnPrefs.getBoolean("vpn_is_running", false) && !LocalDnsVpnService.isVpnInterfaceUp()) {
                if (android.net.VpnService.prepare(applicationContext) == null) {
                    val vpnIntent = Intent(applicationContext, LocalDnsVpnService::class.java).apply {
                        action = LocalDnsVpnService.ACTION_START
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        applicationContext.startForegroundService(vpnIntent)
                    } else {
                        applicationContext.startService(vpnIntent)
                    }
                }
            }
        } catch (e: Exception) {}

        // Re-schedule the fast heartbeat job
        schedule(applicationContext)
        jobFinished(params, false)
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        schedule(applicationContext)
        return true
    }
}
