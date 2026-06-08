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
                
                // Schedule today
                if (todayTimings != null) {
                    val sdf = SimpleDateFormat("HH:mm", Locale.US)
                    for ((name, timeStr) in todayTimings) {
                        try {
                            val timePart = sdf.parse(timeStr)
                            val tCal = Calendar.getInstance().apply { time = now }
                            val pCal = Calendar.getInstance().apply { time = timePart!! }
                            tCal.set(Calendar.HOUR_OF_DAY, pCal.get(Calendar.HOUR_OF_DAY))
                            tCal.set(Calendar.MINUTE, pCal.get(Calendar.MINUTE))
                            tCal.set(Calendar.SECOND, 0)
                            
                            if (tCal.time.after(now)) {
                                setExactAlarm(context, alarmManager, name, tCal.timeInMillis, "PRAYER_TIME_ACTION")
                            }
                        } catch(e: Exception) {}
                    }
                }
                
                // Schedule tomorrow's Fajr
                if (tomTimings != null) {
                    val timeStr = tomTimings["Fajr"]
                    if (timeStr != null) {
                        val sdf = SimpleDateFormat("HH:mm", Locale.US)
                        val pCal = Calendar.getInstance().apply { time = sdf.parse(timeStr)!! }
                        val tCal = Calendar.getInstance().apply { time = tomCal.time }
                        tCal.set(Calendar.HOUR_OF_DAY, pCal.get(Calendar.HOUR_OF_DAY))
                        tCal.set(Calendar.MINUTE, pCal.get(Calendar.MINUTE))
                        tCal.set(Calendar.SECOND, 0)
                        
                        setExactAlarm(context, alarmManager, "Fajr", tCal.timeInMillis, "PRAYER_TIME_ACTION")
                    }
                }
                
                Log.d("PrayerScheduler", "Scheduled prayers from API.")
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
            val urlString = "https://api.aladhan.com/v1/timings/$timestamp?latitude=$lat&longitude=$lng&method=2"
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
        setExactAlarm(context, alarmManager, prayerName, timeMs, "PRAYER_REMINDER_ACTION")
    }

    private fun setExactAlarm(context: Context, alarmManager: AlarmManager, prayerName: String, timeMs: Long, action: String) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            this.action = action
            putExtra("PRAYER_NAME", prayerName)
        }
        val requestCode = prayerName.hashCode() + action.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            }
        } catch(e: SecurityException) {
            // Android 14+ fallback
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, timeMs, 5000, pendingIntent)
        }
    }
}
