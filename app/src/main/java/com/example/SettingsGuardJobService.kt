package com.example

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context

/**
 * Persistent OS-level JobService that runs alongside WorkManager to check
 * Settings Guard liveness and post a notification if the guard was stopped by the OS.
 * Never calls startForegroundService() from the background.
 */
class SettingsGuardJobService : JobService() {

    companion object {
        private const val JOB_ID_PERIODIC = 4091

        fun schedule(context: Context) {
            try {
                GuardHealthWorker.schedulePeriodicWork(context)
                val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler ?: return
                val component = ComponentName(context, SettingsGuardJobService::class.java)

                val periodicExists = scheduler.allPendingJobs.any { it.id == JOB_ID_PERIODIC }
                if (!periodicExists) {
                    val periodicBuilder = JobInfo.Builder(JOB_ID_PERIODIC, component)
                        .setPeriodic(15 * 60 * 1000L)
                        .setPersisted(true)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                    scheduler.schedule(periodicBuilder.build())
                }
            } catch (e: Exception) {}
        }
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        try {
            ProtectionService.pokeFromWatchdog(applicationContext)
            GuardHealthWorker.checkNowAndNotify(applicationContext)
        } catch (e: Exception) {}

        jobFinished(params, false)
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }
}
