package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class SalahViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("salah_prefs", Context.MODE_PRIVATE)
    
    // Store checklist as bitmask (5 bits) for simplicity, for the current day.
    // 0: Fajr, 1: Dhuhr, 2: Asr, 3: Maghrib, 4: Isha
    private val _todaySalahs = MutableStateFlow(getTodaySalahsFromPrefs())
    val todaySalahs = _todaySalahs.asStateFlow()

    private val _past7DaysCounts = MutableStateFlow(getPast7DaysFromPrefs())
    val past7DaysCounts = _past7DaysCounts.asStateFlow()
    
    init {
        checkAndResetIfNeeded()
    }

    private fun getCurrentDayKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.DAY_OF_YEAR)}"
    }

    private fun getTodaySalahsFromPrefs(): BooleanArray {
        val key = "salah_" + getCurrentDayKey()
        val bitmask = prefs.getInt(key, 0)
        return BooleanArray(5) { i -> (bitmask and (1 shl i)) != 0 }
    }
    
    private fun getPast7DaysFromPrefs(): List<Int> {
        val cal = Calendar.getInstance()
        val counts = mutableListOf<Int>()
        // Go from 6 days ago to today
        for (i in 6 downTo 0) {
            val pastCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val key = "salah_" + "${pastCal.get(Calendar.YEAR)}_${pastCal.get(Calendar.DAY_OF_YEAR)}"
            val bitmask = prefs.getInt(key, 0)
            var count = 0
            for (j in 0..4) {
                if ((bitmask and (1 shl j)) != 0) count++
            }
            counts.add(count)
        }
        return counts
    }
    
    fun toggleSalah(index: Int) {
        val current = _todaySalahs.value.clone()
        current[index] = !current[index]
        _todaySalahs.value = current
        
        var bitmask = 0
        for (i in 0..4) {
            if (current[i]) bitmask = bitmask or (1 shl i)
        }
        
        val key = "salah_" + getCurrentDayKey()
        prefs.edit().putInt(key, bitmask).apply()
        
        _past7DaysCounts.value = getPast7DaysFromPrefs()
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
}
