package com.example

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class PrayerScheduleItem(
    val key: String,
    val nameEn: String,
    val nameBn: String,
    val descEn: String,
    val descBn: String,
    val startTime12: String,
    val endTime12: String,
    val startTime24: String,
    val endTime24: String,
    val isCheckable: Boolean,
    val checkIndex: Int = -1,
    val iconEmoji: String = "🕌"
)

object PrayerTimeHelper {

    private val sdf24 = SimpleDateFormat("HH:mm", Locale.US)
    private val sdf12 = SimpleDateFormat("hh:mm a", Locale.US)

    fun to12HourFormat(time24: String?, isBn: Boolean = true): String {
        if (time24.isNullOrBlank()) return "--:--"
        return try {
            val date = sdf24.parse(time24) ?: return time24
            val formatted = sdf12.format(date) // e.g. "04:30 AM" or "12:06 PM"
            if (isBn) {
                toBanglaDigits(formatted)
            } else {
                formatted
            }
        } catch (e: Exception) {
            time24
        }
    }

    fun parseToMinutes(time24: String?, defaultHour: Int, defaultMinute: Int): Int {
        if (time24.isNullOrBlank()) return defaultHour * 60 + defaultMinute
        return try {
            val date = sdf24.parse(time24) ?: return defaultHour * 60 + defaultMinute
            val cal = Calendar.getInstance().apply { time = date }
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        } catch (e: Exception) {
            defaultHour * 60 + defaultMinute
        }
    }

    fun minutesTo24(minutes: Int): String {
        val normalized = (minutes % 1440 + 1440) % 1440
        val h = normalized / 60
        val m = normalized % 60
        return String.format(Locale.US, "%02d:%02d", h, m)
    }

    fun getFullPrayerSchedule(timings: Map<String, String>, isBn: Boolean = true): List<PrayerScheduleItem> {
        val fajr24 = timings["Fajr"] ?: "04:30"
        val sunrise24 = timings["Sunrise"] ?: "05:45"
        val dhuhr24 = timings["Dhuhr"] ?: "12:06"
        val asr24 = timings["Asr"] ?: "16:37"
        val sunset24 = timings["Sunset"] ?: "18:25"
        val maghrib24 = timings["Maghrib"] ?: "18:25"
        val isha24 = timings["Isha"] ?: "19:35"

        // Calculate Tahajjud time (Last 3rd of night: between Maghrib and Fajr)
        val fajrMin = parseToMinutes(fajr24, 4, 30)
        val maghribMin = parseToMinutes(maghrib24, 18, 25)
        
        // Night length in minutes
        val nightDuration = (fajrMin + 1440 - maghribMin) % 1440
        val tahajjudStartMin = (maghribMin + (nightDuration * 2) / 3) % 1440
        val tahajjud24 = minutesTo24(tahajjudStartMin)
        
        // Isha ends at midnight / Tahajjud start (or approx 23:45)
        val ishaEnd24 = minutesTo24((parseToMinutes(isha24, 19, 35) + 240) % 1440)

        return listOf(
            // 1. তাহাজ্জুদ (Tahajjud)
            PrayerScheduleItem(
                key = "Tahajjud",
                nameEn = "Tahajjud",
                nameBn = "তাহাজ্জুদ",
                descEn = "Voluntary night prayer in the last third of night",
                descBn = "শেষ রাতের শ্রেষ্ঠ নফল ও আত্মশুদ্ধির নামাজ",
                startTime12 = to12HourFormat(tahajjud24, isBn),
                endTime12 = to12HourFormat(fajr24, isBn),
                startTime24 = tahajjud24,
                endTime24 = fajr24,
                isCheckable = true,
                checkIndex = 5,
                iconEmoji = "🌙"
            ),
            // 2. ফজর (Fajr)
            PrayerScheduleItem(
                key = "Fajr",
                nameEn = "Fajr",
                nameBn = "ফজর",
                descEn = "Dawn prayer before sunrise",
                descBn = "ভোরের আলো ফোটার আগে আদায়যোগ্য ফরজ নামাজ",
                startTime12 = to12HourFormat(fajr24, isBn),
                endTime12 = to12HourFormat(sunrise24, isBn),
                startTime24 = fajr24,
                endTime24 = sunrise24,
                isCheckable = true,
                checkIndex = 0,
                iconEmoji = "🌅"
            ),
            // 3. সূর্যোদয় (Sunrise)
            PrayerScheduleItem(
                key = "Sunrise",
                nameEn = "Sunrise",
                nameBn = "সূর্যোদয়",
                descEn = "Sun rises — prohibited prayer time until Ishraq",
                descBn = "সূর্য উদিত হওয়ার সময় (নামাজ নিষিদ্ধ, ১৫ মি. পর ইশরাক)",
                startTime12 = to12HourFormat(sunrise24, isBn),
                endTime12 = to12HourFormat(minutesTo24(parseToMinutes(sunrise24, 5, 45) + 20), isBn),
                startTime24 = sunrise24,
                endTime24 = minutesTo24(parseToMinutes(sunrise24, 5, 45) + 20),
                isCheckable = false,
                checkIndex = -1,
                iconEmoji = "☀️"
            ),
            // 4. যোহর (Dhuhr)
            PrayerScheduleItem(
                key = "Dhuhr",
                nameEn = "Dhuhr",
                nameBn = "যোহর",
                descEn = "Noon prayer after the sun passes meridian",
                descBn = "দুপুরের সূর্য পশ্চিমাকাশে ঢলে পড়ার পর",
                startTime12 = to12HourFormat(dhuhr24, isBn),
                endTime12 = to12HourFormat(asr24, isBn),
                startTime24 = dhuhr24,
                endTime24 = asr24,
                isCheckable = true,
                checkIndex = 1,
                iconEmoji = "☀️"
            ),
            // 5. আসর (Asr)
            PrayerScheduleItem(
                key = "Asr",
                nameEn = "Asr",
                nameBn = "আসর",
                descEn = "Late afternoon prayer before sunset",
                descBn = "বিকেলের ছায়া দ্বিগুণ হওয়ার পর থেকে সূর্যাস্ত পর্যন্ত",
                startTime12 = to12HourFormat(asr24, isBn),
                endTime12 = to12HourFormat(sunset24, isBn),
                startTime24 = asr24,
                endTime24 = sunset24,
                isCheckable = true,
                checkIndex = 2,
                iconEmoji = "🌤️"
            ),
            // 6. সূর্যাস্ত (Sunset)
            PrayerScheduleItem(
                key = "Sunset",
                nameEn = "Sunset",
                nameBn = "সূর্যাস্ত",
                descEn = "Sun dips below horizon — Iftar moment",
                descBn = "সূর্য অস্ত যাওয়ার মুহূর্ত ও ইফতারের সময়",
                startTime12 = to12HourFormat(sunset24, isBn),
                endTime12 = to12HourFormat(maghrib24, isBn),
                startTime24 = sunset24,
                endTime24 = maghrib24,
                isCheckable = false,
                checkIndex = -1,
                iconEmoji = "🌇"
            ),
            // 7. মাগরিব (Maghrib)
            PrayerScheduleItem(
                key = "Maghrib",
                nameEn = "Maghrib",
                nameBn = "মাগরিব",
                descEn = "Sunset prayer immediately after sunset",
                descBn = "সূর্যাস্তের ঠিক পরপরই আদায়ের তাকিদপূর্ণ নামাজ",
                startTime12 = to12HourFormat(maghrib24, isBn),
                endTime12 = to12HourFormat(isha24, isBn),
                startTime24 = maghrib24,
                endTime24 = isha24,
                isCheckable = true,
                checkIndex = 3,
                iconEmoji = "🌆"
            ),
            // 8. ইশা ও বিতর (Isha)
            PrayerScheduleItem(
                key = "Isha",
                nameEn = "Isha & Witr",
                nameBn = "ইশা ও বিতর",
                descEn = "Night prayer for tranquility & spiritual purification",
                descBn = "রাতের আঁধারে প্রশান্তিময় ফরজ ও বিতর নামাজ",
                startTime12 = to12HourFormat(isha24, isBn),
                endTime12 = to12HourFormat(tahajjud24, isBn),
                startTime24 = isha24,
                endTime24 = tahajjud24,
                isCheckable = true,
                checkIndex = 4,
                iconEmoji = "🌌"
            )
        )
    }
}
