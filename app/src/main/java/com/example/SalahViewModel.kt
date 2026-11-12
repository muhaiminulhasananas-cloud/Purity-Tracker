package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SalahViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("salah_prefs", Context.MODE_PRIVATE)
    private val prayerPrefs = application.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var snapshotListener: ListenerRegistration? = null
    private var lastBoundUid: String? = null
    
    private val _todayTimings = MutableStateFlow<Map<String, String>>(emptyMap())
    val todayTimings = _todayTimings.asStateFlow()
    
    private val prefListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key?.startsWith("time_") == true) {
            updateTimingsFromPrefs()
        }
    }
    
    private fun updateTimingsFromPrefs() {
        val map = mutableMapOf<String, String>()
        listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Sunset", "Maghrib", "Isha").forEach { p ->
            prayerPrefs.getString("time_$p", null)?.let { time ->
                map[p] = time
            }
        }
        _todayTimings.value = map
    }

    // Store checklist as bitmask (6 bits):
    // 0: Fajr, 1: Dhuhr, 2: Asr, 3: Maghrib, 4: Isha, 5: Tahajjud
    private val _todaySalahs = MutableStateFlow(getTodaySalahsFromPrefs())
    val todaySalahs = _todaySalahs.asStateFlow()

    private val _past7DaysCounts = MutableStateFlow(getPast7DaysFromPrefs())
    val past7DaysCounts = _past7DaysCounts.asStateFlow()
    
    init {
        checkAndResetIfNeeded()
        updateTimingsFromPrefs()
        prayerPrefs.registerOnSharedPreferenceChangeListener(prefListener)
        setupFirebaseAuthAndCloudSync()
    }

    override fun onCleared() {
        super.onCleared()
        prayerPrefs.unregisterOnSharedPreferenceChangeListener(prefListener)
        try {
            snapshotListener?.remove()
        } catch (e: Exception) {
            Log.e("SalahViewModel", "Error removing snapshot listener", e)
        }
        try {
            authListener?.let { auth.removeAuthStateListener(it) }
        } catch (e: Exception) {
            Log.e("SalahViewModel", "Error removing auth listener", e)
        }
    }

    private fun getCurrentDayKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.DAY_OF_YEAR)}"
    }

    private fun getTodaySalahsFromPrefs(): BooleanArray {
        val key = "salah_" + getCurrentDayKey()
        val bitmask = prefs.getInt(key, 0)
        return BooleanArray(6) { i -> (bitmask and (1 shl i)) != 0 }
    }
    
    private fun getPast7DaysFromPrefs(): List<Int> {
        val counts = mutableListOf<Int>()
        // Go from 6 days ago to today
        for (i in 6 downTo 0) {
            val pastCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val key = "salah_" + "${pastCal.get(Calendar.YEAR)}_${pastCal.get(Calendar.DAY_OF_YEAR)}"
            val bitmask = prefs.getInt(key, 0)
            var count = 0
            for (j in 0..4) { // 5 obligatory daily prayers count for daily goal
                if ((bitmask and (1 shl j)) != 0) count++
            }
            counts.add(count)
        }
        return counts
    }
    
    fun toggleSalah(index: Int) {
        if (index !in 0..5) return
        val current = _todaySalahs.value.clone()
        current[index] = !current[index]
        _todaySalahs.value = current
        
        var bitmask = 0
        for (i in 0..5) {
            if (current[i]) bitmask = bitmask or (1 shl i)
        }
        
        val dayKey = getCurrentDayKey()
        val key = "salah_" + dayKey
        prefs.edit().putInt(key, bitmask).apply()
        
        _past7DaysCounts.value = getPast7DaysFromPrefs()

        // Sync immediately to Firebase Firestore
        syncSalahToFirebase(dayKey, bitmask, current)
    }
    
    private fun checkAndResetIfNeeded() {
        val lastDay = prefs.getString("last_opened_day", "")
        val currentDay = getCurrentDayKey()
        if (lastDay != currentDay) {
            // New day
            _todaySalahs.value = getTodaySalahsFromPrefs()
            _past7DaysCounts.value = getPast7DaysFromPrefs()
            prefs.edit().putString("last_opened_day", currentDay).apply()
        }
    }
    
    fun getDayName(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        return days[cal.get(Calendar.DAY_OF_WEEK) - 1]
    }

    // --- Firebase Sync Implementation ---

    private fun getEffectiveUid(): String {
        val authUid = auth.currentUser?.uid
        if (!authUid.isNullOrBlank()) return authUid
        var deviceUid = prefs.getString("device_sync_uid", null)
        if (deviceUid.isNullOrBlank()) {
            deviceUid = "dev_" + java.util.UUID.randomUUID().toString().replace("-", "").take(12)
            prefs.edit().putString("device_sync_uid", deviceUid).apply()
        }
        return deviceUid
    }

    private fun setupFirebaseAuthAndCloudSync() {
        authListener = FirebaseAuth.AuthStateListener { currentAuth ->
            val user = currentAuth.currentUser
            if (user != null) {
                bindCloudSync(user.uid)
            } else {
                val devUid = getEffectiveUid()
                bindCloudSync(devUid)
            }
        }
        try {
            auth.addAuthStateListener(authListener!!)
        } catch (e: Exception) {
            Log.e("SalahViewModel", "Auth listener attach error", e)
        }

        val initialUid = auth.currentUser?.uid ?: getEffectiveUid()
        bindCloudSync(initialUid)
    }

    private fun bindCloudSync(uid: String) {
        if (uid.isBlank()) return
        val isNewUser = (lastBoundUid != null && lastBoundUid != uid)
        lastBoundUid = uid

        try {
            snapshotListener?.remove()
        } catch (e: Exception) {
            Log.e("SalahViewModel", "Error removing previous snapshot listener", e)
        }

        // Upload any existing local records to this user's cloud first
        uploadLocalRecordsToCloud(uid)

        // Listen to cloud state changes in real-time
        snapshotListener = db.collection("users").document(uid)
            .collection("prayer_data").document("state")
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    Log.e("SalahViewModel", "Prayer cloud snapshot error", error)
                    return@addSnapshotListener
                }
                if (doc != null && doc.exists()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val history = doc.get("history") as? Map<*, *>
                            val editor = prefs.edit()
                            var modified = false

                            if (isNewUser) {
                                // Clear old user's salah keys
                                val keysToRemove = prefs.all.keys.filter { it.startsWith("salah_") }
                                keysToRemove.forEach { editor.remove(it) }
                                modified = true
                            }

                            history?.forEach { (k, v) ->
                                if (k is String) {
                                    val cloudMask = (v as? Number)?.toInt() ?: 0
                                    val key = "salah_$k"
                                    val localMask = prefs.getInt(key, 0)
                                    val merged = if (isNewUser) cloudMask else (localMask or cloudMask)
                                    if (merged != localMask || isNewUser) {
                                        editor.putInt(key, merged)
                                        modified = true
                                    }
                                }
                            }

                            val currentDay = getCurrentDayKey()
                            val todayBit = (doc.getLong("todayBitmask") ?: 0L).toInt()
                            if (doc.getString("currentDayKey") == currentDay) {
                                val localToday = prefs.getInt("salah_$currentDay", 0)
                                val mergedToday = if (isNewUser) todayBit else (localToday or todayBit)
                                if (mergedToday != localToday || isNewUser) {
                                    editor.putInt("salah_$currentDay", mergedToday)
                                    modified = true
                                }
                            }

                            if (modified) {
                                editor.apply()
                                _todaySalahs.value = getTodaySalahsFromPrefs()
                                _past7DaysCounts.value = getPast7DaysFromPrefs()
                            }
                        } catch (e: Exception) {
                            Log.e("SalahViewModel", "Error processing prayer snapshot", e)
                        }
                    }
                }
            }

        // Also fetch from the prayer_records collection (ensures full history is fetched)
        db.collection("users").document(uid)
            .collection("prayer_records")
            .limit(30)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    viewModelScope.launch(Dispatchers.IO) {
                        val editor = prefs.edit()
                        var modified = false
                        for (doc in querySnapshot.documents) {
                            val dKey = doc.getString("dayKey") ?: doc.id
                            val bitmask = (doc.getLong("bitmask") ?: 0L).toInt()
                            val local = prefs.getInt("salah_$dKey", 0)
                            val merged = local or bitmask
                            if (merged != local) {
                                editor.putInt("salah_$dKey", merged)
                                modified = true
                            }
                        }
                        if (modified) {
                            editor.apply()
                            _todaySalahs.value = getTodaySalahsFromPrefs()
                            _past7DaysCounts.value = getPast7DaysFromPrefs()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("SalahViewModel", "Error fetching prayer records", e)
            }
    }

    private fun syncSalahToFirebase(dayKey: String, bitmask: Int, states: BooleanArray) {
        val uid = auth.currentUser?.uid ?: getEffectiveUid()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cal = Calendar.getInstance()
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                var count = 0
                for (j in 0..4) {
                    if (states.getOrElse(j) { false }) count++
                }

                // 1. Detailed per-day document
                val recordDoc = hashMapOf<String, Any>(
                    "dayKey" to dayKey,
                    "dateStr" to dateStr,
                    "bitmask" to bitmask,
                    "fajr" to states.getOrElse(0) { false },
                    "dhuhr" to states.getOrElse(1) { false },
                    "asr" to states.getOrElse(2) { false },
                    "maghrib" to states.getOrElse(3) { false },
                    "isha" to states.getOrElse(4) { false },
                    "tahajjud" to states.getOrElse(5) { false },
                    "completedObligatoryCount" to count,
                    "updatedAt" to System.currentTimeMillis()
                )

                db.collection("users").document(uid)
                    .collection("prayer_records").document(dayKey)
                    .set(recordDoc, SetOptions.merge())

                // 2. Summary document with recent history map for fast single-doc restoration
                val historyMap = mutableMapOf<String, Any>()
                for (i in 0..14) {
                    val pastCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                    val pastKey = "${pastCal.get(Calendar.YEAR)}_${pastCal.get(Calendar.DAY_OF_YEAR)}"
                    val pastBitmask = prefs.getInt("salah_$pastKey", 0)
                    if (pastBitmask > 0 || pastKey == dayKey) {
                        historyMap[pastKey] = pastBitmask
                    }
                }

                val stateDoc = hashMapOf<String, Any>(
                    "lastUpdated" to System.currentTimeMillis(),
                    "currentDayKey" to dayKey,
                    "todayBitmask" to bitmask,
                    "history" to historyMap
                )

                db.collection("users").document(uid)
                    .collection("prayer_data").document("state")
                    .set(stateDoc, SetOptions.merge())

            } catch (e: Exception) {
                Log.e("SalahViewModel", "Error saving prayer to Firebase", e)
            }
        }
    }

    private fun uploadLocalRecordsToCloud(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentDay = getCurrentDayKey()
                val todayBitmask = prefs.getInt("salah_$currentDay", 0)
                val historyMap = mutableMapOf<String, Any>()

                for (i in 0..14) {
                    val pastCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                    val pastKey = "${pastCal.get(Calendar.YEAR)}_${pastCal.get(Calendar.DAY_OF_YEAR)}"
                    val pastBitmask = prefs.getInt("salah_$pastKey", 0)
                    if (pastBitmask > 0 || pastKey == currentDay) {
                        historyMap[pastKey] = pastBitmask
                    }
                }

                if (historyMap.isNotEmpty()) {
                    val stateDoc = hashMapOf<String, Any>(
                        "lastUpdated" to System.currentTimeMillis(),
                        "currentDayKey" to currentDay,
                        "todayBitmask" to todayBitmask,
                        "history" to historyMap
                    )
                    db.collection("users").document(uid)
                        .collection("prayer_data").document("state")
                        .set(stateDoc, SetOptions.merge())
                }

                // Also make sure today's record document is stored
                if (todayBitmask > 0) {
                    val states = getTodaySalahsFromPrefs()
                    var count = 0
                    for (j in 0..4) {
                        if (states.getOrElse(j) { false }) count++
                    }
                    val cal = Calendar.getInstance()
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    val recordDoc = hashMapOf<String, Any>(
                        "dayKey" to currentDay,
                        "dateStr" to dateStr,
                        "bitmask" to todayBitmask,
                        "fajr" to states.getOrElse(0) { false },
                        "dhuhr" to states.getOrElse(1) { false },
                        "asr" to states.getOrElse(2) { false },
                        "maghrib" to states.getOrElse(3) { false },
                        "isha" to states.getOrElse(4) { false },
                        "tahajjud" to states.getOrElse(5) { false },
                        "completedObligatoryCount" to count,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    db.collection("users").document(uid)
                        .collection("prayer_records").document(currentDay)
                        .set(recordDoc, SetOptions.merge())
                }
            } catch (e: Exception) {
                Log.e("SalahViewModel", "Error uploading local records to cloud", e)
            }
        }
    }
}
