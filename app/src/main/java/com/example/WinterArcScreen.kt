package com.example

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassTheme
import com.example.ui.theme.floatingTextShadow
import com.example.ui.theme.glassCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WinterArcScreen(
    onBack: () -> Unit,
    winterArcViewModel: WinterArcViewModel,
    initialSubTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLightMode = GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"

    var selectedSubTab by remember(initialSubTab) { mutableStateOf(initialSubTab) }

    // Physical Back button returns sequentially: sub-tab -> overview -> home
    BackHandler {
        if (selectedSubTab != 0) {
            selectedSubTab = 0
        } else {
            onBack()
        }
    }

    val challenges by winterArcViewModel.challenges.collectAsState()
    val workouts by winterArcViewModel.workouts.collectAsState()
    val dayTasks by winterArcViewModel.dayTasks.collectAsState()
    val dayRecords by winterArcViewModel.dayRecords.collectAsState()
    val waterIntake by winterArcViewModel.waterIntakeMl.collectAsState()
    val waterLogs by winterArcViewModel.waterLogs.collectAsState()
    val waterTarget by winterArcViewModel.waterTargetMl.collectAsState()
    val waterReminderEnabled by winterArcViewModel.waterReminderEnabled.collectAsState()
    val waterReminderInterval by winterArcViewModel.waterReminderIntervalMins.collectAsState()
    val waterAutoMode by winterArcViewModel.waterAutoModeEnabled.collectAsState()
    val timelineMode by winterArcViewModel.timelineMode.collectAsState()
    val arcNotificationsEnabled by winterArcViewModel.arcNotificationsEnabled.collectAsState()

    var previewUnlocked by remember { mutableStateOf(false) }

    var showTimelineDialog by remember { mutableStateOf(false) }
    var editingChallenge by remember { mutableStateOf<WinterArcChallenge?>(null) }
    var editingWorkout by remember { mutableStateOf<WinterArcWorkoutItem?>(null) }
    var editingTask by remember { mutableStateOf<WinterArcDayTask?>(null) }
    var showAddChallengeDialog by remember { mutableStateOf(false) }
    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val totalChallenges = challenges.size.coerceAtLeast(1)
    val completedChallenges = challenges.count { it.isCompleted }
    val disciplineScore = (completedChallenges * 100) / totalChallenges

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            winterArcViewModel.scheduleAllSmartWaterSlots()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // October 1 Start Calculation
    val arcDaysInfo = remember(timelineMode) {
        calculateArcDays(timelineMode)
    }

    GlassBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        if (selectedSubTab != 0) {
                            selectedSubTab = 0
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .glassCard(cornerSize = 20.dp, borderWidth = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isLightMode) Color(0xFF0F172A) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val (headerTitle, headerSub) = when (selectedSubTab) {
                        1 -> if (isBn) "জলপান ট্র্যাকার" to "স্মার্ট হাইড্রেশন ও লগিং" else "HYDRATION HUB" to "Smart Hydration & Log"
                        2 -> if (isBn) "ডেইলি ব্লুপ্রিন্ট" to "রুটিন ও কাস্টম ব্যায়াম" else "DAILY BLUEPRINT" to "Workouts & Daily Routines"
                        3 -> if (isBn) "ক্যালেন্ডার ও রেকর্ড" to "দৈনিক রিপোর্ট ও অগ্রগতি" else "CALENDAR RECORDS" to "Daily History & Records"
                        else -> if (isBn) "WINTER ARC 2026" to "The Great Lock In • আত্মশুদ্ধি ও রূপান্তর" else "WINTER ARC 2026" to "The Great Lock In • Self Transformation"
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (selectedSubTab) {
                                1 -> Icons.Filled.WaterDrop
                                2 -> Icons.Filled.Schedule
                                3 -> Icons.Filled.CalendarMonth
                                else -> Icons.Filled.AcUnit
                            },
                            contentDescription = null,
                            tint = when (selectedSubTab) {
                                1 -> Color(0xFF00B4D8)
                                2 -> Color(0xFF10A37F)
                                3 -> Color(0xFFFFD54F)
                                else -> Color(0xFF00B4D8)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = headerTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = if (isLightMode) Color(0xFF0284C7) else Color(0xFF00B4D8)
                        )
                    }
                    Text(
                        text = headerSub,
                        fontSize = 11.sp,
                        color = if (isLightMode) Color(0xFF475569) else Color(0xFF94A3B8)
                    )
                }

                IconButton(
                    onClick = { showTimelineDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .glassCard(cornerSize = 20.dp, borderWidth = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Timeline",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Master Arc Notification Switch Banner (সরাসরি arc নোটিফিকেশন সুইচ)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .glassCard(cornerSize = 16.dp, glassAlpha = 0.95f),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (arcNotificationsEnabled) Color(0xFF00B4D8).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (arcNotificationsEnabled) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsOff,
                                contentDescription = null,
                                tint = if (arcNotificationsEnabled) Color(0xFF00B4D8) else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBn) "Winter Arc নোটিফিকেশন অ্যালার্ট" else "Winter Arc Notification Alerts",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLightMode) Color(0xFF0F172A) else Color.White
                            )
                            Text(
                                text = if (arcNotificationsEnabled) {
                                    if (isBn) "সক্রিয়: পানি, ব্যায়াম ও টাস্কের রিমাইন্ডার স্ক্রিন অফ থাকলেও আসবে" else "Active: Water, workout & night tasks alert on lockscreen"
                                } else {
                                    if (isBn) "নিষ্ক্রিয়: কোনো নোটিফিকেশন আসবে না" else "Disabled: No notifications will be sent"
                                },
                                fontSize = 10.sp,
                                color = if (arcNotificationsEnabled) Color(0xFF10A37F) else Color(0xFFEF4444),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Switch(
                        checked = arcNotificationsEnabled,
                        onCheckedChange = { winterArcViewModel.toggleArcNotifications(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00B4D8),
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Content Area based on selected sub tab
            Box(modifier = Modifier.weight(1f)) {
                when (selectedSubTab) {
                    0 -> ChallengesTabContent(
                        challenges = challenges,
                        onToggle = { winterArcViewModel.toggleChallenge(it) },
                        onEditTime = { editingChallenge = it },
                        onDelete = { winterArcViewModel.deleteChallenge(it.id) },
                        onAddClick = { showAddChallengeDialog = true },
                        arcDaysInfo = arcDaysInfo,
                        disciplineScore = disciplineScore,
                        completedChallenges = completedChallenges,
                        totalChallenges = totalChallenges,
                        isBeforeOctober = arcDaysInfo.isBeforeOctober,
                        daysUntilOctober = arcDaysInfo.daysUntilOctober,
                        previewUnlocked = previewUnlocked,
                        onTogglePreview = { previewUnlocked = !previewUnlocked },
                        isBn = isBn
                    )
                    1 -> WaterTabContent(
                        waterIntake = waterIntake,
                        waterTarget = waterTarget,
                        waterLogs = waterLogs,
                        reminderEnabled = waterReminderEnabled,
                        reminderIntervalMins = waterReminderInterval,
                        waterAutoMode = waterAutoMode,
                        isBeforeOctober = arcDaysInfo.isBeforeOctober,
                        daysUntilOctober = arcDaysInfo.daysUntilOctober,
                        previewUnlocked = previewUnlocked,
                        onTogglePreview = { previewUnlocked = !previewUnlocked },
                        onToggleAutoMode = { winterArcViewModel.toggleWaterAutoMode(it) },
                        onAddWater = { winterArcViewModel.addWater(it) },
                        onDeleteWaterLog = { winterArcViewModel.deleteWaterLog(it) },
                        onReset = { winterArcViewModel.resetWater() },
                        onSetTarget = { winterArcViewModel.setWaterTarget(it) },
                        onToggleReminder = { enabled, interval ->
                            winterArcViewModel.toggleWaterReminder(enabled, interval)
                        }
                    )
                    2 -> RoutineAndTasksTabContent(
                        workouts = workouts,
                        onToggleWorkout = { winterArcViewModel.toggleWorkout(it) },
                        onEditWorkout = { editingWorkout = it },
                        onDeleteWorkout = { winterArcViewModel.deleteWorkout(it.id) },
                        onAddWorkoutClick = { showAddWorkoutDialog = true },
                        dayTasks = dayTasks,
                        onToggleTask = { winterArcViewModel.toggleDayTask(it) },
                        onEditTask = { editingTask = it },
                        onDeleteTask = { winterArcViewModel.deleteDayTask(it) },
                        onAddTaskClick = { showAddTaskDialog = true }
                    )
                    3 -> CalendarAnalysisTabContent(
                        dayRecords = dayRecords,
                        timelineMode = timelineMode
                    )
                    else -> ChallengesTabContent(
                        challenges = challenges,
                        onToggle = { winterArcViewModel.toggleChallenge(it) },
                        onEditTime = { editingChallenge = it },
                        onDelete = { winterArcViewModel.deleteChallenge(it.id) },
                        onAddClick = { showAddChallengeDialog = true },
                        arcDaysInfo = arcDaysInfo,
                        disciplineScore = disciplineScore,
                        completedChallenges = completedChallenges,
                        totalChallenges = totalChallenges,
                        isBeforeOctober = arcDaysInfo.isBeforeOctober,
                        daysUntilOctober = arcDaysInfo.daysUntilOctober,
                        previewUnlocked = previewUnlocked,
                        onTogglePreview = { previewUnlocked = !previewUnlocked },
                        isBn = isBn
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Dedicated Winter Arc Bottom Navigation Bar (replaces app's bottom bar)
            WinterArcBottomBar(
                selectedTab = selectedSubTab,
                onTabSelected = { index ->
                    selectedSubTab = index
                }
            )
        }

        // Dialogs
        if (showTimelineDialog) {
            TimelineModeDialog(
                currentMode = timelineMode,
                onDismiss = { showTimelineDialog = false },
                onSelectMode = { mode, customDateMs ->
                    winterArcViewModel.setTimelineMode(mode, customDateMs)
                    showTimelineDialog = false
                }
            )
        }

        if (editingChallenge != null) {
            EditChallengeTimeDialog(
                challenge = editingChallenge!!,
                onDismiss = { editingChallenge = null },
                onSave = { newTime, customVal ->
                    winterArcViewModel.updateChallengeTime(editingChallenge!!, newTime, customVal)
                    editingChallenge = null
                }
            )
        }

        if (showAddChallengeDialog) {
            AddCustomChallengeDialog(
                onDismiss = { showAddChallengeDialog = false },
                onAdd = { title, sub, cat, time, valStr ->
                    winterArcViewModel.addCustomChallenge(title, sub, cat, time, valStr)
                    showAddChallengeDialog = false
                }
            )
        }

        if (editingWorkout != null) {
            EditWorkoutItemDialog(
                workout = editingWorkout!!,
                onDismiss = { editingWorkout = null },
                onSave = { updated ->
                    winterArcViewModel.updateWorkout(updated)
                    editingWorkout = null
                }
            )
        }

        if (showAddWorkoutDialog) {
            AddWorkoutItemDialog(
                onDismiss = { showAddWorkoutDialog = false },
                onAdd = { name, sets, reps, time ->
                    winterArcViewModel.addWorkout(name, sets, reps, time)
                    showAddWorkoutDialog = false
                }
            )
        }

        if (editingTask != null) {
            EditDayTaskDialog(
                task = editingTask!!,
                onDismiss = { editingTask = null },
                onSave = { updated, h, m ->
                    winterArcViewModel.updateDayTask(updated, h, m)
                    editingTask = null
                }
            )
        }

        if (showAddTaskDialog) {
            AddDayTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onAdd = { title, timeStr, dateStr, hour, minute ->
                    winterArcViewModel.addDayTask(title, timeStr, dateStr, hour, minute)
                    showAddTaskDialog = false
                }
            )
        }
    }
}

// ------------------- SUB TAB CONTENTS -------------------

@Composable
fun ChallengesTabContent(
    challenges: List<WinterArcChallenge>,
    onToggle: (WinterArcChallenge) -> Unit,
    onEditTime: (WinterArcChallenge) -> Unit,
    onDelete: (WinterArcChallenge) -> Unit,
    onAddClick: () -> Unit,
    arcDaysInfo: ArcDaysInfo,
    disciplineScore: Int,
    completedChallenges: Int,
    totalChallenges: Int,
    isBeforeOctober: Boolean = false,
    daysUntilOctober: Int = 0,
    previewUnlocked: Boolean = false,
    onTogglePreview: () -> Unit = {},
    isBn: Boolean = true
) {
    val isLightMode = GlassTheme.isLightMode.value
    // Sort challenges: uncompleted first, completed automatically move to bottom
    val sortedChallenges = remember(challenges) {
        challenges.sortedWith(compareBy<WinterArcChallenge> { it.isCompleted }.thenBy { it.orderIndex })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // October 1 Countdown Notice (Scrolls naturally with content)
        if (arcDaysInfo.isBeforeOctober) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp)
                        .glassCard(cornerSize = 20.dp, glassAlpha = 0.95f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7).copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.HourglassTop, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBn) "⏳ আর মাত্র ${arcDaysInfo.daysUntilOctober} দিন বাকি! (১ অক্টোবর থেকে শুরু)" else "⏳ Only ${arcDaysInfo.daysUntilOctober} days left! (Starts Oct 1)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                            Text(
                                text = if (isBn) "১ অক্টোবর ভোর ৫:০০ টায় স্বয়ংক্রিয়ভাবে ডে-১ শুরু হবে। এখন থেকেই ব্যায়াম ও রুটিন সাজিয়ে প্রস্তুত থাকুন!" else "Day-1 starts automatically on Oct 1 at 5:00 AM. Prepare routines and workouts now!",
                                fontSize = 11.sp,
                                color = Color.White,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Hero Arc Banner Card (Scrollable item with Pre-Arc / Day indicator)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
                    .glassCard(cornerSize = 26.dp, glassAlpha = 0.96f, hasSheen = true),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF00B4D8), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = arcDaysInfo.durationLabel,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00B4D8)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (arcDaysInfo.isBeforeOctober) {
                                    if (isBn) "প্রস্তুতি পর্ব (Pre-Arc)" else "Preparation Phase (Pre-Arc)"
                                } else {
                                    if (isBn) "দিন ${arcDaysInfo.currentDay} / ${arcDaysInfo.totalDays}" else "Day ${arcDaysInfo.currentDay} / ${arcDaysInfo.totalDays}"
                                },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isLightMode) Color(0xFF0F172A) else Color.White
                            )
                        }

                        // Discipline Percentage Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF0284C7).copy(alpha = 0.25f), Color(0xFF00B4D8).copy(alpha = 0.15f))
                                    ),
                                    RoundedCornerShape(20.dp)
                                )
                                .border(1.dp, Color(0xFF00B4D8).copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$disciplineScore%",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF00B4D8)
                                )
                                Text(
                                    text = if (isBn) "আজকের স্কোর" else "Score",
                                    fontSize = 9.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val animatedProgress by animateFloatAsState(
                        targetValue = (completedChallenges.toFloat() / totalChallenges),
                        animationSpec = tween(600),
                        label = "disciplineProgress"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF00B4D8),
                        trackColor = if (isLightMode) Color(0xFFE2E8F0) else Color(0xFF2A2D3A)
                    )
                }
            }
        }

        if (isBeforeOctober && !previewUnlocked) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(cornerSize = 22.dp, glassAlpha = 0.98f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.85f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFFFD54F).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🔒 উইন্টার আর্ক ১ অক্টোবর স্বয়ংক্রিয়ভাবে শুরু হবে",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "চ্যালেঞ্জের ট্র্যাকিং ও দৈনিক স্কোর ১ অক্টোবর ভোর ৫:০০ AM থেকে স্বয়ংক্রিয়ভাবে গণনা শুরু হবে। আর মাত্র $daysUntilOctober দিন বাকি! এখন থেকেই রুটিন ও ওয়ার্কআউট সাজিয়ে সম্পূর্ণ প্রস্তুত থাকুন।",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onTogglePreview,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00B4D8)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("টেস্ট আনলক / প্রিভিউ মোড চালু করুন", fontSize = 11.5.sp, color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "দৈনিক নিয়মাবলী (${challenges.count { it.isCompleted }}/${challenges.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLightMode) Color(0xFF0F172A) else Color.White
                )

                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ নতুন চ্যালেঞ্জ", fontSize = 11.5.sp, color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold)
                }
            }
        }

        items(sortedChallenges, key = { it.id }) { challenge ->
            ChallengeItemCard(
                challenge = challenge,
                onToggle = { onToggle(challenge) },
                onEditTime = { onEditTime(challenge) },
                onDelete = { onDelete(challenge) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ChallengeItemCard(
    challenge: WinterArcChallenge,
    onToggle: () -> Unit,
    onEditTime: () -> Unit,
    onDelete: () -> Unit
) {
    val isLightMode = GlassTheme.isLightMode.value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerSize = 22.dp, glassAlpha = 0.95f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (challenge.isCompleted) Color(0xFF10A37F) else (if (isLightMode) Color(0xFFE2E8F0) else Color(0xFF2A2D3A))
                    )
            ) {
                if (challenge.isCompleted) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Box(modifier = Modifier.size(14.dp).border(2.dp, Color(0xFF94A3B8), CircleShape))
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (challenge.isCompleted) Color(0xFF10A37F) else (if (isLightMode) Color(0xFF0F172A) else Color.White),
                    textDecoration = if (challenge.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = challenge.subtitle,
                    fontSize = 11.5.sp,
                    color = if (isLightMode) Color(0xFF475569) else Color(0xFF94A3B8),
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clickable { onEditTime() }
                            .background(Color(0xFF00B4D8).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF00B4D8).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccessTime, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (challenge.targetTime.isNotBlank()) challenge.targetTime else "সময় দিন",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00B4D8)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.Edit, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(10.dp))
                        }
                    }
                }
            }

            if (challenge.isCustom) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ------------------- ROUTINE & TASKS TAB CONTENT -------------------

@Composable
fun RoutineAndTasksTabContent(
    workouts: List<WinterArcWorkoutItem>,
    onToggleWorkout: (WinterArcWorkoutItem) -> Unit,
    onEditWorkout: (WinterArcWorkoutItem) -> Unit,
    onDeleteWorkout: (WinterArcWorkoutItem) -> Unit,
    onAddWorkoutClick: () -> Unit,
    dayTasks: List<WinterArcDayTask>,
    onToggleTask: (WinterArcDayTask) -> Unit,
    onEditTask: (WinterArcDayTask) -> Unit,
    onDeleteTask: (WinterArcDayTask) -> Unit,
    onAddTaskClick: () -> Unit
) {
    var activeSubMode by remember { mutableStateOf(0) } // 0: Workouts, 1: Night Tasks
    val isLightMode = GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"

    Column(modifier = Modifier.fillMaxSize()) {
        // Switcher between Workout and Night Tasks
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .glassCard(cornerSize = 20.dp, glassAlpha = 0.95f)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { activeSubMode = 0 },
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubMode == 0) Color(0xFF00B4D8) else Color.Transparent
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.FitnessCenter, 
                        contentDescription = null, 
                        tint = if (activeSubMode == 0) Color.White else Color.Gray, 
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isBn) "ব্যায়াম রুটিন" else "Workouts", 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = if (activeSubMode == 0) Color.White else if (isLightMode) Color(0xFF334155) else Color.LightGray
                    )
                }
            }

            Button(
                onClick = { activeSubMode = 1 },
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubMode == 1) Color(0xFF00B4D8) else Color.Transparent
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.NightsStay, 
                        contentDescription = null, 
                        tint = if (activeSubMode == 1) Color.White else Color.Gray, 
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isBn) "নাইট টাস্ক প্ল্যানার" else "Night Tasks", 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = if (activeSubMode == 1) Color.White else if (isLightMode) Color(0xFF334155) else Color.LightGray
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (activeSubMode == 0) {
                WorkoutTabContent(
                    workouts = workouts,
                    onToggle = onToggleWorkout,
                    onEdit = onEditWorkout,
                    onDelete = onDeleteWorkout,
                    onAddClick = onAddWorkoutClick
                )
            } else {
                NightBlueprintTabContent(
                    dayTasks = dayTasks,
                    onToggle = onToggleTask,
                    onEdit = onEditTask,
                    onDelete = onDeleteTask,
                    onAddClick = onAddTaskClick
                )
            }
        }
    }
}

// ------------------- WORKOUT CUSTOMIZER -------------------

@Composable
fun WorkoutTabContent(
    workouts: List<WinterArcWorkoutItem>,
    onToggle: (WinterArcWorkoutItem) -> Unit,
    onEdit: (WinterArcWorkoutItem) -> Unit,
    onDelete: (WinterArcWorkoutItem) -> Unit,
    onAddClick: () -> Unit
) {
    val isLightMode = GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"
    // Sort workouts: uncompleted on top, checked workouts automatically sink to bottom
    val sortedWorkouts = remember(workouts) {
        workouts.sortedWith(compareBy<WinterArcWorkoutItem> { it.isCompleted }.thenBy { it.id })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerSize = 24.dp, glassAlpha = 0.95f),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "ডেইলি ওয়ার্কআউট প্রটোকল" else "Daily Workout Protocol",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLightMode) Color(0xFF0F172A) else Color.White
                            )
                            Text(
                                text = if (isBn) "নির্ধারিত সময়ে ফোনে অটোমেটিক অ্যালার্ম নোটিফিকেশন আসবে!" else "Automatic alarm notifications will fire at scheduled times!",
                                fontSize = 11.sp,
                                color = if (isLightMode) Color(0xFF475569) else Color(0xFF94A3B8),
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onAddClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "ব্যায়াম যোগ" else "Add", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(sortedWorkouts, key = { it.id }) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerSize = 22.dp, glassAlpha = 0.95f),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onToggle(item) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (item.isCompleted) Color(0xFF10A37F) else (if (isLightMode) Color(0xFFE2E8F0) else Color(0xFF2A2D3A))
                            )
                    ) {
                        if (item.isCompleted) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Box(modifier = Modifier.size(14.dp).border(2.dp, Color(0xFF94A3B8), CircleShape))
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isCompleted) Color(0xFF10A37F) else (if (isLightMode) Color(0xFF0F172A) else Color.White),
                            textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "🎯 ${item.sets} Sets • ${item.repsOrDuration}",
                                fontSize = 11.5.sp,
                                color = Color(0xFF00B4D8),
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = item.targetTime,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }

                    IconButton(onClick = { onEdit(item) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color(0xFF00B4D8), modifier = Modifier.size(18.dp))
                    }

                    IconButton(onClick = { onDelete(item) }) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ------------------- NIGHT BLUEPRINT / DAY PLANNER -------------------

@Composable
fun NightBlueprintTabContent(
    dayTasks: List<WinterArcDayTask>,
    onToggle: (WinterArcDayTask) -> Unit,
    onEdit: (WinterArcDayTask) -> Unit,
    onDelete: (WinterArcDayTask) -> Unit,
    onAddClick: () -> Unit
) {
    val isLightMode = GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"
    // Sort tasks: uncompleted on top, checked tasks automatically sink to bottom
    val sortedTasks = remember(dayTasks) {
        dayTasks.sortedWith(compareBy<WinterArcDayTask> { it.isCompleted }.thenBy { it.scheduledTime })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerSize = 24.dp, glassAlpha = 0.95f),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.NightsStay, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "নাইট রুটিন ও আগামীকালের প্ল্যানার 🌙" else "Night Routine & Day Planner 🌙",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLightMode) Color(0xFF0F172A) else Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBn) "আগের রাতে পরের দিনের টাইম-ব্লক শিডিউল সেট করুন। নির্ধারিত সময়ে ফোনের পর্দায় অটোমেটিক নোটিফিকেশন অ্যালার্ট আসবে!" else "Plan tomorrow's time blocks. Scheduled alarm alerts will pop up on your screen!",
                        fontSize = 11.5.sp,
                        color = if (isLightMode) Color(0xFF475569) else Color(0xFF94A3B8),
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onAddClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Filled.AddAlert, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "নতুন টাস্ক যোগ করুন" else "Add New Task", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        item {
            Text(
                text = "পরবর্তী টাস্ক তালিকা (${dayTasks.count { it.isCompleted }}/${dayTasks.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (dayTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "কোনো শিডিউল টাস্ক নেই। আজ রাতেই আগামীকালের প্ল্যান তৈরি করুন!",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(sortedTasks, key = { it.id }) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(cornerSize = 22.dp, glassAlpha = 0.95f),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onToggle(task) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (task.isCompleted) Color(0xFF10A37F) else (if (isLightMode) Color(0xFFE2E8F0) else Color(0xFF2A2D3A))
                                )
                        ) {
                            if (task.isCompleted) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Box(modifier = Modifier.size(14.dp).border(2.dp, Color(0xFF94A3B8), CircleShape))
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (task.isCompleted) Color(0xFF10A37F) else (if (isLightMode) Color(0xFF0F172A) else Color.White),
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "সময়: ${task.scheduledTime} (${task.targetDate})",
                                    fontSize = 11.sp,
                                    color = Color(0xFF00B4D8),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(onClick = { onEdit(task) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color(0xFF00B4D8), modifier = Modifier.size(18.dp))
                        }

                        IconButton(onClick = { onDelete(task) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ------------------- REAL HYDRATION TRACKER (NO DEMO DATA, FULL RESET) -------------------

@Composable
fun WaterTabContent(
    waterIntake: Int,
    waterTarget: Int,
    waterLogs: List<WaterLogEntry>,
    reminderEnabled: Boolean,
    reminderIntervalMins: Int,
    waterAutoMode: Boolean = true,
    isBeforeOctober: Boolean = false,
    daysUntilOctober: Int = 0,
    previewUnlocked: Boolean = false,
    onTogglePreview: () -> Unit = {},
    onToggleAutoMode: (Boolean) -> Unit = {},
    onAddWater: (Int) -> Unit,
    onDeleteWaterLog: (WaterLogEntry) -> Unit,
    onReset: () -> Unit,
    onSetTarget: (Int) -> Unit,
    onToggleReminder: (Boolean, Int) -> Unit
) {
    val isLightMode = GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val percent = ((waterIntake.toFloat() / waterTarget.coerceAtLeast(1)) * 100).toInt().coerceAtMost(100)

    var showCustomWaterDialog by remember { mutableStateOf(false) }
    var customAmountInput by remember { mutableStateOf("250") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val cal = Calendar.getInstance()
    val curMins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val nextUpcomingSlot = remember(curMins) {
        smartWaterSlots.firstOrNull { (it.hour * 60 + it.minute) > curMins } ?: smartWaterSlots.first()
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = Color(0xFF16171E),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "পানি পানের হিসাব রিস্টার্ট করবেন?" else "Reset Today's Water Intake?",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Text(
                    text = if (isBn) "আজকের সকল পানি পানের রেকর্ড মুছে হিসাব ০ ml এ নিয়ে যাওয়া হবে। আপনি কি নিশ্চিত?" else "This will clear today's water intake back to 0 ml. Are you sure?",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReset()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBn) "হ্যাঁ, রিস্টার্ট করুন" else "Yes, Reset", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showResetConfirmDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = Color.Gray)
                }
            }
        )
    }

    if (showCustomWaterDialog) {
        Dialog(onDismissRequest = { showCustomWaterDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().glassCard(cornerSize = 24.dp, glassAlpha = 0.98f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13151F)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isBn) "পানি পানের পরিমাণ লিখুন (ml)" else "Enter Water Intake (ml)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = customAmountInput,
                        onValueChange = { customAmountInput = it.filter { ch -> ch.isDigit() } },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00B4D8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        placeholder = { Text("e.g. 250", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCustomWaterDialog = false },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isBn) "বাতিল" else "Cancel", color = Color(0xFF94A3B8))
                        }
                        Button(
                            onClick = {
                                val amt = customAmountInput.toIntOrNull() ?: 250
                                if (amt > 0) {
                                    onAddWater(amt)
                                }
                                showCustomWaterDialog = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isBn) "যোগ করুন" else "Add", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector: ২টা অপশন (মেনুয়ালি ও অটো সেট)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerSize = 18.dp, glassAlpha = 0.95f),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Option 1: অটো সেট (সকাল ৫:০৫ - রাত ১০:০০)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (waterAutoMode) Color(0xFF00B4D8) else Color(0xFF1E212B)
                            )
                            .border(
                                1.dp,
                                if (waterAutoMode) Color(0xFF00B4D8) else Color(0xFF334155),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onToggleAutoMode(true) }
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = if (waterAutoMode) Color.White else Color(0xFF00B4D8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "⚡ অটো সেট (৫:০৫ - ১০:০০)" else "⚡ Auto Set (5:05-22:00)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (waterAutoMode) Color.White else Color(0xFFCBD5E1)
                            )
                        }
                    }

                    // Option 2: মেনুয়ালি সেট
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (!waterAutoMode) Color(0xFF00B4D8) else Color(0xFF1E212B)
                            )
                            .border(
                                1.dp,
                                if (!waterAutoMode) Color(0xFF00B4D8) else Color(0xFF334155),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onToggleAutoMode(false) }
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint = if (!waterAutoMode) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "✏️ মেনুয়ালি সেট" else "✏️ Manual Set",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!waterAutoMode) Color.White else Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }
        }

        // October 1 Lock Banner (সব অপশন লক থাকবে, অক্টোবর ১ তারিখ হলেই অটো আনলক হয়ে যাবে)
        if (isBeforeOctober && !previewUnlocked) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(cornerSize = 22.dp, glassAlpha = 0.98f, hasSheen = true),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.95f)),
                    border = BorderStroke(1.5.dp, Color(0xFFFFD54F).copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD54F).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isBn) "🔒 পানি পান চ্যালেঞ্জ ১ অক্টোবর স্বয়ংক্রিয়ভাবে শুরু হবে" else "🔒 Water Challenge Unlocks Automatically on Oct 1",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD54F),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBn) {
                                "সকাল ৫:০৫ থেকে রাত ১০:০০ পর্যন্ত প্রতিটি সময়ের পানি পানের অটো রুটিন ও নোটিফিকেশন সাজানো আছে। ১ অক্টোবর ভোর ৫:০৫ AM এ চ্যালেঞ্জটি স্বয়ংক্রিয়ভাবে সবার জন্য আনলক হয়ে যাবে! আর মাত্র $daysUntilOctober দিন বাকি।"
                            } else {
                                "Smart schedule from 5:05 AM to 10:00 PM with auto notifications is prepared. It will unlock automatically on Oct 1 at 5:05 AM! Only $daysUntilOctober days remaining."
                            },
                            fontSize = 11.5.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onTogglePreview,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "👁️ প্রিভিউ আনলক দেখুন (টেস্ট মোড)" else "👁️ Preview Unlock (Test Mode)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Main Intake Card (চেপ্টা গোলাকার - 26dp)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerSize = 26.dp, glassAlpha = 0.95f),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (waterAutoMode) {
                                    if (isBn) "💧 অটো হাইড্রেশন (০৫:০৫ - ১০:০০)" else "💧 AUTO HYDRATION (5:05-22:00)"
                                } else {
                                    if (isBn) "💧 ম্যানুয়াল হাইড্রেশন" else "💧 MANUAL HYDRATION"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00B4D8),
                                letterSpacing = 0.8.sp
                            )
                        }

                        Text(
                            text = if (isBn) "লক্ষ্য: $waterTarget ml" else "Target: $waterTarget ml",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${waterIntake} ml",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isLightMode) Color(0xFF0F172A) else Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isBn) "লক্ষ্যের $percent% সম্পন্ন হয়েছে" else "$percent% of daily goal reached",
                        fontSize = 12.5.sp,
                        color = if (percent >= 100) Color(0xFF10A37F) else Color(0xFF00B4D8),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (waterIntake.toFloat() / waterTarget).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (percent >= 100) Color(0xFF10A37F) else Color(0xFF00B4D8),
                        trackColor = if (isLightMode) Color(0xFFE2E8F0) else Color(0xFF2A2D3A)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Prominent Direct Restart/Reset & Custom Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showResetConfirmDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "রিস্টার্ট (০ ml)" else "Restart (0 ml)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }

                        Button(
                            onClick = { showCustomWaterDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "+ কাস্টম যোগ" else "+ Custom", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Auto Mode: 05:05 AM to 10:00 PM Smart Schedule Timeline
        if (waterAutoMode) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(cornerSize = 22.dp, glassAlpha = 0.95f),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "সকাল ৫:০৫ - রাত ১০:০০ অটো শিডিউল" else "5:05 AM - 10:00 PM Auto Schedule",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF10A37F).copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isBn) "৯টি স্লট সক্রিয়" else "9 Slots Active",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10A37F)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isBn) {
                                "🔔 পরবর্তী পানি পানের সময়: ${nextUpcomingSlot.timeLabel} (${nextUpcomingSlot.amountMl} ml) — ${nextUpcomingSlot.note}"
                            } else {
                                "🔔 Next Water Slot: ${nextUpcomingSlot.timeLabel} (${nextUpcomingSlot.amountMl} ml) — ${nextUpcomingSlot.note}"
                            },
                            fontSize = 11.5.sp,
                            color = Color(0xFF00B4D8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // List of the 9 Smart Slots from 5:05 AM to 10:00 PM
            item {
                Text(
                    text = if (isBn) "⏰ সকাল ৫:০৫ থেকে রাত ১০:০০ এর ৯টি পানির স্লট:" else "⏰ 9 Scheduled Water Slots (5:05 AM - 10:00 PM):",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp)
                )
            }

            items(smartWaterSlots) { slot ->
                val slotMins = slot.hour * 60 + slot.minute
                val isPassed = curMins >= slotMins
                val isNext = slot.id == nextUpcomingSlot.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(cornerSize = 16.dp, glassAlpha = 0.95f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNext) Color(0xFF00B4D8).copy(alpha = 0.12f) else Color.Transparent
                    ),
                    border = if (isNext) BorderStroke(1.dp, Color(0xFF00B4D8).copy(alpha = 0.5f)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isNext) Color(0xFF00B4D8)
                                        else if (isPassed) Color(0xFF10A37F).copy(alpha = 0.2f)
                                        else Color(0xFF1E212B)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.WaterDrop,
                                    contentDescription = null,
                                    tint = if (isNext) Color.White else if (isPassed) Color(0xFF10A37F) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = slot.timeLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isNext) Color(0xFF00B4D8) else (if (isLightMode) Color(0xFF0F172A) else Color.White)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF0284C7).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text("${slot.amountMl} ml", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = slot.note,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        Button(
                            onClick = { onAddWater(slot.amountMl) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isNext) Color(0xFF00B4D8) else Color(0xFF132A3E)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = "+${slot.amountMl} ml",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNext) Color.White else Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }
        } else {
            // Manual Mode: Custom Target & Reminder Settings
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(cornerSize = 22.dp, glassAlpha = 0.95f),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Alarm, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isBn) "পানি পানের অ্যালার্ম ও রিমাইন্ডার" else "Hydration Reminder Alarm",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLightMode) Color(0xFF0F172A) else Color.White
                                    )
                                    Text(
                                        text = if (isBn) "নিয়মিত বিরতিতে নোটিফিকেশন অ্যালার্ট" else "Interval notification alerts",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { onToggleReminder(it, reminderIntervalMins) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF00B4D8)
                                )
                            )
                        }

                        if (reminderEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isBn) "রিমাইন্ডার বিরতি:" else "Reminder Interval:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFCBD5E1)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val intervals = listOf(
                                    30 to if (isBn) "৩০ মি." else "30m",
                                    45 to if (isBn) "৪৫ মি." else "45m",
                                    60 to if (isBn) "১ ঘণ্টা" else "1h",
                                    90 to if (isBn) "১.৫ ঘণ্টা" else "1.5h",
                                    120 to if (isBn) "২ ঘণ্টা" else "2h"
                                )
                                intervals.forEach { (mins, label) ->
                                    val isSelected = reminderIntervalMins == mins
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(0xFF00B4D8) else Color(0xFF1E212B))
                                            .border(1.dp, if (isSelected) Color(0xFF00B4D8) else Color(0xFF334155), RoundedCornerShape(10.dp))
                                            .clickable { onToggleReminder(true, mins) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isBn) "দৈনিক লক্ষ্য পরিবর্তন করুন:" else "Change Daily Target:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFCBD5E1)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val targets = listOf(2500, 3000, 3500, 4000)
                            targets.forEach { tVal ->
                                val isSelected = waterTarget == tVal
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFF00B4D8) else Color(0xFF1E212B))
                                        .border(1.dp, if (isSelected) Color(0xFF00B4D8) else Color(0xFF334155), RoundedCornerShape(10.dp))
                                        .clickable { onSetTarget(tVal) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$tVal ml",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Instant Add Buttons (কাটহীন সরাসরি এক ক্লিকে পানি যোগ)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isBn) "সহজে এক ক্লিকে পানি যোগ করুন:" else "Quick Add Water:",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // +250 ml Glass
                    Button(
                        onClick = { onAddWater(250) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLightMode) Color(0xFFE0F2FE) else Color(0xFF132A3E),
                            contentColor = Color(0xFF00B4D8)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFF00B4D8).copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f).height(74.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.LocalDrink, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("+২৫০ ml", fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF00B4D8))
                            Text(if (isBn) "১ গ্লাস" else "1 Glass", fontSize = 9.5.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    // +500 ml Bottle
                    Button(
                        onClick = { onAddWater(500) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLightMode) Color(0xFFE0F2FE) else Color(0xFF132A3E),
                            contentColor = Color(0xFF38BDF8)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f).height(74.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("+৫০০ ml", fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF38BDF8))
                            Text(if (isBn) "১ বোতল" else "1 Bottle", fontSize = 9.5.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    // +1000 ml Liter
                    Button(
                        onClick = { onAddWater(1000) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLightMode) Color(0xFFE0F2FE) else Color(0xFF132A3E),
                            contentColor = Color(0xFF0284C7)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f).height(74.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.Opacity, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("+১০০০ ml", fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF0284C7))
                            Text(if (isBn) "১ লিটার" else "1 Liter", fontSize = 9.5.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }

        // Today's Drink Logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "📋 আজকের পানি পানের তালিকা (${waterLogs.size}টি এন্ট্রি):" else "📋 Today's Drink Logs (${waterLogs.size}):",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLightMode) Color(0xFF0F172A) else Color.White
                )

                if (waterLogs.isNotEmpty()) {
                    TextButton(
                        onClick = { showResetConfirmDialog = true },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(if (isBn) "সব মুছুন" else "Clear All", fontSize = 11.sp, color = Color(0xFFEF4444))
                    }
                }
            }
        }

        if (waterLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(cornerSize = 18.dp, glassAlpha = 0.95f),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = Color(0xFF00B4D8).copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "আজকে এখনো কোনো পানি রেকর্ড করা হয়নি।" else "No water intake recorded yet today.",
                            fontSize = 12.5.sp,
                            color = Color(0xFFCBD5E1),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isBn) "পানি পান করলে উপরের বাটন বা স্লটে চেপে সহজেই যোগ করুন!" else "Tap the quick add buttons or slots whenever you drink water!",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        } else {
            items(waterLogs.reversed()) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(cornerSize = 16.dp, glassAlpha = 0.95f),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00B4D8).copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "+${log.amountMl} ml",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLightMode) Color(0xFF0F172A) else Color.White
                                )
                                Text(
                                    text = log.timeFormatted,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onDeleteWaterLog(log) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete", tint = Color(0xFFEF4444).copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ------------------- CALENDAR ANALYSIS TAB -------------------

@Composable
fun CalendarAnalysisTabContent(
    dayRecords: List<WinterArcDayRecord>,
    timelineMode: String
) {
    val isLightMode = GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"

    var currentDisplayCal by remember { mutableStateOf(Calendar.getInstance()) }
    val displayYear = currentDisplayCal.get(Calendar.YEAR)
    val displayMonth = currentDisplayCal.get(Calendar.MONTH)

    val monthNameBn = remember(displayMonth, displayYear, isBn) {
        val monthsBn = arrayOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
        val monthsEn = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        if (isBn) "${monthsBn[displayMonth]} $displayYear" else "${monthsEn[displayMonth]} $displayYear"
    }

    val daysInMonth = remember(displayMonth, displayYear) {
        val c = Calendar.getInstance().apply {
            set(Calendar.YEAR, displayYear)
            set(Calendar.MONTH, displayMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        c.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeek = remember(displayMonth, displayYear) {
        val c = Calendar.getInstance().apply {
            set(Calendar.YEAR, displayYear)
            set(Calendar.MONTH, displayMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        c.get(Calendar.DAY_OF_WEEK)
    }

    val todayCalendar = Calendar.getInstance()
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todayCalendar.time)

    val recordMap = remember(dayRecords) {
        dayRecords.associateBy { it.dateStr }
    }

    val totalCompletedDays = dayRecords.count { it.isCompleted }
    val totalMissedDays = dayRecords.count { it.isMissed }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Summary Analysis Cards (চেপ্টা গোলাকার - 24dp)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerSize = 24.dp, glassAlpha = 0.95f),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBn) "📊 ধারাবাহিকতা ও অগ্রগতি বিশ্লেষণ" else "📊 Consistency & Progress Analysis",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00B4D8)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalysisMetricPill(
                            title = if (isBn) "শুরু" else "Start",
                            value = if (isBn) "১ অক্টোবর" else "Oct 1",
                            color = Color(0xFFFFD54F),
                            modifier = Modifier.weight(1f)
                        )
                        AnalysisMetricPill(
                            title = if (isBn) "সফল দিন" else "Success",
                            value = if (isBn) "$totalCompletedDays দিন" else "$totalCompletedDays Days",
                            color = Color(0xFF10A37F),
                            modifier = Modifier.weight(1f)
                        )
                        AnalysisMetricPill(
                            title = if (isBn) "মিস দিন" else "Missed",
                            value = if (isBn) "$totalMissedDays দিন" else "$totalMissedDays Days",
                            color = Color(0xFFEF4444),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Monthly Calendar Navigation & Grid (চেপ্টা গোলাকার - 26dp)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerSize = 26.dp, glassAlpha = 0.96f),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val c = Calendar.getInstance().apply {
                                    time = currentDisplayCal.time
                                    add(Calendar.MONTH, -1)
                                }
                                currentDisplayCal = c
                            },
                            modifier = Modifier.size(34.dp).glassCard(cornerSize = 17.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(20.dp))
                        }

                        Text(
                            text = monthNameBn,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00B4D8)
                        )

                        IconButton(
                            onClick = {
                                val c = Calendar.getInstance().apply {
                                    time = currentDisplayCal.time
                                    add(Calendar.MONTH, 1)
                                }
                                currentDisplayCal = c
                            },
                            modifier = Modifier.size(34.dp).glassCard(cornerSize = 17.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val weekdays = if (isBn) listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি") else listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        weekdays.forEach { w ->
                            Text(
                                text = w,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val totalCells = (firstDayOfWeek - 1) + daysInMonth
                    val rowCount = ((totalCells + 6) / 7)

                    for (row in 0 until rowCount) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 1..7) {
                                val cellIndex = (row * 7) + col
                                val dayNum = cellIndex - (firstDayOfWeek - 1)

                                if (dayNum in 1..daysInMonth) {
                                    val cellDateStr = String.format(Locale.US, "%04d-%02d-%02d", displayYear, displayMonth + 1, dayNum)
                                    val isToday = cellDateStr == todayDateStr
                                    val record = recordMap[cellDateStr]

                                    val isPast = isDateInPast(displayYear, displayMonth, dayNum, todayCalendar)
                                    val isSuccess = record?.isCompleted == true
                                    val isMissed = isPast && !isSuccess

                                    CalendarDayCell(
                                        dayNum = dayNum,
                                        isToday = isToday,
                                        isSuccess = isSuccess,
                                        isMissed = isMissed,
                                        isPast = isPast,
                                        isLightMode = isLightMode
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = Color(0xFF10A37F), label = "সফল দিন 🟢")
                        LegendItem(color = Color(0xFFEF4444), label = "মিস দিন 🔴")
                        LegendItem(color = Color(0xFF00B4D8), label = "আজকের দিন ⚡")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AnalysisMetricPill(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isLightMode = GlassTheme.isLightMode.value
    Box(
        modifier = modifier
            .background(
                if (isLightMode) Color(0xFFF1F5F9) else Color(0xFF1E212B),
                RoundedCornerShape(16.dp)
            )
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 10.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun CalendarDayCell(
    dayNum: Int,
    isToday: Boolean,
    isSuccess: Boolean,
    isMissed: Boolean,
    isPast: Boolean,
    isLightMode: Boolean
) {
    val bgColor = when {
        isToday -> Color(0xFF00B4D8).copy(alpha = 0.25f)
        isSuccess -> Color(0xFF10A37F).copy(alpha = 0.25f)
        isMissed -> Color(0xFFEF4444).copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    val borderColor = when {
        isToday -> Color(0xFF00B4D8)
        isSuccess -> Color(0xFF10A37F)
        isMissed -> Color(0xFFEF4444).copy(alpha = 0.45f)
        else -> Color.Transparent
    }

    val textColor = when {
        isToday -> Color(0xFF00B4D8)
        isSuccess -> Color(0xFF10A37F)
        isMissed -> Color(0xFFEF4444)
        isPast -> if (isLightMode) Color(0xFF0F172A) else Color.White
        else -> Color(0xFF64748B)
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(if (borderColor != Color.Transparent) 1.2.dp else 0.dp, borderColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayNum.toString(),
                fontSize = 12.sp,
                fontWeight = if (isToday || isSuccess) FontWeight.Black else FontWeight.Medium,
                color = textColor
            )
            if (isSuccess) {
                Box(modifier = Modifier.size(3.dp).background(Color(0xFF10A37F), CircleShape))
            } else if (isMissed) {
                Box(modifier = Modifier.size(3.dp).background(Color(0xFFEF4444), CircleShape))
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.5.sp, color = Color(0xFFCBD5E1))
    }
}

fun isDateInPast(year: Int, month: Int, day: Int, todayCal: Calendar): Boolean {
    val checkCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
    }
    return checkCal.timeInMillis < todayCal.timeInMillis
}

// ------------------- DIALOGS -------------------

@Composable
fun TimelineModeDialog(
    currentMode: String,
    onDismiss: () -> Unit,
    onSelectMode: (String, Long) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 24.dp, glassAlpha = 0.98f),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Tune, contentDescription = null, tint = Color(0xFF00B4D8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Winter Arc সময়সীমা নির্ধারণ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "আপনি কত দিনের উইন্টার আর্ক চ্যালেঞ্জ নিতে চান তা বেছে নিন:",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(12.dp))

                TimelineOptionRow(
                    title = "🏆 স্ট্যান্ডার্ড ৯০ দিন (1 Oct - 31 Dec)",
                    subtitle = "ক্লাসিক ৩ মাসের চরম আত্মউন্নয়ন চ্যালেঞ্জ",
                    isSelected = currentMode == "90_days",
                    onClick = { onSelectMode("90_days", 0L) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TimelineOptionRow(
                    title = "❄️ ফুল উইন্টার আর্ক (1 Oct - 28 Feb)",
                    subtitle = "পুরো শীতকাল ও ফেব্রুয়ারি মাস পর্যন্ত ১৫০ দিনের গভীর রূপান্তর",
                    isSelected = currentMode == "150_days",
                    onClick = { onSelectMode("150_days", 0L) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2D3A)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("বন্ধ করুন", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun TimelineOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .background(if (isSelected) Color(0xFF00B4D8).copy(alpha = 0.2f) else Color(0xFF202129))
            .border(
                1.dp,
                if (isSelected) Color(0xFF00B4D8) else Color(0xFF2E303C),
                RoundedCornerShape(18.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = if (isSelected) Color(0xFF00B4D8) else Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun EditChallengeTimeDialog(
    challenge: WinterArcChallenge,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val context = LocalContext.current
    var timeText by remember { mutableStateOf(challenge.targetTime) }
    var valueText by remember { mutableStateOf(challenge.customValue) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().glassCard(cornerSize = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("চ্যালেঞ্জের সময় ও লক্ষ্য কাস্টমাইজ", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(challenge.title, fontSize = 11.5.sp, color = Color(0xFF00B4D8), maxLines = 2)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("নির্ধারিত সময় (যেমন: 05:30 AM)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    val ampm = if (h >= 12) "PM" else "AM"
                                    val h12 = if (h % 12 == 0) 12 else h % 12
                                    timeText = String.format(Locale.US, "%02d:%02d %s", h12, m, ampm)
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                false
                            ).show()
                        }) {
                            Icon(Icons.Filled.AccessTime, contentDescription = null, tint = Color(0xFF00B4D8))
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text("টার্গেট মাত্রা (যেমন: 100 pushups বা 3 km)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("বাতিল", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(timeText, valueText) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
                    ) {
                        Text("সংরক্ষণ", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AddCustomChallengeDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("06:00 AM") }
    var customVal by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().glassCard(cornerSize = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("নতুন উইন্টার আর্ক চ্যালেঞ্জ যোগ করুন", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("চ্যালেঞ্জের নাম *") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("সংক্ষিপ্ত বিবরণ *") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("সময় (যেমন: 06:00 AM)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = customVal,
                    onValueChange = { customVal = it },
                    label = { Text("লক্ষ্য মাত্রা (ঐচ্ছিক)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("বাতিল", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAdd(title, subtitle, "custom", time, customVal)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
                    ) {
                        Text("যোগ করুন", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AddWorkoutItemDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var setsText by remember { mutableStateOf("3") }
    var repsText by remember { mutableStateOf("15 reps") }
    var timeText by remember { mutableStateOf("07:00 AM") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().glassCard(cornerSize = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("নতুন ব্যায়াম কাস্টমাইজ করুন", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text("নির্ধারিত সময়ে ফোনে স্বয়ংক্রিয় অ্যালার্ম আসবে", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ব্যায়ামের নাম (যেমন: পুশ-আপ, ডাম্বেল)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = setsText,
                        onValueChange = { setsText = it },
                        label = { Text("সেট সংখ্যা") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { repsText = it },
                        label = { Text("র্যাপস / সময়") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1.4f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("সময় (যেমন: 07:00 AM)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("বাতিল", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val sets = setsText.toIntOrNull() ?: 3
                            if (name.isNotBlank()) {
                                onAdd(name, sets, repsText, timeText)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
                    ) {
                        Text("সংরক্ষণ", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AddDayTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Int, Int) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var selectedHour by remember { mutableIntStateOf(6) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var timeFormatted by remember { mutableStateOf("06:00 AM") }

    val calTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    var targetDateStr by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calTomorrow.time))
    }
    var isTomorrow by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().glassCard(cornerSize = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("আগামীকালের রুটিন টাস্ক ও নোটিফিকেশন", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("কাজের বিবরণ (যেমন: ফজর ও রানিং)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                selectedHour = h
                                selectedMinute = m
                                val ampm = if (h >= 12) "PM" else "AM"
                                val h12 = if (h % 12 == 0) 12 else h % 12
                                timeFormatted = String.format(Locale.US, "%02d:%02d %s", h12, m, ampm)
                            },
                            selectedHour,
                            selectedMinute,
                            false
                        ).show()
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Filled.AccessTime, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("অ্যালার্ট সময়: $timeFormatted", fontWeight = FontWeight.Bold, color = Color(0xFF00B4D8), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isTomorrow,
                        onClick = {
                            isTomorrow = true
                            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                            targetDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.time)
                        },
                        label = { Text("আগামীকাল (Tomorrow)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isTomorrow,
                        onClick = {
                            isTomorrow = false
                            val c = Calendar.getInstance()
                            targetDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.time)
                        },
                        label = { Text("আজকে (Today)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("বাতিল", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAdd(title, timeFormatted, targetDateStr, selectedHour, selectedMinute)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
                    ) {
                        Text("অ্যালার্ম সেট করুন", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun EditWorkoutItemDialog(
    workout: WinterArcWorkoutItem,
    onDismiss: () -> Unit,
    onSave: (WinterArcWorkoutItem) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(workout.name) }
    var setsText by remember { mutableStateOf(workout.sets.toString()) }
    var repsText by remember { mutableStateOf(workout.repsOrDuration) }
    var timeText by remember { mutableStateOf(workout.targetTime) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().glassCard(cornerSize = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("ব্যায়াম এডিট করুন", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text("সেট, র্যাপস ও অ্যালার্মের সময় পরিবর্তন করুন", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ব্যায়ামের নাম") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = setsText,
                        onValueChange = { setsText = it },
                        label = { Text("সেট সংখ্যা") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { repsText = it },
                        label = { Text("র্যাপস / সময়") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1.4f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("সময় (যেমন: 07:00 AM)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    val ampm = if (h >= 12) "PM" else "AM"
                                    val h12 = if (h % 12 == 0) 12 else h % 12
                                    timeText = String.format(Locale.US, "%02d:%02d %s", h12, m, ampm)
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                false
                            ).show()
                        }) {
                            Icon(Icons.Filled.AccessTime, contentDescription = null, tint = Color(0xFF00B4D8))
                        }
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("বাতিল", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val sets = setsText.toIntOrNull() ?: workout.sets
                            if (name.isNotBlank()) {
                                onSave(workout.copy(name = name, sets = sets, repsOrDuration = repsText, targetTime = timeText))
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
                    ) {
                        Text("সংরক্ষণ", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun EditDayTaskDialog(
    task: WinterArcDayTask,
    onDismiss: () -> Unit,
    onSave: (WinterArcDayTask, Int, Int) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(task.title) }
    var selectedHour by remember { mutableIntStateOf(6) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var timeFormatted by remember { mutableStateOf(task.scheduledTime) }
    var targetDateStr by remember { mutableStateOf(task.targetDate) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().glassCard(cornerSize = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("রুটিন টাস্ক এডিট করুন", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("কাজের বিবরণ") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                selectedHour = h
                                selectedMinute = m
                                val ampm = if (h >= 12) "PM" else "AM"
                                val h12 = if (h % 12 == 0) 12 else h % 12
                                timeFormatted = String.format(Locale.US, "%02d:%02d %s", h12, m, ampm)
                            },
                            selectedHour,
                            selectedMinute,
                            false
                        ).show()
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Filled.AccessTime, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("অ্যালার্ট সময়: $timeFormatted", fontWeight = FontWeight.Bold, color = Color(0xFF00B4D8), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("বাতিল", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(task.copy(title = title, scheduledTime = timeFormatted, targetDate = targetDateStr), selectedHour, selectedMinute)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
                    ) {
                        Text("সংরক্ষণ", color = Color.White)
                    }
                }
            }
        }
    }
}

// ------------------- DEDICATED WINTER ARC BOTTOM BAR -------------------

@Composable
fun WinterArcBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val isLightMode = GlassTheme.isLightMode.value
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val items = listOf(
        Triple(0, if (isBn) "চ্যালেঞ্জ" else "Challenges", Icons.Filled.FitnessCenter),
        Triple(1, if (isBn) "পানি পান" else "Water", Icons.Filled.WaterDrop),
        Triple(2, if (isBn) "রুটিন" else "Routines", Icons.Filled.Schedule),
        Triple(3, if (isBn) "রেকর্ড" else "Records", Icons.Filled.CalendarMonth)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerSize = 24.dp, glassAlpha = 0.96f),
        colors = CardDefaults.cardColors(
            containerColor = if (isLightMode) Color(0xFFF1F5F9).copy(alpha = 0.95f) else Color(0xFF0F172A).copy(alpha = 0.92f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (index, title, icon) ->
                val isSelected = (selectedTab == index)
                val primaryColor = if (index == 2) Color(0xFF00E5FF) else Color(0xFF00B4D8)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onTabSelected(index) }
                        .background(
                            if (isSelected) primaryColor.copy(alpha = 0.18f) else Color.Transparent
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isSelected) primaryColor else if (isLightMode) Color(0xFF64748B) else Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) primaryColor else if (isLightMode) Color(0xFF64748B) else Color(0xFF94A3B8),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

data class ArcDaysInfo(
    val currentDay: Int,
    val totalDays: Int,
    val durationLabel: String,
    val isBeforeOctober: Boolean,
    val daysUntilOctober: Int
)

fun calculateArcDays(timelineMode: String): ArcDaysInfo {
    val startCal = Calendar.getInstance().apply {
        set(Calendar.MONTH, Calendar.OCTOBER)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val totalDays = if (timelineMode == "150_days") 150 else 90
    val label = if (timelineMode == "150_days") "150 Days Arctic Arc (End of Feb)" else "90 Days Winter Arc (End of Dec)"

    val now = Calendar.getInstance()
    val diffMs = now.timeInMillis - startCal.timeInMillis

    val isBeforeOctober = diffMs < 0
    val daysUntilOctober = if (isBeforeOctober) {
        val remainingMs = startCal.timeInMillis - now.timeInMillis
        ((remainingMs / (1000 * 60 * 60 * 24)) + 1).toInt()
    } else 0

    val currentDay = if (isBeforeOctober) {
        1
    } else {
        ((diffMs / (1000 * 60 * 60 * 24)) + 1).toInt().coerceIn(1, totalDays)
    }

    return ArcDaysInfo(
        currentDay = currentDay,
        totalDays = totalDays,
        durationLabel = label,
        isBeforeOctober = isBeforeOctober,
        daysUntilOctober = daysUntilOctober
    )
}
