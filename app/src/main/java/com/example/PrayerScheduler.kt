package com.example

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PrayerScheduler {

    private fun getPrefs(context: Context) = context.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)

    fun updateLocation(context: Context, latitude: Double, longitude: Double) {
        getPrefs(context).edit().putString("lat", latitude.toString()).putString("lng", longitude.toString()).apply()
    }

    fun getLat(context: Context): Double = getPrefs(context).getString("lat", "23.8103")?.toDoubleOrNull() ?: 23.8103
    fun getLng(context: Context): Double = getPrefs(context).getString("lng", "90.4125")?.toDoubleOrNull() ?: 90.4125

    fun isHanafi(context: Context): Boolean = getPrefs(context).getBoolean("use_hanafi_asr", true)

    fun setHanafi(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("use_hanafi_asr", enabled).apply()
    }

    fun getMasjidOffset(context: Context, prayerKey: String): Int {
        // Default offsets for Bangladesh/Subcontinent mosques:
        // Maghrib = 0 (immediately at sunset), Dhuhr = 15m, Asr = 15m, Isha = 15m, Fajr = 20m
        val defaultOffset = when (prayerKey.lowercase()) {
            "fajr" -> 20
            "dhuhr" -> 15
            "asr" -> 15
            "maghrib" -> 0
            "isha" -> 15
            "tahajjud" -> 0
            else -> 0
        }
        return getPrefs(context).getInt("offset_$prayerKey", defaultOffset)
    }

    fun setMasjidOffset(context: Context, prayerKey: String, offsetMinutes: Int) {
        getPrefs(context).edit().putInt("offset_$prayerKey", offsetMinutes).apply()
    }

    fun isMasjidOffsetEnabled(context: Context): Boolean = getPrefs(context).getBoolean("masjid_offset_enabled", true)

    fun setMasjidOffsetEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("masjid_offset_enabled", enabled).apply()
    }

    fun scheduleDailyPrayers(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch for today
                val now = Date()
                val todayTimings = fetchTimings(context, now)
                
                // Fetch for tomorrow
                val tomCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                val tomTimings = fetchTimings(context, tomCal.time)
                
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val sdf = SimpleDateFormat("HH:mm", Locale.US)
                val currentTimeMs = System.currentTimeMillis()
                val offsetEnabled = isMasjidOffsetEnabled(context)

                // Schedule today
                if (todayTimings != null) {
                    val fivePrayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                    
                    // 1. Five Obligatory Prayers + Sunrise & Sunset
                    for ((name, timeStr) in todayTimings) {
                        try {
                            val timePart = sdf.parse(timeStr) ?: continue
                            val tCal = Calendar.getInstance().apply { time = now }
                            val pCal = Calendar.getInstance().apply { time = timePart }
                            tCal.set(Calendar.HOUR_OF_DAY, pCal.get(Calendar.HOUR_OF_DAY))
                            tCal.set(Calendar.MINUTE, pCal.get(Calendar.MINUTE))
                            tCal.set(Calendar.SECOND, 0)
                            tCal.set(Calendar.MILLISECOND, 0)
                            
                            val prayerStartMs = tCal.timeInMillis

                            // Calculate actual Adhan / Mosque announcement time
                            val offsetMinutes = if (offsetEnabled && fivePrayers.any { it.equals(name, ignoreCase = true) }) {
                                getMasjidOffset(context, name)
                            } else 0

                            val adhanTimeMs = prayerStartMs + (offsetMinutes * 60 * 1000L)

                            // Main time alarm (Adhan time)
                            if (adhanTimeMs > currentTimeMs) {
                                setExactAlarm(context, alarmManager, name, adhanTimeMs, "PRAYER_TIME_ACTION", 0)
                            }

                            // If it's one of the 5 daily prayers, schedule 25m, 35m, 50m reminders based on adhan/waqt
                            if (fivePrayers.any { it.equals(name, ignoreCase = true) }) {
                                val reminder25Ms = adhanTimeMs + (25L * 60 * 1000)
                                if (reminder25Ms > currentTimeMs) {
                                    setExactAlarm(context, alarmManager, name, reminder25Ms, "PRAYER_REMINDER_ACTION", 25)
                                }

                                val reminder35Ms = adhanTimeMs + (35L * 60 * 1000)
                                if (reminder35Ms > currentTimeMs) {
                                    setExactAlarm(context, alarmManager, name, reminder35Ms, "PRAYER_REMINDER_ACTION", 35)
                                }

                                val reminder50Ms = adhanTimeMs + (50L * 60 * 1000)
                                if (reminder50Ms > currentTimeMs) {
                                    setExactAlarm(context, alarmManager, name, reminder50Ms, "PRAYER_REMINDER_ACTION", 50)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("PrayerScheduler", "Error parsing time for $name", e)
                        }
                    }

                    // 2. Tahajjud (তাহাজ্জুদ) Schedule
                    val fajrStr = todayTimings["Fajr"] ?: "04:30"
                    val maghribStr = todayTimings["Maghrib"] ?: "18:25"
                    val fajrDate = sdf.parse(fajrStr)
                    val maghribDate = sdf.parse(maghribStr)
                    if (fajrDate != null && maghribDate != null) {
                        val fajrMin = Calendar.getInstance().apply { time = fajrDate }.let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) }
                        val maghribMin = Calendar.getInstance().apply { time = maghribDate }.let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) }
                        val nightDuration = (fajrMin + 1440 - maghribMin) % 1440
                        val tahajjudMin = (maghribMin + (nightDuration * 2) / 3) % 1440
                        
                        val tahajjudCal = Calendar.getInstance().apply {
                            time = now
                            set(Calendar.HOUR_OF_DAY, tahajjudMin / 60)
                            set(Calendar.MINUTE, tahajjudMin % 60)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        // If already passed for today, schedule for next night
                        if (tahajjudCal.timeInMillis <= currentTimeMs) {
                            tahajjudCal.add(Calendar.DAY_OF_YEAR, 1)
                        }
                        if (tahajjudCal.timeInMillis > currentTimeMs) {
                            setExactAlarm(context, alarmManager, "Tahajjud", tahajjudCal.timeInMillis, "PRAYER_TIME_ACTION", 0)
                        }
                    }
                }
                
                // Schedule tomorrow's Fajr and Tahajjud
                if (tomTimings != null) {
                    val timeStr = tomTimings["Fajr"]
                    if (timeStr != null) {
                        val pCal = Calendar.getInstance().apply { time = sdf.parse(timeStr)!! }
                        val tCal = Calendar.getInstance().apply { time = tomCal.time }
                        tCal.set(Calendar.HOUR_OF_DAY, pCal.get(Calendar.HOUR_OF_DAY))
                        tCal.set(Calendar.MINUTE, pCal.get(Calendar.MINUTE))
                        tCal.set(Calendar.SECOND, 0)
                        tCal.set(Calendar.MILLISECOND, 0)
                        
                        val fajrStartMs = tCal.timeInMillis
                        val fajrOffset = if (offsetEnabled) getMasjidOffset(context, "Fajr") else 0
                        val fajrAdhanMs = fajrStartMs + (fajrOffset * 60 * 1000L)

                        setExactAlarm(context, alarmManager, "Fajr", fajrAdhanMs, "PRAYER_TIME_ACTION", 0)
                        setExactAlarm(context, alarmManager, "Fajr", fajrAdhanMs + (25L * 60 * 1000), "PRAYER_REMINDER_ACTION", 25)
                        setExactAlarm(context, alarmManager, "Fajr", fajrAdhanMs + (35L * 60 * 1000), "PRAYER_REMINDER_ACTION", 35)
                        setExactAlarm(context, alarmManager, "Fajr", fajrAdhanMs + (50L * 60 * 1000), "PRAYER_REMINDER_ACTION", 50)
                    }

                    // Tomorrow's Sunrise & Sunset
                    tomTimings["Sunrise"]?.let { sStr ->
                        val pCal = Calendar.getInstance().apply { time = sdf.parse(sStr)!! }
                        val tCal = Calendar.getInstance().apply { time = tomCal.time }
                        tCal.set(Calendar.HOUR_OF_DAY, pCal.get(Calendar.HOUR_OF_DAY))
                        tCal.set(Calendar.MINUTE, pCal.get(Calendar.MINUTE))
                        setExactAlarm(context, alarmManager, "Sunrise", tCal.timeInMillis, "PRAYER_TIME_ACTION", 0)
                    }
                    tomTimings["Sunset"]?.let { sStr ->
                        val pCal = Calendar.getInstance().apply { time = sdf.parse(sStr)!! }
                        val tCal = Calendar.getInstance().apply { time = tomCal.time }
                        tCal.set(Calendar.HOUR_OF_DAY, pCal.get(Calendar.HOUR_OF_DAY))
                        tCal.set(Calendar.MINUTE, pCal.get(Calendar.MINUTE))
                        setExactAlarm(context, alarmManager, "Sunset", tCal.timeInMillis, "PRAYER_TIME_ACTION", 0)
                    }
                }
                
                Log.d("PrayerScheduler", "Scheduled all daily prayers with masjid offsets & reminders.")
            } catch (e: Exception) {
                Log.e("PrayerScheduler", "Failed to schedule", e)
            }
        }
    }

    private fun fetchTimings(context: Context, date: Date): Map<String, String>? {
        try {
            val timestamp = date.time / 1000
            val lat = getLat(context)
            val lng = getLng(context)
            val useHanafi = isHanafi(context)
            // school=1 (Hanafi Asr shadow 2x), school=0 (Shafi'i/Standard shadow 1x)
            // method=1 (University of Islamic Sciences, Karachi - standard for subcontinent / Bangladesh)
            val schoolParam = if (useHanafi) "&school=1" else "&school=0"
            val urlString = "https://api.aladhan.com/v1/timings/$timestamp?latitude=$lat&longitude=$lng&method=1$schoolParam"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            
            if (conn.responseCode == 200) {
                val reader = InputStreamReader(conn.inputStream)
                val response = reader.readText()
                reader.close()
                
                val json = JSONObject(response)
                val timings = json.getJSONObject("data").getJSONObject("timings")
                
                val result = mapOf(
                    "Fajr" to timings.getString("Fajr"),
                    "Sunrise" to timings.getString("Sunrise"),
                    "Dhuhr" to timings.getString("Dhuhr"),
                    "Asr" to timings.getString("Asr"),
                    "Sunset" to timings.getString("Sunset"),
                    "Maghrib" to timings.getString("Maghrib"),
                    "Isha" to timings.getString("Isha")
                )
                
                // If it's today's fetch, save to prefs
                val calNow = Calendar.getInstance()
                val calFetch = Calendar.getInstance().apply { time = date }
                if (calNow.get(Calendar.DAY_OF_YEAR) == calFetch.get(Calendar.DAY_OF_YEAR)) {
                    val edit = getPrefs(context).edit()
                    result.forEach { (k, v) -> edit.putString("time_$k", v) }
                    edit.apply()
                }
                
                return result
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun schedulePrayerReminder(context: Context, prayerName: String, minutesDelayed: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val timeMs = System.currentTimeMillis() + (minutesDelayed * 60 * 1000)
        setExactAlarm(context, alarmManager, prayerName, timeMs, "PRAYER_REMINDER_ACTION", minutesDelayed)
    }

    private fun setExactAlarm(
        context: Context,
        alarmManager: AlarmManager,
        prayerName: String,
        timeMs: Long,
        action: String,
        reminderStep: Int
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            this.action = action
            putExtra("PRAYER_NAME", prayerName)
            putExtra("REMINDER_STEP", reminderStep)
        }
        val requestCode = prayerName.hashCode() * 31 + reminderStep
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val showIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val showPendingIntent = PendingIntent.getActivity(
                    context, requestCode + 50000, showIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmClockInfo = AlarmManager.AlarmClockInfo(timeMs, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            }
        } catch (e: Exception) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
                } else {
                    alarmManager.setWindow(AlarmManager.RTC_WAKEUP, timeMs, 5000, pendingIntent)
                }
            } catch (err: Exception) {
                Log.e("PrayerScheduler", "Failed to schedule alarm", err)
            }
        }
    }
}
