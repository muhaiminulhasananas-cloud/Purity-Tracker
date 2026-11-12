package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class DistrictInfo(
    val nameEn: String,
    val nameBn: String,
    val lat: Double,
    val lng: Double
)

val BANGLADESH_DISTRICTS = listOf(
    DistrictInfo("Kishoreganj", "কিশোরগঞ্জ", 24.4449, 90.7766),
    DistrictInfo("Dhaka", "ঢাকা", 23.8103, 90.4125),
    DistrictInfo("Chittagong", "চট্টগ্রাম", 22.3569, 91.7832),
    DistrictInfo("Sylhet", "সিলেট", 24.8949, 91.8687),
    DistrictInfo("Rajshahi", "রাজশাহী", 24.3745, 88.6042),
    DistrictInfo("Khulna", "খুলনা", 22.8456, 89.5403),
    DistrictInfo("Barisal", "বরিশাল", 22.7010, 90.3535),
    DistrictInfo("Rangpur", "রংপুর", 25.7439, 89.2752),
    DistrictInfo("Mymensingh", "ময়মনসিংহ", 24.7471, 90.4203),
    DistrictInfo("Comilla", "কুমিল্লা", 23.4682, 91.1788),
    DistrictInfo("Gazipur", "গাজীপুর", 24.0023, 90.4264),
    DistrictInfo("Narayanganj", "নারায়ণগঞ্জ", 23.6337, 90.4965),
    DistrictInfo("Bogra", "বগুড়া", 24.8465, 89.3777),
    DistrictInfo("Dinajpur", "দিনাজপুর", 25.6217, 88.6355),
    DistrictInfo("Faridpur", "ফরিদপুর", 23.6071, 89.8429),
    DistrictInfo("Feni", "ফেনী", 23.0186, 91.3966),
    DistrictInfo("Jessore", "যশোর", 23.1664, 89.2081),
    DistrictInfo("Cox's Bazar", "কক্সবাজার", 21.4272, 92.0058),
    DistrictInfo("Brahmanbaria", "ব্রাহ্মণবাড়িয়া", 23.9571, 91.1119),
    DistrictInfo("Narsingdi", "নরসিংদী", 23.9322, 90.7154),
    DistrictInfo("Tangail", "টাঙ্গাইল", 24.2513, 89.9167),
    DistrictInfo("Sirajganj", "সিরাজগঞ্জ", 24.4534, 89.7008),
    DistrictInfo("Pabna", "পাবনা", 24.0064, 89.2372),
    DistrictInfo("Kushtia", "কুষ্টিয়া", 23.9013, 89.1205),
    DistrictInfo("Noakhali", "নোয়াখালী", 22.8696, 91.0998),
    DistrictInfo("Jamalpur", "জামালপুর", 24.9375, 89.9378),
    DistrictInfo("Habiganj", "হবিগঞ্জ", 24.3749, 91.4155),
    DistrictInfo("Moulvibazar", "মৌলভীবাজার", 24.4829, 91.7774),
    DistrictInfo("Sunamganj", "সুনামগঞ্জ", 25.0658, 91.3950),
    DistrictInfo("Patuakhali", "পটুয়াখালী", 22.3596, 90.3299),
    DistrictInfo("Bhola", "ভোলা", 22.6859, 90.6481),
    DistrictInfo("Satkhira", "সাতক্ষীরা", 22.7185, 89.0705),
    DistrictInfo("Naogaon", "নওগাঁ", 24.7936, 88.9318),
    DistrictInfo("Natore", "নাটোর", 24.4206, 88.9324),
    DistrictInfo("Chandpur", "চাঁদপুর", 23.2321, 90.6631),
    DistrictInfo("Lakshmipur", "লক্ষ্মীপুর", 22.9425, 90.8412),
    DistrictInfo("Manikganj", "মানিকগঞ্জ", 23.8617, 90.0003),
    DistrictInfo("Munshiganj", "মুন্সীগঞ্জ", 23.5422, 90.5305),
    DistrictInfo("Netrokona", "নেত্রকোণা", 24.8709, 90.7279),
    DistrictInfo("Sherpur", "শেরপুর", 25.0205, 90.0153),
    DistrictInfo("Gopalganj", "গোপালগঞ্জ", 23.0051, 89.8266),
    DistrictInfo("Madaripur", "মাদারীপুর", 23.1641, 90.1897),
    DistrictInfo("Shariatpur", "শরীয়তপুর", 23.2423, 90.4348),
    DistrictInfo("Rajbari", "রাজবাড়ী", 23.7574, 89.6445),
    DistrictInfo("Magura", "মাগুরা", 23.4873, 89.4198),
    DistrictInfo("Jhenaidah", "ঝিনাইদহ", 23.5448, 89.1539),
    DistrictInfo("Narail", "নড়াইল", 23.1725, 89.5127),
    DistrictInfo("Bagerhat", "বাগেরহাট", 22.6516, 89.7859),
    DistrictInfo("Chuadanga", "চুয়াডাঙ্গা", 23.6402, 88.8418),
    DistrictInfo("Meherpur", "মেহেরপুর", 23.7622, 88.6318),
    DistrictInfo("Chapainawabganj", "চাঁপাইনবাবগঞ্জ", 24.5965, 88.2775),
    DistrictInfo("Joypurhat", "জয়পুরহাট", 25.1015, 89.0270),
    DistrictInfo("Gaibandha", "গাইবান্ধা", 25.3288, 89.5404),
    DistrictInfo("Kurigram", "কুড়িগ্রাম", 25.8054, 89.6362),
    DistrictInfo("Lalmonirhat", "লালমনিরহাট", 25.9923, 89.2847),
    DistrictInfo("Nilphamari", "নীলফামারী", 25.9318, 88.8560),
    DistrictInfo("Panchagarh", "পঞ্চগড়", 26.3411, 88.5542),
    DistrictInfo("Thakurgaon", "ঠাকুরগাঁও", 26.0337, 88.4617),
    DistrictInfo("Pirojpur", "পিরোজপুর", 22.5841, 89.9720),
    DistrictInfo("Jhalokati", "ঝালকাঠি", 22.6406, 90.1987),
    DistrictInfo("Barguna", "বরগুনা", 22.0953, 90.1121)
)

fun toBanglaDigits(numberStr: String): String {
    val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val sb = StringBuilder()
    for (ch in numberStr) {
        if (ch in '0'..'9') {
            sb.append(banglaDigits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

enum class PrayerWaqt(
    val nameEn: String,
    val nameBn: String,
    val defaultStartHour: Int,
    val defaultStartMin: Int,
    val defaultEndHour: Int,
    val defaultEndMin: Int
) {
    FAJR("Fajr", "ফজর", 4, 30, 5, 45),
    SUNRISE("Sunrise", "সূর্যোদয়", 5, 45, 6, 0),
    DHUHR("Dhuhr", "যোহর", 12, 6, 16, 37),
    ASR("Asr", "আসর", 16, 37, 18, 25),
    MAGHRIB("Maghrib", "মাগরিব", 18, 25, 19, 35),
    ISHA("Isha", "ইশা", 19, 35, 23, 30)
}

@Composable
fun PrayerSceneryCard(
    timings: Map<String, String>,
    onOpenAllPrayers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBn = LanguageStore.currentLanguage.value == "bn"

    var showRakatDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showMosqueOffsetDialog by remember { mutableStateOf(false) }
    var isLocatingGps by remember { mutableStateOf(false) }

    // Read stored location or default to Kishoreganj
    val prefs = remember { context.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE) }
    var locationName by remember {
        mutableStateOf(prefs.getString("location_name", "Kishoreganj (কিশোরগঞ্জ)") ?: "Kishoreganj (কিশোরগঞ্জ)")
    }

    // Function to acquire GPS Location actively
    val detectLocation = {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            isLocatingGps = true
            var found = false

            fun applyLocation(loc: Location) {
                if (found) return
                found = true
                isLocatingGps = false

                // Find closest district
                val closest = BANGLADESH_DISTRICTS.minByOrNull { d ->
                    val dLat = d.lat - loc.latitude
                    val dLng = d.lng - loc.longitude
                    dLat * dLat + dLng * dLng
                } ?: BANGLADESH_DISTRICTS.first()

                val newLocName = "${closest.nameEn} (${closest.nameBn})"
                locationName = newLocName
                prefs.edit()
                    .putString("location_name", newLocName)
                    .putString("lat", loc.latitude.toString())
                    .putString("lng", loc.longitude.toString())
                    .apply()

                PrayerScheduler.scheduleDailyPrayers(context)
                Toast.makeText(
                    context,
                    if (isBn) "📍 বর্তমান অবস্থান সনাক্ত হয়েছে: $newLocName" else "📍 Location detected: $newLocName",
                    Toast.LENGTH_SHORT
                ).show()
                showLocationDialog = false
            }

            if (lm != null) {
                try {
                    val lastLoc: Location? = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

                    if (lastLoc != null) {
                        applyLocation(lastLoc)
                    } else {
                        // Request active single update with listener
                        val listener = object : LocationListener {
                            override fun onLocationChanged(location: Location) {
                                applyLocation(location)
                                lm.removeUpdates(this)
                            }
                            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                            override fun onProviderEnabled(provider: String) {}
                            override fun onProviderDisabled(provider: String) {}
                        }

                        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, listener, Looper.getMainLooper())
                        } else if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, listener, Looper.getMainLooper())
                        }

                        // Timeout fallback after 4 seconds
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (!found) {
                                isLocatingGps = false
                                val defaultDist = BANGLADESH_DISTRICTS.first()
                                val defName = "${defaultDist.nameEn} (${defaultDist.nameBn})"
                                locationName = defName
                                prefs.edit().putString("location_name", defName).putString("lat", defaultDist.lat.toString()).putString("lng", defaultDist.lng.toString()).apply()
                                PrayerScheduler.scheduleDailyPrayers(context)
                                Toast.makeText(context, if (isBn) "📍 ডিফল্ট লোকেশন সেট করা হয়েছে: $defName" else "📍 Default location set: $defName", Toast.LENGTH_SHORT).show()
                                showLocationDialog = false
                            }
                        }, 4000)
                    }
                } catch (e: SecurityException) {
                    isLocatingGps = false
                    Toast.makeText(context, if (isBn) "লোকেশন অ্যাক্সেস করা যায়নি" else "Location access denied", Toast.LENGTH_SHORT).show()
                }
            } else {
                isLocatingGps = false
            }
        }
    }

    // Permission launcher for GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            detectLocation()
        } else {
            Toast.makeText(
                context,
                if (isBn) "লোকেশন পারমিশন প্রয়োজন" else "Location permission is required for Auto GPS",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Live clock trigger (updates every 1 second)
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    // Parse prayer times or fallback to standard timings
    val calendar = remember(currentTimeMillis) { Calendar.getInstance().apply { timeInMillis = currentTimeMillis } }
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)
    val currentSecond = calendar.get(Calendar.SECOND)
    val currentTotalMinutes = currentHour * 60 + currentMinute

    // Determine current Waqt & Next Waqt
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.US) }
    val sdf12 = remember { SimpleDateFormat("hh:mm a", Locale.US) }

    fun parseMinutes(timeStr: String?, defaultHour: Int, defaultMin: Int): Int {
        if (timeStr == null) return defaultHour * 60 + defaultMin
        return try {
            val date = sdf.parse(timeStr) ?: return defaultHour * 60 + defaultMin
            val cal = Calendar.getInstance().apply { time = date }
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        } catch (e: Exception) {
            defaultHour * 60 + defaultMin
        }
    }

    val fajrStart = parseMinutes(timings["Fajr"], 4, 30)
    val sunriseTime = parseMinutes(timings["Sunrise"], 5, 45)
    val dhuhrStart = parseMinutes(timings["Dhuhr"], 12, 6)
    val asrStart = parseMinutes(timings["Asr"], 16, 37)
    val maghribStart = parseMinutes(timings["Maghrib"], 18, 25)
    val ishaStart = parseMinutes(timings["Isha"], 19, 35)

    // Current waqt evaluation
    val currentWaqt: PrayerWaqt
    val currentStartMinutes: Int
    val currentEndMinutes: Int
    val nextWaqtNameBn: String
    val nextWaqtNameEn: String
    val nextWaqtTargetMinute: Int
    val isNextTomorrow: Boolean

    if (currentTotalMinutes in fajrStart until sunriseTime) {
        currentWaqt = PrayerWaqt.FAJR
        currentStartMinutes = fajrStart
        currentEndMinutes = sunriseTime
        nextWaqtNameBn = "যোহর"
        nextWaqtNameEn = "Dhuhr"
        nextWaqtTargetMinute = dhuhrStart
        isNextTomorrow = false
    } else if (currentTotalMinutes in sunriseTime until dhuhrStart) {
        currentWaqt = PrayerWaqt.SUNRISE
        currentStartMinutes = sunriseTime
        currentEndMinutes = dhuhrStart
        nextWaqtNameBn = "যোহর"
        nextWaqtNameEn = "Dhuhr"
        nextWaqtTargetMinute = dhuhrStart
        isNextTomorrow = false
    } else if (currentTotalMinutes in dhuhrStart until asrStart) {
        currentWaqt = PrayerWaqt.DHUHR
        currentStartMinutes = dhuhrStart
        currentEndMinutes = asrStart
        nextWaqtNameBn = "আসর"
        nextWaqtNameEn = "Asr"
        nextWaqtTargetMinute = asrStart
        isNextTomorrow = false
    } else if (currentTotalMinutes in asrStart until maghribStart) {
        currentWaqt = PrayerWaqt.ASR
        currentStartMinutes = asrStart
        currentEndMinutes = maghribStart
        nextWaqtNameBn = "মাগরিব"
        nextWaqtNameEn = "Maghrib"
        nextWaqtTargetMinute = maghribStart
        isNextTomorrow = false
    } else if (currentTotalMinutes in maghribStart until ishaStart) {
        currentWaqt = PrayerWaqt.MAGHRIB
        currentStartMinutes = maghribStart
        currentEndMinutes = ishaStart
        nextWaqtNameBn = "ইশা"
        nextWaqtNameEn = "Isha"
        nextWaqtTargetMinute = ishaStart
        isNextTomorrow = false
    } else {
        currentWaqt = PrayerWaqt.ISHA
        currentStartMinutes = ishaStart
        currentEndMinutes = fajrStart
        nextWaqtNameBn = "ফজর"
        nextWaqtNameEn = "Fajr"
        nextWaqtTargetMinute = fajrStart
        isNextTomorrow = currentTotalMinutes >= ishaStart
    }

    // Format start and end times in 12hr format e.g. "১২:০৬ PM - ০৪:৩৭ PM"
    fun formatMinutesTo12Hr(totalMins: Int): String {
        val h = totalMins / 60
        val m = totalMins % 60
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
        }
        val formatted = sdf12.format(cal.time)
        return if (isBn) toBanglaDigits(formatted) else formatted
    }

    val timeRangeStr = "${formatMinutesTo12Hr(currentStartMinutes)} - ${formatMinutesTo12Hr(currentEndMinutes)}"

    // Calculate countdown to next waqt
    val targetSecondsToday = nextWaqtTargetMinute * 60
    val currentSecondsToday = currentTotalMinutes * 60 + currentSecond
    val diffSeconds = if (isNextTomorrow || targetSecondsToday < currentSecondsToday) {
        (24 * 3600 - currentSecondsToday) + targetSecondsToday
    } else {
        targetSecondsToday - currentSecondsToday
    }

    val countdownHours = (diffSeconds / 3600).coerceAtLeast(0)
    val countdownMinutes = ((diffSeconds % 3600) / 60).coerceAtLeast(0)
    val countdownSecs = (diffSeconds % 60).coerceAtLeast(0)

    val rawCountdownStr = String.format(Locale.US, "%02d:%02d:%02d", countdownHours, countdownMinutes, countdownSecs)
    val countdownDisplay = if (isBn) toBanglaDigits(rawCountdownStr) else rawCountdownStr

    val currentWaqtTitle = if (isBn) currentWaqt.nameBn else currentWaqt.nameEn
    val nextWaqtTitle = if (isBn) nextWaqtNameBn else nextWaqtNameEn

    // Render the Card matching the user's design
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(390.dp)
        ) {
            // 1. Dynamic Scenic Canvas Vector Art
            IslamicSceneryCanvas(
                currentWaqt = currentWaqt,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Translucent subtle vignette overlay for crisp readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.30f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.65f)
                            )
                        )
                    )
            )

            // 3. UI Elements on top
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left: "এখন সময়" and Large Prayer Name + Time Range
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = if (isBn) "এখন সময়" else "CURRENT PRAYER",
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentWaqtTitle,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = timeRangeStr,
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Right: "মসজিদ সময়" & "রাকাত" Pill Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Mosque Offset Settings Button
                        Surface(
                            onClick = { showMosqueOffsetDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = "Mosque Offset",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBn) "মসজিদ" else "Masjid",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Rakat Pill Button
                        Surface(
                            onClick = { showRakatDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isBn) "রাকাত" else "Rakat",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Middle / Lower Center: Next Waqt and Live Large Countdown Clock
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isBn) "পরবর্তী ওয়াক্ত: $nextWaqtTitle" else "Next Prayer: $nextWaqtTitle",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Large Digital Countdown Clock
                    Text(
                        text = countdownDisplay,
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Bottom Footer Row: "সকল নামাজ >" and "[Location] >"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: "সকল নামাজ >"
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenAllPrayers() }
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "সকল নামাজ" else "All Prayers",
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Right: Location Name e.g. "Kishoreganj (কি... >"
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showLocationDialog = true }
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = locationName,
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 160.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }

    // Rakat Dialog
    if (showRakatDialog) {
        RakatInfoDialog(onDismiss = { showRakatDialog = false })
    }

    // Location / District Chooser Dialog
    if (showLocationDialog) {
        LocationChooserDialog(
            currentLocationName = locationName,
            isLocating = isLocatingGps,
            onDismiss = { showLocationDialog = false },
            onSelectDistrict = { dist ->
                locationName = "${dist.nameEn} (${dist.nameBn})"
                prefs.edit()
                    .putString("location_name", locationName)
                    .putString("lat", dist.lat.toString())
                    .putString("lng", dist.lng.toString())
                    .apply()
                PrayerScheduler.scheduleDailyPrayers(context)
                showLocationDialog = false
            },
            onAutoDetectGps = {
                val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                if (!hasFine && !hasCoarse) {
                    locationPermissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                } else {
                    detectLocation()
                }
            }
        )
    }

    // Mosque / Jamat Offset Dialog
    if (showMosqueOffsetDialog) {
        MosqueOffsetDialog(
            onDismiss = { showMosqueOffsetDialog = false },
            onSaved = {
                Toast.makeText(
                    context,
                    if (isBn) "মসজিদের সময়সূচী সংরক্ষিত ও আপডেট করা হয়েছে" else "Masjid time settings saved & updated",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

/**
 * Atmospheric Islamic Scenery Canvas Art matching the screenshot:
 */
@Composable
fun IslamicSceneryCanvas(
    currentWaqt: PrayerWaqt,
    modifier: Modifier = Modifier
) {
    // Sky color scheme based on time of day
    val skyColors = when (currentWaqt) {
        PrayerWaqt.FAJR -> listOf(Color(0xFF0F2042), Color(0xFF1E3A5F), Color(0xFF3B6978), Color(0xFF7098A3))
        PrayerWaqt.SUNRISE -> listOf(Color(0xFF1F3C64), Color(0xFF3B6978), Color(0xFFE08D58), Color(0xFFF7C59F))
        PrayerWaqt.DHUHR -> listOf(Color(0xFF0D5E7B), Color(0xFF197A9B), Color(0xFF289EB9), Color(0xFF4DBFD6))
        PrayerWaqt.ASR -> listOf(Color(0xFF13506B), Color(0xFF247B8F), Color(0xFF6DA3A0), Color(0xFFDF9F67))
        PrayerWaqt.MAGHRIB -> listOf(Color(0xFF21153B), Color(0xFF4C2A58), Color(0xFF88436B), Color(0xFFC76961))
        PrayerWaqt.ISHA -> listOf(Color(0xFF060E1A), Color(0xFF0B192C), Color(0xFF132A45), Color(0xFF1E3E62))
    }

    // Celestial body (Sun/Moon) details
    val isNight = currentWaqt == PrayerWaqt.ISHA || currentWaqt == PrayerWaqt.FAJR
    val celestialColor = if (isNight) Color(0xFFFFFBEA) else Color(0xFFFFD54F)
    val glowColor = if (isNight) Color(0xFFFFFBEA).copy(alpha = 0.25f) else Color(0xFFFF9800).copy(alpha = 0.35f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 1. Sky Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = skyColors,
                startY = 0f,
                endY = height * 0.70f
            ),
            size = Size(width, height)
        )

        // 2. Glowing Celestial Body (Sun or Crescent/Full Moon)
        val celestialCenterX = width * 0.78f
        val celestialCenterY = height * 0.22f
        val celestialRadius = width * 0.08f

        // Outer Aura Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor, Color.Transparent),
                center = Offset(celestialCenterX, celestialCenterY),
                radius = celestialRadius * 2.6f
            ),
            center = Offset(celestialCenterX, celestialCenterY),
            radius = celestialRadius * 2.6f
        )

        // Inner Bright Sphere
        drawCircle(
            color = celestialColor,
            center = Offset(celestialCenterX, celestialCenterY),
            radius = celestialRadius
        )

        // If Night, draw Crescent mask for a classic Islamic Crescent Moon
        if (isNight) {
            drawCircle(
                color = skyColors[1],
                center = Offset(celestialCenterX - celestialRadius * 0.35f, celestialCenterY - celestialRadius * 0.15f),
                radius = celestialRadius * 0.88f
            )
        }

        // 3. Birds in Sky (Flying flock)
        drawFlockOfBirds(this, width, height)

        // 4. Distant Mountain Ridge (Layer 1 - Deep soft mist)
        val mountain1Path = Path().apply {
            moveTo(0f, height * 0.48f)
            cubicTo(width * 0.15f, height * 0.42f, width * 0.30f, height * 0.45f, width * 0.45f, height * 0.40f)
            cubicTo(width * 0.60f, height * 0.36f, width * 0.80f, height * 0.44f, width, height * 0.38f)
            lineTo(width, height * 0.65f)
            lineTo(0f, height * 0.65f)
            close()
        }
        drawPath(
            mountain1Path,
            brush = Brush.verticalGradient(
                colors = listOf(skyColors[1].copy(alpha = 0.55f), skyColors[2].copy(alpha = 0.75f)),
                startY = height * 0.36f,
                endY = height * 0.65f
            )
        )

        // 5. Midground Mountain Ridge (Layer 2 - Crisp Teal/Navy Silhouette)
        val mountain2Path = Path().apply {
            moveTo(0f, height * 0.53f)
            cubicTo(width * 0.20f, height * 0.47f, width * 0.35f, height * 0.52f, width * 0.55f, height * 0.46f)
            cubicTo(width * 0.75f, height * 0.42f, width * 0.90f, height * 0.50f, width, height * 0.45f)
            lineTo(width, height * 0.70f)
            lineTo(0f, height * 0.70f)
            close()
        }
        drawPath(
            mountain2Path,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0D253A).copy(alpha = 0.85f), Color(0xFF081826)),
                startY = height * 0.42f,
                endY = height * 0.70f
            )
        )

        // 6. Serene Lake / Water Body with Horizon Line
        val waterTopY = height * 0.60f
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A1F33).copy(alpha = 0.90f),
                    Color(0xFF05111D),
                    Color(0xFF020912)
                ),
                startY = waterTopY,
                endY = height
            ),
            topLeft = Offset(0f, waterTopY),
            size = Size(width, height - waterTopY)
        )

        // 7. Water Moon/Sun Shimmer Reflection
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                center = Offset(celestialCenterX, waterTopY + height * 0.08f),
                radius = width * 0.35f
            ),
            topLeft = Offset(celestialCenterX - width * 0.35f, waterTopY),
            size = Size(width * 0.70f, height * 0.25f)
        )

        // 8. Foreground Grand Mosque Architecture Silhouettes (Left & Right)
        val mosqueBaseColor = Color(0xFF05101A)
        val mosqueHighlightColor = Color(0xFF0C2438)

        // Mosque Complex
        drawMosqueComplex(this, width, height, mosqueBaseColor, mosqueHighlightColor)
    }
}

private fun drawFlockOfBirds(drawScope: DrawScope, w: Float, h: Float) {
    val birdColor = Color.White.copy(alpha = 0.55f)
    drawBird(drawScope, w * 0.25f, h * 0.18f, w * 0.025f, birdColor)
    drawBird(drawScope, w * 0.32f, h * 0.14f, w * 0.032f, birdColor)
    drawBird(drawScope, w * 0.40f, h * 0.17f, w * 0.022f, birdColor)
    drawBird(drawScope, w * 0.48f, h * 0.13f, w * 0.028f, birdColor)
}

private fun drawBird(drawScope: DrawScope, x: Float, y: Float, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(x - size, y + size * 0.3f)
        quadraticTo(x - size * 0.5f, y - size * 0.5f, x, y)
        quadraticTo(x + size * 0.5f, y - size * 0.5f, x + size, y + size * 0.3f)
        quadraticTo(x + size * 0.5f, y - size * 0.2f, x, y + size * 0.1f)
        quadraticTo(x - size * 0.5f, y - size * 0.2f, x - size, y + size * 0.3f)
        close()
    }
    drawScope.drawPath(path, color = color)
}

private fun drawMosqueComplex(
    drawScope: DrawScope,
    w: Float,
    h: Float,
    baseColor: Color,
    highlightColor: Color
) {
    // Grand Center-Left Mosque Building
    val bLeft = w * 0.04f
    val bWidth = w * 0.62f
    val bTop = h * 0.52f
    val bHeight = h * 0.48f

    // Main Mosque Foundation
    drawScope.drawRect(
        color = baseColor,
        topLeft = Offset(bLeft, bTop),
        size = Size(bWidth, bHeight)
    )

    // Central Grand Dome
    val domeCenterX = bLeft + bWidth * 0.48f
    val domeRadius = bWidth * 0.24f
    val domeBaseY = bTop

    val domePath = Path().apply {
        moveTo(domeCenterX - domeRadius, domeBaseY)
        cubicTo(
            domeCenterX - domeRadius, domeBaseY - domeRadius * 1.35f,
            domeCenterX, domeBaseY - domeRadius * 1.65f,
            domeCenterX, domeBaseY - domeRadius * 1.65f
        )
        cubicTo(
            domeCenterX, domeBaseY - domeRadius * 1.65f,
            domeCenterX + domeRadius, domeBaseY - domeRadius * 1.35f,
            domeCenterX + domeRadius, domeBaseY
        )
        close()
    }
    drawScope.drawPath(domePath, color = baseColor)

    // Golden Crescent on Top of Central Dome
    drawFinial(drawScope, Offset(domeCenterX, domeBaseY - domeRadius * 1.65f), bWidth * 0.04f)

    // Left Small Dome
    val sDome1X = bLeft + bWidth * 0.18f
    val sDome1R = bWidth * 0.12f
    val sDome1Path = Path().apply {
        moveTo(sDome1X - sDome1R, domeBaseY)
        cubicTo(
            sDome1X - sDome1R, domeBaseY - sDome1R * 1.25f,
            sDome1X, domeBaseY - sDome1R * 1.55f,
            sDome1X, domeBaseY - sDome1R * 1.55f
        )
        cubicTo(
            sDome1X, domeBaseY - sDome1R * 1.55f,
            sDome1X + sDome1R, domeBaseY - sDome1R * 1.25f,
            sDome1X + sDome1R, domeBaseY
        )
        close()
    }
    drawScope.drawPath(sDome1Path, color = baseColor)
    drawFinial(drawScope, Offset(sDome1X, domeBaseY - sDome1R * 1.55f), bWidth * 0.025f)

    // Right Small Dome
    val sDome2X = bLeft + bWidth * 0.78f
    val sDome2R = bWidth * 0.12f
    val sDome2Path = Path().apply {
        moveTo(sDome2X - sDome2R, domeBaseY)
        cubicTo(
            sDome2X - sDome2R, domeBaseY - sDome2R * 1.25f,
            sDome2X, domeBaseY - sDome2R * 1.55f,
            sDome2X, domeBaseY - sDome2R * 1.55f
        )
        cubicTo(
            sDome2X, domeBaseY - sDome2R * 1.55f,
            sDome2X + sDome2R, domeBaseY - sDome2R * 1.25f,
            sDome2X + sDome2R, domeBaseY
        )
        close()
    }
    drawScope.drawPath(sDome2Path, color = baseColor)
    drawFinial(drawScope, Offset(sDome2X, domeBaseY - sDome2R * 1.55f), bWidth * 0.025f)

    // Tall Slender Minaret on Left
    drawMinaret(drawScope, bLeft - w * 0.02f, h * 0.30f, w * 0.065f, h * 0.70f, baseColor, highlightColor)

    // Tall Slender Minaret on Right
    drawMinaret(drawScope, bLeft + bWidth + w * 0.04f, h * 0.26f, w * 0.075f, h * 0.74f, baseColor, highlightColor)

    // Arched windows pattern along Mosque Wall
    val winCount = 5
    val winSpacing = bWidth / (winCount + 1)
    for (i in 0 until winCount) {
        val wx = bLeft + w * 0.05f + i * winSpacing
        val wy = bTop + h * 0.12f
        val ww = w * 0.055f
        val wh = h * 0.10f
        drawArchedWindow(drawScope, wx, wy, ww, wh, baseColor.copy(alpha = 0.5f))
    }
}

private fun drawMinaret(
    drawScope: DrawScope,
    x: Float,
    topY: Float,
    w: Float,
    h: Float,
    baseColor: Color,
    highlightColor: Color
) {
    // Minaret Shaft
    drawScope.drawRect(
        color = baseColor,
        topLeft = Offset(x, topY + w * 1.2f),
        size = Size(w, h - w * 1.2f)
    )

    // Balcony 1
    drawScope.drawRect(
        color = highlightColor,
        topLeft = Offset(x - w * 0.2f, topY + w * 2.2f),
        size = Size(w * 1.4f, w * 0.35f)
    )

    // Minaret Dome Top
    val domeCenterX = x + w * 0.5f
    val domeBaseY = topY + w * 1.2f
    val domePath = Path().apply {
        moveTo(x, domeBaseY)
        cubicTo(x, topY + w * 0.3f, domeCenterX, topY, domeCenterX, topY)
        cubicTo(domeCenterX, topY, x + w, topY + w * 0.3f, x + w, domeBaseY)
        close()
    }
    drawScope.drawPath(domePath, color = highlightColor)

    // Finial & Crescent
    drawFinial(drawScope, Offset(domeCenterX, topY), w * 0.35f)
}

private fun drawFinial(drawScope: DrawScope, tip: Offset, size: Float) {
    drawScope.drawLine(
        color = Color(0xFFFFD700),
        start = tip,
        end = Offset(tip.x, tip.y - size * 0.8f),
        strokeWidth = 2.5f
    )
    drawScope.drawCircle(
        color = Color(0xFFFFD700),
        radius = size * 0.25f,
        center = Offset(tip.x, tip.y - size * 0.8f)
    )
}

private fun drawArchedWindow(drawScope: DrawScope, x: Float, y: Float, w: Float, h: Float, color: Color) {
    val path = Path().apply {
        moveTo(x, y + h)
        lineTo(x, y + w * 0.5f)
        cubicTo(x, y, x + w * 0.5f, y, x + w * 0.5f, y)
        cubicTo(x + w * 0.5f, y, x + w, y, x + w, y + w * 0.5f)
        lineTo(x + w, y + h)
        close()
    }
    drawScope.drawPath(path, color = color)
}

/**
 * Detailed Rakat Dialog explaining the exact rakat structure of all 5 daily prayers + Tahajjud
 */
@Composable
fun RakatInfoDialog(onDismiss: () -> Unit) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isLightMode) Color(0xFFFFFFFF) else Color(0xFF101B2B)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF0284C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.MenuBook,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isBn) "৫ ওয়াক্ত ও তাহাজ্জুদের রাকাত তালিকা" else "Daily & Tahajjud Rakat Breakdown",
                            color = titleColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = descColor)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val rakatList = if (isBn) listOf(
                    Triple("ফজর", "৪ রাকাত", "২ রাকাত সুন্নত (মুয়াক্কাদা) + ২ রাকাত ফরজ"),
                    Triple("যোহর", "১২ রাকাত", "৪ রাকাত সুন্নত + ৪ রাকাত ফরজ + ২ রাকাত সুন্নত + ২ রাকাত নফল"),
                    Triple("আসর", "৮ রাকাত", "৪ রাকাত সুন্নত (গায়রে মুয়াক্কাদা) + ৪ রাকাত ফরজ"),
                    Triple("মাগরিব", "৭ রাকাত", "৩ রাকাত ফরজ + ২ রাকাত সুন্নত + ২ রাকাত নফল"),
                    Triple("ইশা ও বিতর", "১৭ রাকাত", "৪ রাকাত সুন্নত + ৪ রাকাত ফরজ + ২ রাকাত সুন্নত + ২ রাকাত নফল + ৩ রাকাত বিতর + ২ রাকাত নফল"),
                    Triple("তাহাজ্জুদ (নফল)", "২ থেকে ১২ রাকাত", "রাতের শেষ তৃতীয়াংশের অতি ফজিলতপূর্ণ নফল নামাজ")
                ) else listOf(
                    Triple("Fajr", "4 Rakats", "2 Sunnah (Muakkadah) + 2 Fardh"),
                    Triple("Dhuhr", "12 Rakats", "4 Sunnah + 4 Fardh + 2 Sunnah + 2 Nafl"),
                    Triple("Asr", "8 Rakats", "4 Sunnah (Ghair Muakkadah) + 4 Fardh"),
                    Triple("Maghrib", "7 Rakats", "3 Fardh + 2 Sunnah + 2 Nafl"),
                    Triple("Isha & Witr", "17 Rakats", "4 Sunnah + 4 Fardh + 2 Sunnah + 2 Nafl + 3 Witr + 2 Nafl"),
                    Triple("Tahajjud", "2 to 12 Rakats", "Highly rewarding optional night prayer")
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rakatList) { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isLightMode) Color(0xFFF1F5F9) else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.first,
                                        color = if (isLightMode) Color(0xFF0369A1) else Color(0xFF38BDF8),
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = item.second,
                                        color = if (isLightMode) Color(0xFF10B981) else Color(0xFF34D399),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.third,
                                    color = descColor,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isBn) "বুঝেছি (Close)" else "Got it",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * District / Location Chooser Dialog with Search & GPS Auto-Detection
 */
@Composable
fun LocationChooserDialog(
    currentLocationName: String,
    isLocating: Boolean,
    onDismiss: () -> Unit,
    onSelectDistrict: (DistrictInfo) -> Unit,
    onAutoDetectGps: () -> Unit
) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    var searchQuery by remember { mutableStateOf("") }
    val filteredDistricts = remember(searchQuery) {
        if (searchQuery.isBlank()) BANGLADESH_DISTRICTS
        else BANGLADESH_DISTRICTS.filter {
            it.nameEn.contains(searchQuery, ignoreCase = true) || it.nameBn.contains(searchQuery)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isLightMode) Color(0xFFFFFFFF) else Color(0xFF101B2B)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF0284C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isBn) "নামাজের স্থান / জেলা নির্বাচন" else "Select Prayer Location",
                            color = titleColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = descColor)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GPS Auto-detect button with loader
                Button(
                    onClick = onAutoDetectGps,
                    enabled = !isLocating,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "লোকেশন খোঁজা হচ্ছে..." else "Detecting location...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    } else {
                        Icon(Icons.Filled.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "অটো জিপিএস লোকেশন চালু করুন" else "Auto Detect GPS Location",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (isBn) "জেলা খুঁজুন (যেমন: কিশোরগঞ্জ, ঢাকা...)" else "Search district...",
                            color = descColor,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = descColor)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = if (isLightMode) Color(0xFFCBD5E1) else Color.DarkGray
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // District List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredDistricts) { dist ->
                        val isSelected = currentLocationName.contains(dist.nameEn, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) (if (isLightMode) Color(0xFFE0F2FE) else Color(0xFF0284C7).copy(alpha = 0.25f))
                                    else (if (isLightMode) Color(0xFFF8FAFC) else Color.White.copy(alpha = 0.04f)),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectDistrict(dist) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Place,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF0284C7) else descColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${dist.nameEn} (${dist.nameBn})",
                                        color = if (isSelected) (if (isLightMode) Color(0xFF0369A1) else Color(0xFF38BDF8)) else titleColor,
                                        fontSize = 13.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
