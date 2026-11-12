package com.example

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class PrayerOffsetConfig(
    val key: String,
    val nameEn: String,
    val nameBn: String,
    val defaultMinutes: Int,
    val iconEmoji: String
)

val MOSQUE_PRAYERS = listOf(
    PrayerOffsetConfig("Fajr", "Fajr", "ফজর", 20, "🌅"),
    PrayerOffsetConfig("Dhuhr", "Dhuhr", "যোহর", 15, "☀️"),
    PrayerOffsetConfig("Asr", "Asr", "আসর", 15, "🌤️"),
    PrayerOffsetConfig("Maghrib", "Maghrib", "মাগরিব", 0, "🌆"),
    PrayerOffsetConfig("Isha", "Isha", "ইশা", 15, "🌌")
)

/**
 * Dialog allowing users to configure Mosque Adhan / Jamat offsets and Hanafi Asr calculation.
 */
@Composable
fun MosqueOffsetDialog(
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF475569) else Color.LightGray

    var isOffsetEnabled by remember { mutableStateOf(PrayerScheduler.isMasjidOffsetEnabled(context)) }
    var isHanafi by remember { mutableStateOf(PrayerScheduler.isHanafi(context)) }

    // Offset state map (prayerKey -> offset in minutes)
    val offsets = remember {
        mutableStateMapOf<String, Int>().apply {
            MOSQUE_PRAYERS.forEach { p ->
                put(p.key, PrayerScheduler.getMasjidOffset(context, p.key))
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(24.dp),
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
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(Color(0xFF0284C7), Color(0xFF0D9488))
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBn) "মসজিদের সময় অনুযায়ী আজান" else "Masjid Adhan Time Offset",
                                color = titleColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBn) "লোকাল মসজিদের সাথে আজান মেলান" else "Sync Adhan with local mosque",
                                color = descColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = descColor)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle 1: Enable Mosque Offset
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isLightMode) Color(0xFFF1F5F9) else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = if (isBn) "মসজিদ অনুযায়ী আজান সমন্বয়" else "Enable Masjid Time Offset",
                            color = titleColor,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isBn) "ওয়াক্তের নির্দিষ্ট মিনিট পর মসজিদের আজান বা জামাতের সময় আজান বাজবে"
                            else "Plays Adhan with +/- minutes offset matching mosque timing",
                            color = descColor,
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp
                        )
                    }

                    Switch(
                        checked = isOffsetEnabled,
                        onCheckedChange = { isOffsetEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0284C7)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle 2: Hanafi Asr Method
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isLightMode) Color(0xFFF1F5F9) else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = if (isBn) "হানাফী আসরের সময়সূচী (বাংলাদেশ)" else "Hanafi Asr Calculation",
                            color = titleColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isBn) "ছায়া দ্বিগুণ হওয়ার পর আসর শুরু (মসজিদের সাধারণ নিয়ম)"
                            else "2x shadow standard for Indian subcontinent & Bangladesh",
                            color = descColor,
                            fontSize = 10.5.sp
                        )
                    }

                    Switch(
                        checked = isHanafi,
                        onCheckedChange = { isHanafi = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Offset Rows for 5 daily prayers
                Text(
                    text = if (isBn) "ওয়াক্ত শুরুর পর মসজিদের আজানের ব্যবধান (+/- মিনিট):" else "Offset from start time (+/- minutes):",
                    color = titleColor,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MOSQUE_PRAYERS) { prayer ->
                        val curOffset = offsets[prayer.key] ?: prayer.defaultMinutes
                        val prayerLabel = if (isBn) prayer.nameBn else prayer.nameEn

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isLightMode) Color(0xFFF8FAFC) else Color.White.copy(alpha = 0.04f),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = prayer.iconEmoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = prayerLabel,
                                        color = titleColor,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Stepper (- / +)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Decrement Button
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(
                                                if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.12f),
                                                CircleShape
                                            )
                                            .clickable(enabled = isOffsetEnabled) {
                                                if (curOffset > -60) {
                                                    offsets[prayer.key] = curOffset - 5
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "−",
                                            color = if (isOffsetEnabled) titleColor else descColor,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Minutes Display Pill
                                    Box(
                                        modifier = Modifier
                                            .widthIn(min = 60.dp)
                                            .height(28.dp)
                                            .background(
                                                if (isOffsetEnabled) (if (curOffset >= 0) Color(0xFF0284C7).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                                                else Color.Gray.copy(alpha = 0.1f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val displayStr = if (curOffset >= 0) "+$curOffset" else "$curOffset"
                                        val unitStr = if (isBn) "মি." else "m"
                                        val finalVal = if (isBn) toBanglaDigits("$displayStr $unitStr") else "$displayStr $unitStr"
                                        Text(
                                            text = finalVal,
                                            color = if (isOffsetEnabled) (if (curOffset >= 0) Color(0xFF0284C7) else Color(0xFFEF4444)) else descColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Increment Button
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(
                                                if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.12f),
                                                CircleShape
                                            )
                                            .clickable(enabled = isOffsetEnabled) {
                                                if (curOffset < 90) {
                                                    offsets[prayer.key] = curOffset + 5
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+",
                                            color = if (isOffsetEnabled) titleColor else descColor,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons (Reset Default & Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            MOSQUE_PRAYERS.forEach { p ->
                                offsets[p.key] = p.defaultMinutes
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = descColor)
                    ) {
                        Text(
                            text = if (isBn) "রিসেট" else "Reset",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            PrayerScheduler.setMasjidOffsetEnabled(context, isOffsetEnabled)
                            PrayerScheduler.setHanafi(context, isHanafi)
                            offsets.forEach { (key, offset) ->
                                PrayerScheduler.setMasjidOffset(context, key, offset)
                            }
                            PrayerScheduler.scheduleDailyPrayers(context)
                            onSaved()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "সেভ করুন" else "Save Settings",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
