package com.example

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class WinterArcViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val dao = AppDatabase.getDatabase(context).winterArcDao()
    private val prefs = context.getSharedPreferences("winter_arc_prefs", Context.MODE_PRIVATE)

    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var snapshotListener: com.google.firebase.firestore.ListenerRegistration? = null

    val challenges = dao.getAllChallenges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workouts = dao.getAllWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dayTasks = dao.getAllDayTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val runRecords = dao.getAllRuns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dayRecords = dao.getAllDayRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Water tracking state
    private val _waterIntakeMl = MutableStateFlow(loadWaterIntake())
    val waterIntakeMl = _waterIntakeMl.asStateFlow()

    private val _waterLogs = MutableStateFlow<List<WaterLogEntry>>(loadWaterLogs())
    val waterLogs = _waterLogs.asStateFlow()

    private val _waterTargetMl = MutableStateFlow(prefs.getInt("water_target_ml", 3500))
    val waterTargetMl = _waterTargetMl.asStateFlow()

    // Master Winter Arc Notifications Switch
    private val _arcNotificationsEnabled = MutableStateFlow(prefs.getBoolean("arc_notifications_enabled", true))
    val arcNotificationsEnabled = _arcNotificationsEnabled.asStateFlow()

    // Water reminder settings
    private val _waterReminderEnabled = MutableStateFlow(prefs.getBoolean("water_reminder_enabled", true))
    val waterReminderEnabled = _waterReminderEnabled.asStateFlow()

    private val _waterReminderIntervalMins = MutableStateFlow(prefs.getInt("water_reminder_interval_mins", 90))
    val waterReminderIntervalMins = _waterReminderIntervalMins.asStateFlow()

    // Auto-Water Mode: Defaults to true for smart 05:05 AM - 10:00 PM schedule
    private val _waterAutoModeEnabled = MutableStateFlow(prefs.getBoolean("water_auto_mode", true))
    val waterAutoModeEnabled = _waterAutoModeEnabled.asStateFlow()

    // Timeline duration mode: "90_days" (Dec 31) or "150_days" (Feb end) or "custom"
    private val _timelineMode = MutableStateFlow(prefs.getString("timeline_mode", "90_days") ?: "90_days")
    val timelineMode = _timelineMode.asStateFlow()

    private val _customTargetDateMs = MutableStateFlow(prefs.getLong("custom_target_date_ms", 0L))
    val customTargetDateMs = _customTargetDateMs.asStateFlow()

    init {
        initializeDefaultChallengesIfEmpty()
        initializeDefaultWorkoutsIfEmpty()
        seedCalendarHistoryIfEmpty()
        if (_arcNotificationsEnabled.value) {
            if (_waterReminderEnabled.value) {
                scheduleAllSmartWaterSlots()
            }
            scheduleAllWorkoutsAlarms()
            scheduleAllTaskAlarms()
        }
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            try {
                auth.signInAnonymously().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        syncFromCloud()
                        syncToCloud()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("WinterArcCloud", "Anonymous sign in error", e)
            }
        }
        syncFromCloud()
        authListener = FirebaseAuth.AuthStateListener { currentAuth ->
            if (currentAuth.currentUser != null) {
                syncFromCloud()
                syncToCloud()
            }
        }
        try {
            FirebaseAuth.getInstance().addAuthStateListener(authListener!!)
        } catch (e: Exception) {}
    }

    private fun getTodayDateStr(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun loadWaterIntake(): Int {
        val lastDate = prefs.getString("water_last_date", "")
        val today = getTodayDateStr()
        return if (lastDate == today) {
            prefs.getInt("water_current_ml", 0)
        } else {
            prefs.edit().putString("water_last_date", today).putInt("water_current_ml", 0).putString("water_logs_json", "[]").apply()
            0
        }
    }

    private fun loadWaterLogs(): List<WaterLogEntry> {
        val lastDate = prefs.getString("water_last_date", "")
        val today = getTodayDateStr()
        if (lastDate != today) return emptyList()
        val jsonStr = prefs.getString("water_logs_json", "[]") ?: "[]"
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<WaterLogEntry>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    WaterLogEntry(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        amountMl = obj.optInt("amountMl", 250),
                        timeFormatted = obj.optString("timeFormatted", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveWaterLogs(list: List<WaterLogEntry>) {
        try {
            val arr = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("amountMl", item.amountMl)
                    put("timeFormatted", item.timeFormatted)
                }
                arr.put(obj)
            }
            prefs.edit().putString("water_logs_json", arr.toString()).apply()
        } catch (e: Exception) {}
    }

    fun addWater(amountMl: Int) {
        val timeNow = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val newEntry = WaterLogEntry(
            id = System.currentTimeMillis(),
            amountMl = amountMl,
            timeFormatted = timeNow
        )
        val updatedLogs = _waterLogs.value + newEntry
        _waterLogs.value = updatedLogs
        saveWaterLogs(updatedLogs)

        val newAmount = (_waterIntakeMl.value + amountMl).coerceIn(0, 10000)
        _waterIntakeMl.value = newAmount
        prefs.edit()
            .putString("water_last_date", getTodayDateStr())
            .putInt("water_current_ml", newAmount)
            .apply()
        updateTodayDayRecord()
        syncToCloud()
    }

    fun deleteWaterLog(entry: WaterLogEntry) {
        val updatedLogs = _waterLogs.value.filter { it.id != entry.id }
        _waterLogs.value = updatedLogs
        saveWaterLogs(updatedLogs)

        val newAmount = (_waterIntakeMl.value - entry.amountMl).coerceAtLeast(0)
        _waterIntakeMl.value = newAmount
        prefs.edit()
            .putString("water_last_date", getTodayDateStr())
            .putInt("water_current_ml", newAmount)
            .apply()
        updateTodayDayRecord()
        syncToCloud()
    }

    fun resetWater() {
        _waterIntakeMl.value = 0
        _waterLogs.value = emptyList()
        _waterAutoModeEnabled.value = false
        prefs.edit()
            .putString("water_last_date", getTodayDateStr())
            .putInt("water_current_ml", 0)
            .putString("water_logs_json", "[]")
            .putBoolean("water_auto_mode", false)
            .apply()
        updateTodayDayRecord()
        syncToCloud()
    }

    fun setWaterTarget(targetMl: Int) {
        _waterTargetMl.value = targetMl
        prefs.edit().putInt("water_target_ml", targetMl).apply()
    }

    fun toggleWaterAutoMode(enabled: Boolean) {
        _waterAutoModeEnabled.value = enabled
        prefs.edit().putBoolean("water_auto_mode", enabled).apply()
        if (enabled) {
            scheduleAllSmartWaterSlots()
        }
        syncToCloud()
    }

    fun autoSyncWaterToCurrentTime(): Int {
        val cal = Calendar.getInstance()
        val curMins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        
        // Sum all slots that have already passed today from 5:00 AM to 10:30 PM
        val passedSlots = smartWaterSlots.filter { (it.hour * 60 + it.minute) <= curMins }
        val targetExpected = passedSlots.sumOf { it.amountMl }
        
        if (_waterIntakeMl.value < targetExpected) {
            _waterIntakeMl.value = targetExpected
            prefs.edit()
                .putString("water_last_date", getTodayDateStr())
                .putInt("water_current_ml", targetExpected)
                .apply()
            updateTodayDayRecord()
        }
        return targetExpected
    }

    fun toggleWaterReminder(enabled: Boolean, intervalMins: Int = _waterReminderIntervalMins.value) {
        _waterReminderEnabled.value = enabled
        _waterReminderIntervalMins.value = intervalMins
        prefs.edit()
            .putBoolean("water_reminder_enabled", enabled)
            .putInt("water_reminder_interval_mins", intervalMins)
            .apply()

        if (enabled) {
            scheduleAllSmartWaterSlots()
        } else {
            cancelAllSmartWaterReminders()
        }
    }

    fun sendTestWaterNotification() {
        try {
            val intent = Intent(context, WinterArcAlarmReceiver::class.java).apply {
                putExtra(WinterArcAlarmReceiver.EXTRA_REMINDER_TYPE, "water")
                putExtra(WinterArcAlarmReceiver.EXTRA_WATER_AMOUNT, 350)
                putExtra(WinterArcAlarmReceiver.EXTRA_TASK_TITLE, "স্মার্ট হাইড্রেশন অ্যালার্ট")
                putExtra(WinterArcAlarmReceiver.EXTRA_TASK_ID, 9991)
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            android.util.Log.e("WinterArc", "Test water notification failed", e)
        }
    }

    fun sendTestTaskNotification(taskTitle: String = "ডেইলি ফোকাস ও ওয়ার্কআউট প্রটোকল") {
        try {
            val intent = Intent(context, WinterArcAlarmReceiver::class.java).apply {
                putExtra(WinterArcAlarmReceiver.EXTRA_REMINDER_TYPE, "task")
                putExtra(WinterArcAlarmReceiver.EXTRA_TASK_TITLE, taskTitle)
                putExtra(WinterArcAlarmReceiver.EXTRA_TASK_TIME, "এখনই")
                putExtra(WinterArcAlarmReceiver.EXTRA_TASK_ID, 9992)
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            android.util.Log.e("WinterArc", "Test task notification failed", e)
        }
    }

    fun scheduleAllSmartWaterSlots() {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val nowMs = System.currentTimeMillis()

            smartWaterSlots.forEach { slot ->
                val targetCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, slot.hour)
                    set(Calendar.MINUTE, slot.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= nowMs) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }

                val intent = Intent(context, WinterArcAlarmReceiver::class.java).apply {
                    putExtra(WinterArcAlarmReceiver.EXTRA_REMINDER_TYPE, "water")
                    putExtra(WinterArcAlarmReceiver.EXTRA_WATER_AMOUNT, slot.amountMl)
                    putExtra(WinterArcAlarmReceiver.EXTRA_TASK_TITLE, "${slot.timeLabel} - ${slot.note}")
                    putExtra(WinterArcAlarmReceiver.EXTRA_TASK_ID, 9000 + slot.id)
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
                    } catch (ex: Exception) {
                        android.util.Log.e("WinterArc", "Fallback water slot alarm schedule failed", ex)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WinterArc", "Schedule all smart water slots failed", e)
        }
    }

    fun toggleArcNotifications(enabled: Boolean) {
        _arcNotificationsEnabled.value = enabled
        prefs.edit().putBoolean("arc_notifications_enabled", enabled).apply()
        if (enabled) {
            scheduleAllSmartWaterSlots()
            scheduleAllWorkoutsAlarms()
            scheduleAllTaskAlarms()
        } else {
            cancelAllSmartWaterReminders()
            cancelAllWorkoutReminders()
            cancelAllTaskReminders()
        }
    }

    fun scheduleAllWorkoutsAlarms() {
        if (!_arcNotificationsEnabled.value) return
        workouts.value.forEach {
            if (!it.isCompleted) {
                scheduleWorkoutAlarm(it)
            }
        }
    }

    fun cancelAllWorkoutReminders() {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            workouts.value.forEach {
                val intent = Intent(context, WinterArcAlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    it.notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
        } catch (e: Exception) {}
    }

    fun scheduleAllTaskAlarms() {
        if (!_arcNotificationsEnabled.value) return
        dayTasks.value.forEach {
            if (!it.isCompleted && it.notificationScheduled) {
                val cal = parseTimeToCalendar(it.scheduledTime)
                if (cal != null) {
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val minute = cal.get(Calendar.MINUTE)
                    scheduleTaskAlarm(it, hour, minute, it.targetDate)
                }
            }
        }
    }

    fun cancelAllTaskReminders() {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            dayTasks.value.forEach {
                val intent = Intent(context, WinterArcAlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    it.notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
        } catch (e: Exception) {}
    }

    private fun cancelAllSmartWaterReminders() {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            smartWaterSlots.forEach { slot ->
                val intent = Intent(context, WinterArcAlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    9000 + slot.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
        } catch (e: Exception) {}
    }

    fun setTimelineMode(mode: String, customEndDateMs: Long = 0L) {
        _timelineMode.value = mode
        _customTargetDateMs.value = customEndDateMs
        prefs.edit()
            .putString("timeline_mode", mode)
            .putLong("custom_target_date_ms", customEndDateMs)
            .apply()
    }

    // Toggle challenge completion
    fun toggleChallenge(challenge: WinterArcChallenge) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = challenge.copy(
                isCompleted = !challenge.isCompleted,
                completedDate = if (!challenge.isCompleted) getTodayDateStr() else ""
            )
            dao.updateChallenge(updated)
            updateTodayDayRecord()
        }
    }

    fun updateChallengeTime(challenge: WinterArcChallenge, newTime: String, customValue: String = challenge.customValue) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = challenge.copy(targetTime = newTime, customValue = customValue)
            dao.updateChallenge(updated)
            syncToCloud()
        }
    }

    fun addCustomChallenge(title: String, subtitle: String, category: String, targetTime: String, customValue: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newChallenge = WinterArcChallenge(
                id = "custom_" + System.currentTimeMillis(),
                title = title,
                subtitle = subtitle,
                category = category,
                targetTime = targetTime,
                customValue = customValue,
                isCustom = true,
                orderIndex = 100
            )
            dao.insertChallenge(newChallenge)
            syncToCloud()
        }
    }

    fun deleteChallenge(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteChallenge(id)
            val uid = getEffectiveUid()
            try {
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .collection("winter_arc_challenges").document(id)
                    .delete()
            } catch (e: Exception) {}
            updateTodayDayRecord()
            syncToCloud()
        }
    }

    // Workouts management
    fun toggleWorkout(workout: WinterArcWorkoutItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = workout.copy(isCompleted = !workout.isCompleted)
            dao.updateWorkout(updated)
            updateTodayDayRecord()
            syncToCloud()
        }
    }

    fun addWorkout(name: String, sets: Int, reps: String, time: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val notifId = (System.currentTimeMillis() % 100000).toInt() + 2000
            val item = WinterArcWorkoutItem(
                name = name,
                sets = sets,
                repsOrDuration = reps,
                targetTime = time,
                dateStr = getTodayDateStr(),
                notificationScheduled = true,
                notificationId = notifId
            )
            val id = dao.insertWorkout(item)
            scheduleWorkoutAlarm(item.copy(id = id.toInt()))
            syncToCloud()
        }
    }

    fun updateWorkout(workout: WinterArcWorkoutItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateWorkout(workout)
            scheduleWorkoutAlarm(workout)
            syncToCloud()
        }
    }

    fun deleteWorkout(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            cancelWorkoutAlarm(id)
            dao.deleteWorkout(id)
            val uid = getEffectiveUid()
            try {
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .collection("winter_arc_workouts").document(id.toString())
                    .delete()
            } catch (e: Exception) {}
            syncToCloud()
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

    private fun scheduleWorkoutAlarm(workout: WinterArcWorkoutItem) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val calendar = parseTimeToCalendar(workout.targetTime) ?: return

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                // If today's time has already passed, schedule for tomorrow
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(context, WinterArcAlarmReceiver::class.java).apply {
                putExtra(WinterArcAlarmReceiver.EXTRA_REMINDER_TYPE, "workout")
                putExtra(WinterArcAlarmReceiver.EXTRA_TASK_ID, workout.notificationId)
                putExtra(WinterArcAlarmReceiver.EXTRA_TASK_TITLE, workout.name)
                putExtra(WinterArcAlarmReceiver.EXTRA_TASK_TIME, workout.targetTime)
                putExtra(WinterArcAlarmReceiver.EXTRA_WORKOUT_SETS, workout.sets)
                putExtra(WinterArcAlarmReceiver.EXTRA_WORKOUT_REPS, workout.repsOrDuration)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                workout.notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } catch (e: Exception) {
                try {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } catch (ex: Exception) {
                    android.util.Log.e("WinterArc", "Workout fallback alarm failed", ex)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WinterArc", "Workout alarm failed", e)
        }
    }

    private fun cancelWorkoutAlarm(workoutId: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, WinterArcAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                workoutId + 2000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {}
    }

    private fun parseTimeToCalendar(timeStr: String): Calendar? {
        return try {
            val cal = Calendar.getInstance()
            val engStr = convertBanglaDigitsToEnglish(timeStr)
            val clean = engStr.trim().uppercase(Locale.US)
            val isPm = clean.endsWith("PM")
            val isAm = clean.endsWith("AM")
            val rawTime = clean.replace("AM", "").replace("PM", "").trim()
            val parts = rawTime.split(":")
            if (parts.size >= 2) {
                var hour = parts[0].trim().toInt()
                val min = parts[1].trim().toInt()
                if (isPm && hour < 12) hour += 12
                if (isAm && hour == 12) hour = 0
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, min)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Day Planner & Night Blueprint
    fun addDayTask(title: String, time: String, targetDate: String, hour: Int, minute: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = WinterArcDayTask(
                title = title,
                scheduledTime = time,
                targetDate = targetDate,
                notificationScheduled = true,
                notificationId = (System.currentTimeMillis() % 100000).toInt()
            )
            val id = dao.insertDayTask(task)
            scheduleTaskAlarm(task.copy(id = id.toInt()), hour, minute, targetDate)
            syncToCloud()
        }
    }

    fun toggleDayTask(task: WinterArcDayTask) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateDayTask(task.copy(isCompleted = !task.isCompleted))
            syncToCloud()
        }
    }

    fun updateDayTask(task: WinterArcDayTask, hour: Int = 0, minute: Int = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateDayTask(task)
            if (task.notificationScheduled && (hour > 0 || minute > 0)) {
                scheduleTaskAlarm(task, hour, minute, task.targetDate)
            }
            syncToCloud()
        }
    }

    fun deleteDayTask(task: WinterArcDayTask) {
        viewModelScope.launch(Dispatchers.IO) {
            cancelTaskAlarm(task)
            dao.deleteDayTask(task.id)
            val uid = getEffectiveUid()
            try {
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .collection("winter_arc_tasks").document(task.id.toString())
                    .delete()
            } catch (e: Exception) {}
            syncToCloud()
        }
    }

    private fun getEffectiveUid(): String {
        val authUid = FirebaseAuth.getInstance().currentUser?.uid
        if (!authUid.isNullOrBlank()) return authUid
        var deviceUid = prefs.getString("device_sync_uid", null)
        if (deviceUid.isNullOrBlank()) {
            deviceUid = "usr_" + java.util.UUID.randomUUID().toString().replace("-", "").take(12)
            prefs.edit().putString("device_sync_uid", deviceUid).apply()
        }
        return deviceUid
    }

    fun saveRunRecord(record: WinterArcRunRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertRun(record)
            val uid = getEffectiveUid()
            try {
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .collection("winter_arc_runs").document(record.id.toString())
                    .set(record, SetOptions.merge())
            } catch (e: Exception) {
                android.util.Log.e("WinterArcCloud", "saveRunRecord error", e)
            }
            syncToCloud()
        }
    }

    private fun scheduleTaskAlarm(task: WinterArcDayTask, hour: Int, minute: Int, targetDateStr: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val calendar = Calendar.getInstance()
            val parts = targetDateStr.split("-")
            if (parts.size == 3) {
                calendar.set(Calendar.YEAR, parts[0].toInt())
                calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
                calendar.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
            }
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val triggerTime = calendar.timeInMillis
            if (triggerTime <= System.currentTimeMillis()) return

            val intent = Intent(context, WinterArcAlarmReceiver::class.java).apply {
                putExtra(WinterArcAlarmReceiver.EXTRA_REMINDER_TYPE, "task")
                putExtra(WinterArcAlarmReceiver.EXTRA_TASK_ID, task.notificationId)
                putExtra(WinterArcAlarmReceiver.EXTRA_TASK_TITLE, task.title)
                putExtra(WinterArcAlarmReceiver.EXTRA_TASK_TIME, task.scheduledTime)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } catch (e: Exception) {
                try {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } catch (ex: Exception) {
                    android.util.Log.e("WinterArc", "Task fallback alarm failed", ex)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WinterArc", "Alarm schedule failed", e)
        }
    }

    private fun cancelTaskAlarm(task: WinterArcDayTask) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, WinterArcAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {}
    }

    // Daily records for Analysis Calendar
    private fun updateTodayDayRecord() {
        viewModelScope.launch(Dispatchers.IO) {
            val today = getTodayDateStr()
            val currentChallenges = challenges.value
            val currentWorkouts = workouts.value
            val completedCount = currentChallenges.count { it.isCompleted }
            val totalCount = currentChallenges.size.coerceAtLeast(1)
            val isSuccess = completedCount >= (totalCount * 0.7f).toInt()
            val anyWorkoutDone = currentWorkouts.any { it.isCompleted }

            val record = WinterArcDayRecord(
                dateStr = today,
                completedChallengesCount = completedCount,
                totalChallengesCount = totalCount,
                isCompleted = isSuccess,
                isMissed = false,
                waterIntakeMl = _waterIntakeMl.value,
                runDistanceKm = 0f,
                workoutDone = anyWorkoutDone
            )
            dao.insertOrUpdateDayRecord(record)
            syncToCloud()
        }
    }

    private fun seedCalendarHistoryIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            val todayStr = getTodayDateStr()
            val existingToday = dao.getDayRecord(todayStr)
            if (existingToday == null) {
                val record = WinterArcDayRecord(
                    dateStr = todayStr,
                    completedChallengesCount = 0,
                    totalChallengesCount = 12,
                    isCompleted = false,
                    isMissed = false,
                    waterIntakeMl = _waterIntakeMl.value,
                    runDistanceKm = 0f,
                    workoutDone = false
                )
                dao.insertOrUpdateDayRecord(record)
            }
        }
    }

    private fun initializeDefaultChallengesIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = mutableListOf(
                WinterArcChallenge(
                    id = "early_rise",
                    title = "ভোর ৫:০০ টায় ঘুম থেকে ওঠা (Early Monk Rise)",
                    subtitle = "অলসতাকে গুঁড়িয়ে দিন, সকালের শান্ত আলো ও নামাজের সাথে দিন শুরু।",
                    category = "discipline",
                    targetTime = "05:00 AM",
                    orderIndex = 1
                ),
                WinterArcChallenge(
                    id = "morning_exercise",
                    title = "সকালের শরীরচর্চা ও মর্নিং ফিটনেস (Morning Fitness)",
                    subtitle = "হালকা জগিং, কার্ডিও বা স্ট্রেচিং দিয়ে শরীর ও মন সতেজ রাখুন।",
                    category = "fitness",
                    targetTime = "05:45 AM",
                    customValue = "30 mins",
                    orderIndex = 2
                ),
                WinterArcChallenge(
                    id = "workout_power",
                    title = "শক্তি চর্চা ও হোম ওয়ার্কআউট (Strength Protocol)",
                    subtitle = "১০০টি পুশ-আপ, পুল-আপ বা জিম ট্রেনিং সম্পন্ন করুন।",
                    category = "fitness",
                    targetTime = "06:30 AM",
                    customValue = "100 push-ups / 3 sets",
                    orderIndex = 3
                ),
                WinterArcChallenge(
                    id = "water_hydration",
                    title = "দৈনিক ৩.৫ লিটার বিশুদ্ধ পানি পান (Hydration)",
                    subtitle = "শরীরকে টক্সিনমুক্ত ও শক্তিপূর্ণ রাখতে পর্যাপ্ত পানি পান করুন।",
                    category = "water",
                    targetTime = "সারাদিন",
                    customValue = "3500 ml",
                    orderIndex = 4
                ),
                WinterArcChallenge(
                    id = "cold_shower",
                    title = "শীতল জলের স্নান (Cold Shower Reset)",
                    subtitle = "ডোপামিন রিসেট এবং লৌহকঠিন ইচ্ছাশক্তি (Mental Toughness)।",
                    category = "discipline",
                    targetTime = "07:30 AM",
                    orderIndex = 5
                ),
                WinterArcChallenge(
                    id = "purity_clean",
                    title = "১০০% দৃষ্টি সংযম ও নো-ফ্যাপ (Zero Relapse)",
                    subtitle = "কোনো অশ্লীল কন্টেন্ট বা অনৈতিক চিন্তা স্পর্শ করবে না। চরিত্রই শক্তি।",
                    category = "spiritual",
                    targetTime = "সারাদিন",
                    orderIndex = 6
                ),
                WinterArcChallenge(
                    id = "screen_detox",
                    title = "ডিজিটাল ডিটক্স (Strict Screen Time Limit)",
                    subtitle = "ফালতু রিলস বা ভিডিও স্ক্রোলিং বন্ধ। স্ক্রিন টাইম ২ ঘণ্টার নিচে রাখুন।",
                    category = "mindset",
                    targetTime = "সারাদিন",
                    customValue = "< 2 hours",
                    orderIndex = 7
                ),
                WinterArcChallenge(
                    id = "book_reading",
                    title = "২০ পৃষ্ঠা বই পাঠ (Deep Reading)",
                    subtitle = "জ্ঞান, মনন ও গভীর চিন্তাশক্তি বৃদ্ধি করতে আত্মউন্নয়ন বই পড়ুন।",
                    category = "mindset",
                    targetTime = "09:00 PM",
                    customValue = "20 pages",
                    orderIndex = 8
                ),
                WinterArcChallenge(
                    id = "salah_focus",
                    title = "৫ ওয়াক্ত নামাজ জামাতে আদায় (Spiritual Core)",
                    subtitle = "আল্লাহর সাথে অটুট সংযোগ এবং আত্মার চিরস্থায়ী প্রশান্তি।",
                    category = "spiritual",
                    targetTime = "ওয়াক্ত অনুযায়ী",
                    orderIndex = 9
                ),
                WinterArcChallenge(
                    id = "night_planner",
                    title = "আগামীকালের রুটিন ব্লুপ্রিন্ট (Night Blueprint)",
                    subtitle = "ঘুমানোর আগে পরের সারাদিনের টাইম-ব্লক শিডিউল নির্ধারণ করা।",
                    category = "discipline",
                    targetTime = "10:00 PM",
                    orderIndex = 10
                ),
                WinterArcChallenge(
                    id = "clean_diet",
                    title = "নো জাঙ্ক ফুড ও হাই প্রোটিন খাবার (Clean Fuel)",
                    subtitle = "চিনি, অতিরিক্ত তেল ও প্রসেসড খাবার বর্জন করে পুষ্টিকর খাবার গ্রহণ।",
                    category = "fitness",
                    targetTime = "সারাদিন",
                    orderIndex = 11
                ),
                WinterArcChallenge(
                    id = "no_complaints",
                    title = "নো এক্সকিউজ ও অভিযোগহীন দিন (Iron Stoicism)",
                    subtitle = "কষ্ট হলেও মুখ বুজে নিজের সেরাটা দিন। সত্যিকারের পুরুষের মতো অটল থাকুন।",
                    category = "mindset",
                    targetTime = "সারাদিন",
                    orderIndex = 12
                )
            )
            dao.insertChallenges(list)
        }
    }

    private fun initializeDefaultWorkoutsIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            val defaults = listOf(
                WinterArcWorkoutItem(name = "Push-ups (বুকডন)", sets = 4, repsOrDuration = "25 reps", targetTime = "06:30 AM", notificationId = 2001),
                WinterArcWorkoutItem(name = "Pull-ups (চিন-আপ)", sets = 3, repsOrDuration = "10 reps", targetTime = "06:45 AM", notificationId = 2002),
                WinterArcWorkoutItem(name = "Bodyweight Squats (স্কোয়াট)", sets = 4, repsOrDuration = "25 reps", targetTime = "07:00 AM", notificationId = 2003),
                WinterArcWorkoutItem(name = "Plank (কোর স্ট্যাবিলিটি)", sets = 3, repsOrDuration = "60 sec", targetTime = "07:15 AM", notificationId = 2004),
                WinterArcWorkoutItem(name = "Dumbbell Curls / Shoulder Press", sets = 3, repsOrDuration = "12 reps", targetTime = "07:25 AM", notificationId = 2005)
            )
            defaults.forEach { 
                dao.insertWorkout(it) 
                scheduleWorkoutAlarm(it)
            }
        }
    }

    fun syncToCloud() {
        val uid = getEffectiveUid()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val chList = dao.getChallengesList()
                val wList = dao.getWorkoutsList()
                val tList = dao.getDayTasksList()
                val rList = dao.getDayRecordsList()

                val payload = hashMapOf<String, Any>(
                    "waterIntakeMl" to _waterIntakeMl.value,
                    "waterTargetMl" to _waterTargetMl.value,
                    "waterAutoMode" to _waterAutoModeEnabled.value,
                    "timelineMode" to _timelineMode.value,
                    "customTargetDateMs" to _customTargetDateMs.value,
                    "updatedAt" to System.currentTimeMillis(),
                    "waterLogs" to _waterLogs.value.map { wl ->
                        hashMapOf(
                            "id" to wl.id,
                            "amountMl" to wl.amountMl,
                            "timeFormatted" to wl.timeFormatted
                        )
                    },
                    "challenges" to chList.map { ch ->
                        hashMapOf(
                            "id" to ch.id,
                            "title" to ch.title,
                            "subtitle" to ch.subtitle,
                            "category" to ch.category,
                            "targetTime" to ch.targetTime,
                            "customValue" to ch.customValue,
                            "isCompleted" to ch.isCompleted,
                            "completedDate" to ch.completedDate,
                            "orderIndex" to ch.orderIndex,
                            "isCustom" to ch.isCustom
                        )
                    },
                    "workouts" to wList.map { w ->
                        hashMapOf(
                            "id" to w.id,
                            "name" to w.name,
                            "sets" to w.sets,
                            "repsOrDuration" to w.repsOrDuration,
                            "targetTime" to w.targetTime,
                            "isCompleted" to w.isCompleted,
                            "dateStr" to w.dateStr
                        )
                    },
                    "dayTasks" to tList.map { t ->
                        hashMapOf(
                            "id" to t.id,
                            "title" to t.title,
                            "scheduledTime" to t.scheduledTime,
                            "isCompleted" to t.isCompleted,
                            "targetDate" to t.targetDate
                        )
                    },
                    "dayRecords" to rList.map { r ->
                        hashMapOf(
                            "dateStr" to r.dateStr,
                            "completedChallengesCount" to r.completedChallengesCount,
                            "totalChallengesCount" to r.totalChallengesCount,
                            "isCompleted" to r.isCompleted,
                            "isMissed" to r.isMissed,
                            "waterIntakeMl" to r.waterIntakeMl,
                            "runDistanceKm" to r.runDistanceKm.toDouble(),
                            "workoutDone" to r.workoutDone
                        )
                    }
                )

                db.collection("users").document(uid)
                    .collection("winter_arc").document("state")
                    .set(payload, SetOptions.merge())

                // Also write each workout, challenge, and task into Firestore subcollections
                for (w in wList) {
                    db.collection("users").document(uid)
                        .collection("winter_arc_workouts").document(w.id.toString())
                        .set(w, SetOptions.merge())
                }
                for (ch in chList) {
                    db.collection("users").document(uid)
                        .collection("winter_arc_challenges").document(ch.id)
                        .set(ch, SetOptions.merge())
                }
                for (t in tList) {
                    db.collection("users").document(uid)
                        .collection("winter_arc_tasks").document(t.id.toString())
                        .set(t, SetOptions.merge())
                }
            } catch (e: Exception) {
                android.util.Log.e("WinterArcCloud", "syncToCloud error", e)
            }
        }
    }

    fun syncFromCloud() {
        val uid = getEffectiveUid()
        val db = FirebaseFirestore.getInstance()
        try {
            snapshotListener?.remove()
        } catch (e: Exception) {}

        snapshotListener = db.collection("users").document(uid)
            .collection("winter_arc").document("state")
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    android.util.Log.e("WinterArcCloud", "Snapshot listener error", error)
                    return@addSnapshotListener
                }
                if (doc != null && doc.exists()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val water = (doc.getLong("waterIntakeMl") ?: 0L).toInt()
                            val target = (doc.getLong("waterTargetMl") ?: 3500L).toInt()
                            val autoMode = doc.getBoolean("waterAutoMode") ?: false
                            val tlMode = doc.getString("timelineMode") ?: "90_days"
                            val customDate = doc.getLong("customTargetDateMs") ?: 0L

                            _waterIntakeMl.value = water
                            _waterTargetMl.value = target
                            _waterAutoModeEnabled.value = autoMode
                            _timelineMode.value = tlMode
                            _customTargetDateMs.value = customDate

                            prefs.edit()
                                .putInt("water_current_ml", water)
                                .putInt("water_target_ml", target)
                                .putBoolean("water_auto_mode", autoMode)
                                .putString("timeline_mode", tlMode)
                                .putLong("custom_target_date_ms", customDate)
                                .apply()

                            @Suppress("UNCHECKED_CAST")
                            val rawWaterLogs = doc.get("waterLogs") as? List<Map<String, Any>>
                            if (rawWaterLogs != null) {
                                val loadedLogs = rawWaterLogs.mapNotNull { m ->
                                    val amount = (m["amountMl"] as? Long)?.toInt() ?: return@mapNotNull null
                                    val time = m["timeFormatted"] as? String ?: ""
                                    val id = (m["id"] as? Long) ?: System.currentTimeMillis()
                                    WaterLogEntry(id = id, amountMl = amount, timeFormatted = time)
                                }
                                _waterLogs.value = loadedLogs
                                saveWaterLogs(loadedLogs)
                            }

                            @Suppress("UNCHECKED_CAST")
                            val rawChallenges = doc.get("challenges") as? List<Map<String, Any>>
                            rawChallenges?.forEach { map ->
                                val chId = map["id"] as? String ?: return@forEach
                                val ch = WinterArcChallenge(
                                    id = chId,
                                    title = map["title"] as? String ?: "",
                                    subtitle = map["subtitle"] as? String ?: "",
                                    category = map["category"] as? String ?: "discipline",
                                    targetTime = map["targetTime"] as? String ?: "",
                                    customValue = map["customValue"] as? String ?: "",
                                    isCompleted = map["isCompleted"] as? Boolean ?: false,
                                    completedDate = map["completedDate"] as? String ?: "",
                                    orderIndex = (map["orderIndex"] as? Long)?.toInt() ?: 0,
                                    isCustom = map["isCustom"] as? Boolean ?: false
                                )
                                dao.insertChallenge(ch)
                            }

                            @Suppress("UNCHECKED_CAST")
                            val rawWorkouts = doc.get("workouts") as? List<Map<String, Any>>
                            rawWorkouts?.forEach { map ->
                                val wId = (map["id"] as? Long)?.toInt() ?: 0
                                val w = WinterArcWorkoutItem(
                                    id = wId,
                                    name = map["name"] as? String ?: "",
                                    sets = (map["sets"] as? Long)?.toInt() ?: 3,
                                    repsOrDuration = map["repsOrDuration"] as? String ?: "",
                                    targetTime = map["targetTime"] as? String ?: "",
                                    isCompleted = map["isCompleted"] as? Boolean ?: false,
                                    dateStr = map["dateStr"] as? String ?: getTodayDateStr()
                                )
                                dao.insertWorkout(w)
                            }

                            @Suppress("UNCHECKED_CAST")
                            val rawTasks = doc.get("dayTasks") as? List<Map<String, Any>>
                            rawTasks?.forEach { map ->
                                val tId = (map["id"] as? Long)?.toInt() ?: 0
                                val t = WinterArcDayTask(
                                    id = tId,
                                    title = map["title"] as? String ?: "",
                                    scheduledTime = map["scheduledTime"] as? String ?: "",
                                    targetDate = map["targetDate"] as? String ?: getTodayDateStr(),
                                    isCompleted = map["isCompleted"] as? Boolean ?: false
                                )
                                dao.insertDayTask(t)
                            }

                            @Suppress("UNCHECKED_CAST")
                            val rawRecords = doc.get("dayRecords") as? List<Map<String, Any>>
                            rawRecords?.forEach { map ->
                                val dStr = map["dateStr"] as? String ?: return@forEach
                                val r = WinterArcDayRecord(
                                    dateStr = dStr,
                                    completedChallengesCount = (map["completedChallengesCount"] as? Long)?.toInt() ?: 0,
                                    totalChallengesCount = (map["totalChallengesCount"] as? Long)?.toInt() ?: 12,
                                    isCompleted = map["isCompleted"] as? Boolean ?: false,
                                    isMissed = map["isMissed"] as? Boolean ?: false,
                                    waterIntakeMl = (map["waterIntakeMl"] as? Long)?.toInt() ?: 0,
                                    runDistanceKm = (map["runDistanceKm"] as? Number)?.toFloat() ?: 0f,
                                    workoutDone = map["workoutDone"] as? Boolean ?: false
                                )
                                dao.insertOrUpdateDayRecord(r)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("WinterArcCloud", "syncFromCloud parse error", e)
                        }
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            snapshotListener?.remove()
        } catch (e: Exception) {}
        try {
            authListener?.let { FirebaseAuth.getInstance().removeAuthStateListener(it) }
        } catch (e: Exception) {}
    }
}

data class WaterSmartSlot(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val amountMl: Int,
    val timeLabel: String,
    val note: String
)

val smartWaterSlots = listOf(
    WaterSmartSlot(1, 5, 5, 400, "০৫:০৫ AM", "ঘুম ভাঙার পর প্রথম গ্লাস — মেটাবলিজম ও বডি অ্যাক্টিভেশন"),
    WaterSmartSlot(2, 7, 0, 350, "০৭:০০ AM", "সকালের ব্যায়াম ও ফজর পরবর্তী হাইড্রেট"),
    WaterSmartSlot(3, 9, 0, 350, "০৯:০০ AM", "সকালের নাস্তার পর কর্মদিবসের শুরুতে ফোকাস বুস্ট"),
    WaterSmartSlot(4, 11, 30, 350, "১১:৩০ AM", "মিড-ডে এনার্জি রিফ্রেশ ও শরীর ডিটক্স"),
    WaterSmartSlot(5, 13, 30, 400, "০১:৩০ PM", "জোহর সালাত ও দুপুরের খাবারের পূর্বের হাইড্রেট"),
    WaterSmartSlot(6, 15, 30, 350, "০৩:৩০ PM", "পোস্ট-লাঞ্চ অলসতা ও ক্লান্তি দূরীকরণ"),
    WaterSmartSlot(7, 17, 30, 350, "০৫:৩০ PM", "আসর পরবর্তী ও সন্ধ্যার দৌড়/ব্যায়ামের প্রস্তুতি"),
    WaterSmartSlot(8, 19, 45, 350, "০৭:৪৫ PM", "মাগরিবের পর ও রাতের খাবারের পূর্বে হাইড্রেট"),
    WaterSmartSlot(9, 22, 0, 250, "১০:০০ PM", "ঘুমানোর পূর্বে শেষ সিপ — সারাদিনের হাইড্রেশন সমাপ্তি")
)
