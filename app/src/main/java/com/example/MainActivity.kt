package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.isActive
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

val DeepBlue = Color(0xFF131D2D)
val CardBlue = Color(0xFF1E2A40)
val RedAlert = Color(0xFFE53E3E)
val LightRed = Color(0xFFFF8A8A)
val GreenText = Color(0xFF10B981)
val CyanAccent = Color(0xFF06B6D4)

data class Badge(
    val id: Int,
    val name: String,
    val days: Int,
    @DrawableRes val imgRes: Int,
    val descName: String
)

@Composable
fun BadgeImage(imageUrl: String, modifier: Modifier = Modifier, fallbackName: String = "Badge") {
    if (imageUrl.startsWith("data:image")) {
        val base64String = imageUrl.substringAfter("base64,")
        val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = fallbackName,
                modifier = modifier,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            Icon(Icons.Filled.EmojiEvents, contentDescription = fallbackName, modifier = modifier, tint = Color(0xFFFFD700))
        }
    } else if (imageUrl.isNotEmpty()) {
        coil.compose.AsyncImage(
            model = imageUrl,
            contentDescription = fallbackName,
            modifier = modifier,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    } else {
        Icon(Icons.Filled.EmojiEvents, contentDescription = fallbackName, modifier = modifier, tint = Color(0xFFFFD700))
    }
}

val Badges = listOf(
    Badge(0, "জোকার", 0, R.drawable.badge_0, "Clown"),
    Badge(1, "নতুন যোদ্ধা", 1, R.drawable.badge_1, "Noob"),
    Badge(2, "Novice", 3, R.drawable.badge_2, "Novice"),
    Badge(3, "Average", 7, R.drawable.badge_3, "Average"),
    Badge(4, "Advanced", 15, R.drawable.badge_4, "Advanced"),
    Badge(5, "Sigma", 30, R.drawable.badge_5, "Sigma"),
    Badge(6, "Chad", 45, R.drawable.badge_6, "Chad"),
    Badge(7, "Absolute Chad", 60, R.drawable.badge_7, "Absolute Chad"),
    Badge(8, "Giga Chad", 120, R.drawable.badge_8, "Giga Chad"),
    Badge(9, "Absolute Giga Chad", 365, R.drawable.badge_9, "Absolute Giga Chad")
)

class PurityTrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("purity_prefs", Context.MODE_PRIVATE)
    private val _startTime = MutableStateFlow(prefs.getLong("start_time", System.currentTimeMillis()))
    val startTime = _startTime.asStateFlow()

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())

    val durationMs = combine(_startTime, _currentTime) { start, current ->
        maxOf(0L, current - start)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0L)

    private val sessionHistoryDao = AppDatabase.getDatabase(application).sessionHistoryDao()
    
    val historyList = sessionHistoryDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bestRecord = sessionHistoryDao.getBestRecord()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            while (true) {
                _currentTime.value = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    private fun saveHistoryBeforeReset(endTime: Long, customBadges: List<CustomBadge>) {
        val start = _startTime.value
        val currentDurationMs = maxOf(0L, endTime - start)
        val durationDays = (currentDurationMs / (1000 * 60 * 60 * 24)).toInt()
        if (currentDurationMs > 60000) { // Only save if more than 1 minute to avoid spam
            val badgeName = customBadges.lastOrNull { durationDays >= it.requiredDays }?.name 
                ?: Badges.lastOrNull { durationDays >= it.days }?.descName 
                ?: "Warrior"
            val history = SessionHistory(
                title = badgeName,
                startTime = start,
                endTime = endTime,
                durationDays = durationDays,
                statusText = "$durationDays days"
            )
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                sessionHistoryDao.insertHistory(history)
            }
        }
    }

    fun resetTimer(customBadges: List<CustomBadge> = emptyList()) {
        val now = System.currentTimeMillis()
        saveHistoryBeforeReset(now, customBadges)
        _startTime.value = now
        prefs.edit().putLong("start_time", now).apply()
    }

    fun resetTimerToDate(year: Int, month: Int, dayOfMonth: Int, customBadges: List<CustomBadge> = emptyList()) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth, 0, 0, 0)
        val time = calendar.timeInMillis
        val now = System.currentTimeMillis()
        saveHistoryBeforeReset(now, customBadges)
        val finalTime = if (time > now) now else time
        _startTime.value = finalTime
        prefs.edit().putLong("start_time", finalTime).apply()
    }

    fun restoreStreak(newStartTime: Long) {
        _startTime.value = newStartTime
        prefs.edit().putLong("start_time", newStartTime).apply()
    }
}

class MainActivity : ComponentActivity() {

    private fun triggerGuardianAlert(context: Context, reason: String) {
        GuardianManager.triggerGuardianAlert(context, reason)
    }

    private val _vpnConfigureEvent = kotlinx.coroutines.flow.MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    private val _motivationEvent = kotlinx.coroutines.flow.MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        if (intent.action == "com.example.VPN_CONFIGURE") {
            triggerGuardianAlert(this, "attempted to open VPN configuration or disconnect the VPN Shield")
            _vpnConfigureEvent.tryEmit(true)
        } else if (intent.action == "com.example.MOTIVATION") {
            _motivationEvent.tryEmit(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == "com.example.VPN_CONFIGURE") {
            triggerGuardianAlert(this, "attempted to open VPN configuration or disconnect the VPN Shield")
            _vpnConfigureEvent.tryEmit(true)
        } else if (intent?.action == "com.example.MOTIVATION") {
            _motivationEvent.tryEmit(true)
        }
        try {
            val pref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
            val lang = pref.getString("language", "")
            if (!lang.isNullOrBlank() && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                val locale = java.util.Locale(lang)
                java.util.Locale.setDefault(locale)
                val config = android.content.res.Configuration(resources.configuration)
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)
            }
        } catch (e: Exception) {}
        
        enableEdgeToEdge()
        setContent {
            var showMeditationDialog by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                launch {
                    _vpnConfigureEvent.collect {
                        showMeditationDialog = true
                    }
                }
                launch {
                    _motivationEvent.collect {
                        showMeditationDialog = true
                    }
                }
            }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = DeepBlue,
                    surface = CardBlue,
                    primary = CyanAccent,
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                if (showMeditationDialog) {
                    androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBlue),
                            modifier = Modifier.padding(16.dp).fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = LightRed, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("একটু থামুন ভাই!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("শয়তান আপনাকে প্ররোচিত করছে। আপনি নিজের কাছে যে প্রতিজ্ঞা করেছিলেন, তা কি ভুলে গেছেন? এক ফোঁটা সাময়িক আনন্দ আপনার জীবনের দীর্ঘস্থায়ী শান্তি কেড়ে নিতে পারে।", color = Color.LightGray, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(modifier = Modifier.background(DeepBlue, RoundedCornerShape(8.dp)).padding(16.dp)) {
                                    Text("\"যে নিজেকে নিয়ন্ত্রণ করতে পারে, সে দুনিয়ার সবচেয়ে শক্তিশালী মানুষ।\" \n\nদয়া করে ভিপিএন বন্ধ না করে মাত্র ১৫ মিনিট মেডিটেশন করুন। ১৫ মিনিট পর আপনার এই বাজে চিন্তা আর থাকবে না! ইনশাআল্লাহ।", color = CyanAccent, textAlign = TextAlign.Center, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { showMeditationDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("আমি হার মানবো না (Back)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                AppScreen()
            }
        }
    }
}

@Composable
fun AppScreen() {
    var currentTab by remember { mutableStateOf("home") }
    Scaffold(
        bottomBar = { AppBottomNav(currentTab) { currentTab = it } },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                "home" -> HomeScreen()
                "history" -> HistoryScreen()
                "focus" -> FocusScreen()
                "ai" -> AiScreen()
                "community" -> CommunityScreen()
                "profile" -> ProfileScreen(onNavigateToStaticPage = { currentTab = it })
                "admin_badges" -> AdminBadgeScreen(onBack = { currentTab = "profile" })
            }
        }
    }
}

@Composable
fun AppBottomNav(currentTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF151F30),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text(androidx.compose.ui.res.stringResource(id = R.string.nav_home), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            selected = currentTab == "home",
            onClick = { onTabSelected("home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenText,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.History, contentDescription = "History") },
            label = { Text(androidx.compose.ui.res.stringResource(id = R.string.nav_history), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            selected = currentTab == "history",
            onClick = { onTabSelected("history") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenText,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.GpsFixed, contentDescription = "Focus") },
            label = { Text(androidx.compose.ui.res.stringResource(id = R.string.nav_focus), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            selected = currentTab == "focus",
            onClick = { onTabSelected("focus") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenText,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Spa, contentDescription = "AI") },
            label = { Text(androidx.compose.ui.res.stringResource(id = R.string.nav_ai), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            selected = currentTab == "ai",
            onClick = { onTabSelected("ai") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenText,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Group, contentDescription = "Community") },
            label = { Text(androidx.compose.ui.res.stringResource(id = R.string.nav_community), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            selected = currentTab == "community",
            onClick = { onTabSelected("community") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenText,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text(androidx.compose.ui.res.stringResource(id = R.string.nav_profile), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            selected = currentTab == "profile",
            onClick = { onTabSelected("profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenText,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color.Gray
            )
        )
    }
}

@Composable
fun HistoryScreen(viewModel: PurityTrackerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val bestRecord by viewModel.bestRecord.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val currentDurationDays = (durationMs / (1000 * 60 * 60 * 24)).toInt()

    val recordDays = maxOf(currentDurationDays, bestRecord?.durationDays ?: 0)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardBlue)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Best Record", color = Color.LightGray, fontSize = 12.sp)
                    Text("$recordDays days", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardBlue)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GreenText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Current Streak", color = Color.LightGray, fontSize = 12.sp)
                    Text("$currentDurationDays days", color = GreenText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Text("Tracked Purity Sessions (${historyList.size + 1})", color = Color.White, fontWeight = FontWeight.Bold)

        val startTime by viewModel.startTime.collectAsStateWithLifecycle()
        val calendar = Calendar.getInstance().apply { timeInMillis = startTime }
        val formatter = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
        val startStr = formatter.format(calendar.time)

        TrackedSessionItem("Current (Active)", "Start Time: $startStr", "$currentDurationDays days", Icons.Filled.Star, CyanAccent, CyanAccent)

        historyList.forEach { history ->
            val hCal = Calendar.getInstance().apply { timeInMillis = history.startTime }
            val hStartStr = formatter.format(hCal.time)
            TrackedSessionItem(history.title, "Start Time: $hStartStr", history.statusText, Icons.Filled.Star, Color.Gray, Color.White)
        }

        if (historyList.isEmpty()) {
            Text("Failure History (0)", color = Color.White, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBlue)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = GreenText, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No Relapses Recorded!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Your preservation history is perfectly clean.", color = Color.LightGray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun TrackedSessionItem(title: String, desc: String, duration: String, icon: ImageVector, iconTint: Color, durationColor: Color) {
    Card(
         modifier = Modifier.fillMaxWidth(),
         colors = CardDefaults.cardColors(containerColor = CardBlue)
    ) {
         Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
             Icon(icon, contentDescription = null, tint = iconTint)
             Spacer(modifier = Modifier.width(16.dp))
             Column(modifier = Modifier.weight(1f)) {
                 Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                 Text(desc, color = Color.LightGray, fontSize = 12.sp)
             }
             Text(duration, color = durationColor, fontWeight = FontWeight.Bold)
         }
    }
}

@Composable
fun FocusScreen() {
    var isPlaying by remember { mutableStateOf(false) }
    var exerciseState by remember { mutableStateOf("Inhale") }
    var secondsLeft by remember { mutableStateOf(4) }
    var totalSeconds by remember { mutableStateOf(0L) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                delay(1000)
                totalSeconds++
                secondsLeft--
                if (secondsLeft <= 0) {
                    when (exerciseState) {
                        "Inhale" -> {
                            exerciseState = "Hold"
                            secondsLeft = 4
                        }
                        "Hold" -> {
                            if (totalSeconds % 12L < 8L) { 
                                exerciseState = "Exhale" 
                                secondsLeft = 4 
                            } else { 
                                exerciseState = "Inhale" 
                                secondsLeft = 4 
                            }
                        }
                        "Exhale" -> {
                            exerciseState = "Hold "
                            secondsLeft = 4
                        }
                        "Hold " -> {
                            exerciseState = "Inhale"
                            secondsLeft = 4
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ফোকাস মোড", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Eliminate all distractions and awaken your inner strength.", color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.weight(1f))

        val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "breathing")
        val circleScale by transition.animateFloat(
            initialValue = if (exerciseState == "Inhale") 0.8f else 1.2f,
            targetValue = if (exerciseState == "Inhale") 1.2f else if (exerciseState == "Exhale") 0.8f else (if (exerciseState == "Hold") 1.2f else 0.8f),
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(4000, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Restart
            ),
            label = "scale"
        )
        
        val actualScale = if (isPlaying) circleScale else 1f

        Box(
            modifier = Modifier.size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = actualScale
                        scaleY = actualScale
                    }
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = CyanAccent.copy(alpha = 0.5f),
                    strokeWidth = 4.dp
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (isPlaying) exerciseState else "Ready", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                if (isPlaying) {
                    Text("$secondsLeft Seconds", color = Color.LightGray, fontSize = 18.sp)
                } else {
                    Text("Press Play to Start", color = Color.LightGray, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val mins = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60

        Text(String.format("%02d : %02d : %02d : %02d", days, hours, mins, secs), color = CyanAccent, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Text("Days : Hours : Mins : Secs", color = Color.LightGray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(32.dp))

        IconButton(
            onClick = { 
                if (isPlaying) {
                    isPlaying = false
                } else {
                    isPlaying = true
                    totalSeconds = 0L
                    exerciseState = "Inhale"
                    secondsLeft = 4
                }
            }, 
            modifier = Modifier.size(64.dp).background(if (isPlaying) RedAlert else GreenText, CircleShape)
        ) {
             Icon(
                 if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow, 
                 contentDescription = if (isPlaying) "Stop" else "Play", 
                 tint = DeepBlue, 
                 modifier = Modifier.size(32.dp)
             )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text("Close your eyes to resist urges, and recall your\nguiding inner safe light.", color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun AiScreen() {
    var isIslamicAi by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
            Card(
                colors = CardDefaults.cardColors(containerColor = if(isIslamicAi) CardBlue else DeepBlue),
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                modifier = Modifier.clickable { isIslamicAi = true }
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = GreenText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ইসলামিক এআই", color = Color.White)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = if(!isIslamicAi) CardBlue else DeepBlue),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                modifier = Modifier.clickable { isIslamicAi = false }
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Book, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("সালাহ ট্র্যাকার", color = Color.White)
                }
            }
        }

        if (isIslamicAi) {
            IslamicAiChat()
        } else {
            SalahTracker()
        }
    }
}

@Composable
fun IslamicAiChat(aiViewModel: AiViewModel = viewModel()) {
    val messages by aiViewModel.messages.collectAsState()
    val isLoading by aiViewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        androidx.compose.foundation.lazy.LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (msg.isUser) CyanAccent.copy(alpha=0.2f) else CardBlue),
                        shape = if (msg.isUser) RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp),
                        modifier = Modifier.padding(
                            start = if (msg.isUser) 32.dp else 0.dp,
                            end = if (msg.isUser) 0.dp else 32.dp
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(msg.text, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
            if (isLoading) {
                item {
                    Text("Thinking...", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask something...") },
                modifier = Modifier.weight(1f).background(CardBlue, RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBlue,
                    unfocusedContainerColor = CardBlue,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { 
                    aiViewModel.sendMessage(inputText)
                    inputText = "" 
                }, 
                modifier = Modifier.background(CyanAccent, CircleShape),
                enabled = !isLoading && inputText.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = DeepBlue)
            }
        }
    }
}

@Composable
fun SalahTracker(viewModel: SalahViewModel = viewModel()) {
    val todaySalahs by viewModel.todaySalahs.collectAsState()
    val past7DaysCounts by viewModel.past7DaysCounts.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Past 7 Days Salah Records", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier =Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                // days are 6 downTo 0
                for (i in 6 downTo 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(viewModel.getDayName(i), color = Color.LightGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        val isAll = past7DaysCounts[6 - i] == 5
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp).background(if(isAll) GreenText.copy(alpha=0.2f) else DeepBlue, CircleShape).border(1.dp, if(isAll) GreenText else Color.Gray, CircleShape)) {
                            Text(past7DaysCounts[6 - i].toString(), color = if(isAll) GreenText else Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Today's Five Waqt Salah Checklist", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        SalahItem("ফজর", "Pre-Dawn Prayer", todaySalahs[0]) { viewModel.toggleSalah(0) }
        SalahItem("যোহর (Dhuhr)", "Noon Prayer", todaySalahs[1]) { viewModel.toggleSalah(1) }
        SalahItem("আসর (Asr)", "Afternoon Prayer", todaySalahs[2]) { viewModel.toggleSalah(2) }
        SalahItem("মাগরিব (Maghrib)", "Sunset Prayer", todaySalahs[3]) { viewModel.toggleSalah(3) }
        SalahItem("এশা (Isha)", "Night Prayer", todaySalahs[4]) { viewModel.toggleSalah(4) }
    }
}

@Composable
fun SalahItem(title: String, desc: String, checked: Boolean, onClick: () -> Unit) {
     Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = CardBlue)) {
          Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
               Box(modifier = Modifier.size(24.dp).background(DeepBlue, CircleShape)) {
                   if (checked) {
                       Icon(Icons.Filled.Check, contentDescription = null, tint = GreenText, modifier = Modifier.size(16.dp).align(Alignment.Center))
                   }
               }
               Spacer(modifier = Modifier.width(16.dp))
               Column(modifier = Modifier.weight(1f)) {
                   Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                   Text(desc, color = Color.LightGray, fontSize = 12.sp)
               }
               Box(modifier = Modifier.size(24.dp).background(if(checked) GreenText.copy(alpha=0.3f) else Color.Transparent, RoundedCornerShape(4.dp)).border(2.dp, if(checked) GreenText else Color.Gray, RoundedCornerShape(4.dp))) {
                   if (checked) {
                       Icon(Icons.Filled.Check, contentDescription = null, tint = GreenText, modifier = Modifier.align(Alignment.Center))
                   }
               }
          }
     }
}

@Composable
fun CommunityScreen(communityViewModel: CommunityViewModel = viewModel(), authViewModel: AuthViewModel = viewModel()) {
    val posts by communityViewModel.posts.collectAsState()
    val leaders by communityViewModel.leaders.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    var postText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
         Row(verticalAlignment = Alignment.CenterVertically) {
             Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700))
             Spacer(modifier = Modifier.width(8.dp))
             Text("Global Leaderboard (Top Days)", color = Color.White, fontWeight = FontWeight.Bold)
         }
         Spacer(modifier = Modifier.height(16.dp))
         
         if (leaders.isEmpty()) {
             Text("No warriors found on leaderboard yet...", color = Color.LightGray)
         } else {
             leaders.forEachIndexed { index, leader ->
                 val days = TimeUnit.MILLISECONDS.toDays(leader.streakMs).toInt()
                 Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                     Text("${index + 1}.", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                     Text(leader.name, color = Color.White, modifier = Modifier.weight(1f))
                     
                     Spacer(modifier = Modifier.width(8.dp))
                     Text("${days} days streak", color = Color.White, fontSize = 12.sp, modifier = Modifier.width(100.dp), textAlign = TextAlign.End)
                 }
             }
         }

         Spacer(modifier = Modifier.height(24.dp))
         Text("Post your progress", color = Color.White, fontWeight = FontWeight.Bold)
         Spacer(modifier = Modifier.height(8.dp))
         Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
              Column(modifier = Modifier.padding(16.dp)) {
                  OutlinedTextField(
                      value = postText,
                      onValueChange = { postText = it },
                      modifier = Modifier.fillMaxWidth(),
                      placeholder = { Text("Write something inspiring to fuel other warriors...", color = Color.LightGray, fontSize = 14.sp) },
                      colors = OutlinedTextFieldDefaults.colors(
                          unfocusedBorderColor = Color.Gray,
                          focusedBorderColor = CyanAccent,
                          unfocusedTextColor = Color.White,
                          focusedTextColor = Color.White
                      )
                  )
                  Spacer(modifier = Modifier.height(16.dp))
                  Button(
                      onClick = { 
                          if (postText.isNotBlank()) {
                              communityViewModel.addPost(postText, "Active Streak: Unknown", "Novice")
                              postText = ""
                          }
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                      modifier = Modifier.fillMaxWidth()
                  ) {
                      Text("Publish Status", color = Color.White)
                  }
              }
         }

         Spacer(modifier = Modifier.height(24.dp))
         Text("Latest Motivational Board", color = Color.White, fontWeight = FontWeight.Bold)
         Spacer(modifier = Modifier.height(16.dp))
         
         if (posts.isEmpty()) {
             Text("Loading posts or no posts yet...", color = Color.LightGray)
         } else {
             posts.forEach { post ->
                 CommunityPost(
                     post = post,
                     currentUserId = currentUser?.uid ?: "",
                     communityViewModel = communityViewModel
                 )
             }
         }
    }
}

@Composable
fun CommunityPost(post: Post, currentUserId: String, communityViewModel: CommunityViewModel) {
    val context = LocalContext.current
    val hasLiked = post.likedBy.contains(currentUserId)
    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
        Column(modifier = Modifier.padding(16.dp)) {
             Row(verticalAlignment = Alignment.CenterVertically) {
                 Box(modifier = Modifier.size(40.dp).background(CyanAccent, CircleShape))
                 Spacer(modifier = Modifier.width(12.dp))
                 Column(modifier = Modifier.weight(1f)) {
                      Text(post.name, color = Color.White, fontWeight = FontWeight.Bold)
                      if (post.userEmail == "muhaiminulhasananas@gmail.com") {
                          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)) {
                              Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(18.dp))
                              Spacer(modifier = Modifier.width(4.dp))
                              Text("Admin & Developer", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                          }
                      }
                      Text(post.streak, color = Color.LightGray, fontSize = 12.sp)
                 }
                 Box(modifier = Modifier.background(DeepBlue, RoundedCornerShape(4.dp)).border(1.dp, CyanAccent, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                      Text(post.badge, color = CyanAccent, fontSize = 10.sp)
                 }
             }
             Spacer(modifier = Modifier.height(12.dp))
             Text(post.content, color = Color.White, fontSize = 14.sp)
             Spacer(modifier = Modifier.height(12.dp))
             Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier=Modifier.fillMaxWidth()) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Box(modifier = Modifier.background(if(hasLiked) Color(0xFFEF4444).copy(alpha=0.2f) else Color(0xFF4B5563), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp).clickable { communityViewModel.toggleLike(post.id, post.likedBy) }) {
                         Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Filled.Favorite, contentDescription = null, tint = if(hasLiked) Color(0xFFEF4444) else LightRed, modifier = Modifier.size(16.dp))
                             Spacer(modifier = Modifier.width(4.dp))
                             Text(post.claps.toString(), color = Color.White, fontSize = 12.sp)
                         }
                     }
                     Spacer(modifier = Modifier.width(12.dp))
                     Box(modifier = Modifier.background(Color(0xFF4B5563), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp).clickable {
                         showComments = !showComments
                         if (showComments) communityViewModel.fetchComments(post.id)
                     }) {
                         Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Filled.ChatBubble, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                             Spacer(modifier = Modifier.width(4.dp))
                             Text(post.commentsCount.toString(), color = Color.White, fontSize = 12.sp)
                         }
                     }
                 }
                 Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.LightGray, modifier = Modifier.clickable {
                     val sendIntent: android.content.Intent = android.content.Intent().apply {
                         action = android.content.Intent.ACTION_SEND
                         putExtra(android.content.Intent.EXTRA_TEXT, "${post.name} shared on Purity Tracker:\n\n${post.content}")
                         type = "text/plain"
                     }
                     val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                     context.startActivity(shareIntent)
                 })
             }
             
             if (showComments) {
                 Spacer(modifier = Modifier.height(16.dp))
                 Divider(color = Color.DarkGray)
                 Spacer(modifier = Modifier.height(8.dp))
                 
                 val commentsMap by communityViewModel.comments.collectAsState()
                 val postComments = commentsMap[post.id] ?: emptyList()
                 
                 if (postComments.isEmpty()) {
                     Text("No comments yet. Be the first!", color = Color.LightGray, fontSize = 12.sp)
                 } else {
                     postComments.forEach { comment ->
                         Column(modifier = Modifier.padding(bottom = 8.dp)) {
                             Text(comment.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                             if (comment.userEmail == "muhaiminulhasananas@gmail.com") {
                                 Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)) {
                                     Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(14.dp))
                                     Spacer(modifier = Modifier.width(4.dp))
                                     Text("Admin", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                 }
                             }
                             Spacer(modifier = Modifier.height(2.dp))
                             Text(comment.content, color = Color.LightGray, fontSize = 12.sp)
                         }
                     }
                 }
                 
                 Spacer(modifier = Modifier.height(8.dp))
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     OutlinedTextField(
                         value = commentText,
                         onValueChange = { commentText = it },
                         placeholder = { Text("Write a comment...", color = Color.Gray, fontSize = 12.sp) },
                         modifier = Modifier.weight(1f).height(50.dp),
                         colors = OutlinedTextFieldDefaults.colors(
                             unfocusedBorderColor = Color.DarkGray,
                             focusedBorderColor = CyanAccent,
                             unfocusedTextColor = Color.White,
                             focusedTextColor = Color.White
                         ),
                         singleLine = true
                     )
                     Spacer(modifier = Modifier.width(8.dp))
                     IconButton(onClick = {
                         if (commentText.isNotBlank()) {
                             communityViewModel.addComment(post.id, commentText)
                             commentText = ""
                         }
                     }, modifier = Modifier.background(CyanAccent, CircleShape).size(40.dp)) {
                         Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(18.dp))
                     }
                 }
             }
        }
    }
}

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    purityTrackerViewModel: PurityTrackerViewModel = viewModel(),
    communityViewModel: CommunityViewModel = viewModel(),
    onNavigateToStaticPage: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // Attempt to migrate cloud streak when logging in
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser!!.uid).get().addOnSuccessListener { doc ->
                val record = doc.toObject(LeaderboardUser::class.java)
                if (record != null && record.streakMs > 0) {
                    val elapsed = System.currentTimeMillis() - record.timestamp
                    val realStreak = record.streakMs + elapsed
                    val currentStreak = System.currentTimeMillis() - purityTrackerViewModel.startTime.value
                    if (realStreak > currentStreak) {
                        purityTrackerViewModel.restoreStreak(System.currentTimeMillis() - realStreak)
                        android.widget.Toast.makeText(context, "Cloud sync: Restored your previous streak!", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
         Text(androidx.compose.ui.res.stringResource(id = R.string.nav_profile), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
         Spacer(modifier = Modifier.height(24.dp))
         Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
              Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                   Icon(Icons.Filled.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                   Spacer(modifier = Modifier.height(16.dp))
                   
                   if (currentUser == null) {
                       var isSignUpMode by remember { mutableStateOf(false) }
                       var email by remember { mutableStateOf("") }
                       var password by remember { mutableStateOf("") }
                       var isLoading by remember { mutableStateOf(false) }

                       Text(if(isSignUpMode) "Create Account" else "Log In to Sync Progress", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                       Spacer(modifier = Modifier.height(16.dp))
                       androidx.compose.material3.OutlinedTextField(
                           value = email, 
                           onValueChange = { email = it }, 
                           label = { Text("Email", color = Color.LightGray) },
                           colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                               focusedTextColor = Color.White, 
                               unfocusedTextColor = Color.White,
                               focusedBorderColor = CyanAccent, 
                               unfocusedBorderColor = Color.Gray,
                               focusedLabelColor = CyanAccent,
                               unfocusedLabelColor = Color.LightGray
                           ),
                           modifier = Modifier.fillMaxWidth(),
                           singleLine = true
                       )
                       Spacer(modifier = Modifier.height(8.dp))
                       androidx.compose.material3.OutlinedTextField(
                           value = password, 
                           onValueChange = { password = it }, 
                           label = { Text("Password", color = Color.LightGray) },
                           colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                               focusedTextColor = Color.White, 
                               unfocusedTextColor = Color.White,
                               focusedBorderColor = CyanAccent, 
                               unfocusedBorderColor = Color.Gray,
                               focusedLabelColor = CyanAccent,
                               unfocusedLabelColor = Color.LightGray
                           ),
                           modifier = Modifier.fillMaxWidth(),
                           singleLine = true,
                           visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                       )
                       Spacer(modifier = Modifier.height(16.dp))
                       if (isLoading) {
                           androidx.compose.material3.CircularProgressIndicator(color = CyanAccent)
                       } else {
                           Button(
                               onClick = {
                                   if (email.isNotBlank() && password.length >= 6) {
                                       isLoading = true
                                       if (isSignUpMode) {
                                           authViewModel.signUpWithEmail(email, password, context) { success, msg ->
                                               isLoading = false
                                               if (!success) android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                           }
                                       } else {
                                           authViewModel.signInWithEmail(email, password, context) { success, msg ->
                                               isLoading = false
                                               if (!success) android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                           }
                                       }
                                   } else {
                                       android.widget.Toast.makeText(context, "Enter valid email and password (min 6 chars)", android.widget.Toast.LENGTH_SHORT).show()
                                   }
                               },
                               colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                               modifier = Modifier.fillMaxWidth()
                           ) {
                               Text(if(isSignUpMode) "Sign Up" else "Log In", color = Color.Black, fontWeight = FontWeight.Bold)
                           }
                       }
                       Spacer(modifier = Modifier.height(8.dp))
                       TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                           Text(if(isSignUpMode) "Already have an account? Log In" else "New here? Sign Up", color = CyanAccent)
                       }
                       
                       Spacer(modifier = Modifier.height(16.dp))
                       Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                           androidx.compose.foundation.layout.Box(modifier = Modifier.height(1.dp).weight(1f).background(Color.Gray))
                           Text(" OR ", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
                           androidx.compose.foundation.layout.Box(modifier = Modifier.height(1.dp).weight(1f).background(Color.Gray))
                       }
                       Spacer(modifier = Modifier.height(16.dp))
                       
                       Button(onClick = { authViewModel.signInWithGoogle(context) }, colors = ButtonDefaults.buttonColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                           Icon(painterResource(id = R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Unspecified)
                           Spacer(modifier = Modifier.width(8.dp))
                           Text("Log In with Google", color = Color.DarkGray, fontWeight = FontWeight.Bold)
                       }
                       
                       Spacer(modifier = Modifier.height(16.dp))
                       Text("Developed by Anas", color = Color.Gray, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                   } else {
                       Text("Welcome, ${currentUser?.displayName ?: "Warrior"}!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                       Spacer(modifier = Modifier.height(8.dp))
                       Text("Email: ${currentUser?.email}", color = Color.LightGray, fontSize = 14.sp)
                       Spacer(modifier = Modifier.height(24.dp))
                       Button(onClick = { authViewModel.signOut() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)), modifier = Modifier.fillMaxWidth()) {
                           Text("Log Out")
                       }
                       
                       // Check if user is admin based on email
                       if (currentUser?.email == "muhaiminulhasananas@gmail.com") {
                           Spacer(modifier = Modifier.height(12.dp))
                           Button(onClick = { onNavigateToStaticPage("admin_badges") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), modifier = Modifier.fillMaxWidth()) {
                               Icon(Icons.Filled.AdminPanelSettings, contentDescription = null)
                               Spacer(modifier = Modifier.width(8.dp))
                               Text("Admin: Manage Badges")
                           }
                       }
                   }
              }
         }

         Spacer(modifier = Modifier.height(24.dp))
         Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
             Column(modifier = Modifier.padding(16.dp)) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(Icons.Filled.Security, contentDescription = null, tint = CyanAccent)
                     Spacer(modifier = Modifier.width(8.dp))
                     Text("গার্ডিয়ান প্রটেকশন শিল্ড", color = Color.White, fontWeight = FontWeight.Bold)
                 }
                 Spacer(modifier = Modifier.height(12.dp))
                 val sharedPref = context.getSharedPreferences("GuardianPrefs", Context.MODE_PRIVATE)
                 var userName by remember { mutableStateOf(sharedPref.getString("userName", "") ?: "") }
                 var guardianName by remember { mutableStateOf(sharedPref.getString("guardianName", "") ?: "") }
                 var guardianEmail by remember { mutableStateOf(sharedPref.getString("guardianEmail", "") ?: "") }
                 var guardianPhone by remember { mutableStateOf(sharedPref.getString("guardianPhone", "") ?: "") }
                 var isShieldActive by remember { mutableStateOf(sharedPref.getBoolean("isShieldActive", false)) }

                 Row(verticalAlignment = Alignment.CenterVertically) {
                     androidx.compose.foundation.layout.Box(
                         modifier = Modifier
                             .background(Color.Red.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                             .padding(horizontal = 6.dp, vertical = 2.dp)
                     ) {
                         Text("Upcoming Feature", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                     }
                 }
                 Spacer(modifier = Modifier.height(4.dp))
                 Text("এখানে আপনার অভিভাবক, শিক্ষক বা কোনো সম্মানিত ব্যক্তির মেইল যোগ করুন। খুব শিগ্রই আমরা ফায়ারবেসের মাধ্যমে অটোমেটিক ইমেইল এলার্ট সিস্টেম চালু করব। আপাতত আপনার টার্গেট মেইল সেট করে রাখুন!", color = Color.LightGray, fontSize = 12.sp)

                 Spacer(modifier = Modifier.height(16.dp))
                 OutlinedTextField(value = userName, onValueChange = { userName = it; sharedPref.edit().putString("userName", it).apply() }, label = { Text("আপনার নাম (Your Name) *") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray, focusedBorderColor = CyanAccent, unfocusedTextColor = Color.White, focusedTextColor = Color.White, unfocusedLabelColor = Color.Gray, focusedLabelColor = CyanAccent))
                 Spacer(modifier = Modifier.height(12.dp))
                 OutlinedTextField(value = guardianName, onValueChange = { guardianName = it; sharedPref.edit().putString("guardianName", it).apply() }, label = { Text("গার্জিয়ান/শিক্ষকের নাম (Guardian Name) *") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray, focusedBorderColor = CyanAccent, unfocusedTextColor = Color.White, focusedTextColor = Color.White, unfocusedLabelColor = Color.Gray, focusedLabelColor = CyanAccent))
                 Spacer(modifier = Modifier.height(12.dp))
                 OutlinedTextField(value = guardianEmail, onValueChange = { guardianEmail = it; sharedPref.edit().putString("guardianEmail", it).apply() }, label = { Text("অভিভাবকের ইমেইল (Guardian Email) *") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray, focusedBorderColor = CyanAccent, unfocusedTextColor = Color.White, focusedTextColor = Color.White, unfocusedLabelColor = Color.Gray, focusedLabelColor = CyanAccent))
                 Spacer(modifier = Modifier.height(12.dp))
                 OutlinedTextField(value = guardianPhone, onValueChange = { guardianPhone = it; sharedPref.edit().putString("guardianPhone", it).apply() }, label = { Text("অভিভাবকের ফোন নম্বর - ঐচ্ছিক (Phone - Optional)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray, focusedBorderColor = CyanAccent, unfocusedTextColor = Color.White, focusedTextColor = Color.White, unfocusedLabelColor = Color.Gray, focusedLabelColor = CyanAccent))
                 Spacer(modifier = Modifier.height(16.dp))

                 Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                     Column {
                         Text("এলার্ট সিস্টেম চালু করুন", color = Color.White, fontWeight = FontWeight.Bold)
                         Text("সক্রিয় থাকলে চেষ্টা করা মাত্র ইমেইল পাঠানো হবে", color = Color.LightGray, fontSize = 12.sp)
                     }
                     Switch(
                         checked = isShieldActive,
                         onCheckedChange = {
                             if (it && (userName.isBlank() || guardianName.isBlank() || guardianEmail.isBlank())) {
                                 android.widget.Toast.makeText(context, "অনুগ্রহ করে সকল * চিহ্নিত তথ্য পূরণ করুন।", android.widget.Toast.LENGTH_SHORT).show()
                                 return@Switch
                             }
                             isShieldActive = it
                             sharedPref.edit().putBoolean("isShieldActive", it).apply()
                             android.widget.Toast.makeText(context, if (it) "গার্ডিয়ান প্রটেকশন শিল্ড সচল হয়েছে!" else "গার্ডিয়ান শিল্ড বন্ধ করা হয়েছে।", android.widget.Toast.LENGTH_SHORT).show()
                         },
                         colors = SwitchDefaults.colors(checkedThumbColor = DeepBlue, checkedTrackColor = CyanAccent)
                     )
                 }

                 Spacer(modifier = Modifier.height(16.dp))
                 Card(colors = CardDefaults.cardColors(containerColor = GreenText.copy(alpha = 0.1f)), border = BorderStroke(1.dp, GreenText)) {
                     Column(modifier = Modifier.padding(12.dp)) {
                         Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Filled.Info, contentDescription = null, tint = GreenText, modifier = Modifier.size(16.dp))
                             Spacer(modifier = Modifier.width(4.dp))
                             Text("আপকামিং ফায়ারবেস ইন্টিগ্রেশন:", color = GreenText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                         }
                         Spacer(modifier = Modifier.height(4.dp))
                         Text("আপাতত শুধু গার্জিয়ানের মেইল সেভ করে রাখুন। পরবর্তীতে এক ক্লিকেই ফায়ারবেস ট্রিগার ইমেইল এর মাধ্যমে সুরক্ষাকবচ সচল করা হবে। এটি একটি আপকামিং ফিচার।", color = GreenText, fontSize = 12.sp)
                     }
                 }
                 Spacer(modifier = Modifier.height(16.dp))
                 Button(onClick = {
                      if (userName.isBlank() || guardianName.isBlank() || guardianEmail.isBlank()) {
                          android.widget.Toast.makeText(context, "অনুগ্রহ করে সকল * চিহ্নিত তথ্য পূরণ করুন।", android.widget.Toast.LENGTH_SHORT).show()
                      } else {
                          sharedPref.edit().apply {
                              putString("userName", userName)
                              putString("guardianName", guardianName)
                              putString("guardianEmail", guardianEmail)
                              putString("guardianPhone", guardianPhone)
                              putBoolean("isShieldActive", isShieldActive)
                          }.apply()
                          android.widget.Toast.makeText(context, "গার্জিয়ান স্ট্যাটাস সফলভাবে সেভ করা হয়েছে!", android.widget.Toast.LENGTH_SHORT).show()
                      }
                  }, colors = ButtonDefaults.buttonColors(containerColor = CyanAccent), modifier = Modifier.fillMaxWidth()) {
                     Text("গার্জিয়ান স্ট্যাটাস সেভ করুন", color = DeepBlue)
                  }
             }
         }

         Spacer(modifier = Modifier.height(24.dp))
         var expanded by remember { mutableStateOf(false) }
         Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().clickable { expanded = true }.padding(vertical = 8.dp)) {
             Row(verticalAlignment = Alignment.CenterVertically) {
                 Icon(Icons.Filled.Language, contentDescription = null, tint = Color.LightGray)
                 Spacer(modifier = Modifier.width(8.dp))
                 Text("ভাষা পরিবর্তন (Change Language)", color = Color.White)
             }
             Box {
                 Text("Select >", color = CyanAccent)
                 DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                     val languages = listOf("en" to "English", "bn" to "বাংলা", "hi" to "हिंदी", "es" to "Español", "ar" to "العربية", "ur" to "اردو")
                     languages.forEach { (tag, name) ->
                         DropdownMenuItem(text = { Text(name) }, onClick = {
                             changeAppLocale(context, tag)
                             expanded = false
                         })
                     }
                 }
             }
         }
    }
}

fun changeAppLocale(context: Context, localeTag: String) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
        localeManager?.applicationLocales = android.os.LocaleList.forLanguageTags(localeTag)
    } else {
        val locale = java.util.Locale(localeTag)
        java.util.Locale.setDefault(locale)
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        context.getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE).edit().putString("language", localeTag).apply()
        if (context is android.app.Activity) {
            context.recreate()
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: PurityTrackerViewModel = viewModel(),
    badgesViewModel: BadgesViewModel = viewModel(),
    communityViewModel: CommunityViewModel = viewModel()
) {
    val durationMs by viewModel.durationMs.collectAsState()
    val dynamicBadges by badgesViewModel.badges.collectAsState()
    
    // Sync streak when it changes periodically (every time recomposed with new days is too frequent, so use a simple LaunchedEffect on Days change)
    val days = TimeUnit.MILLISECONDS.toDays(durationMs).toInt()
    LaunchedEffect(days) {
        communityViewModel.syncUserStreak(durationMs)
    }
    
    var showBadgesMenu by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val hours = TimeUnit.MILLISECONDS.toHours(durationMs) % 24
    val mins = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
    val secs = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60

    val currentBadge = dynamicBadges.lastOrNull { days >= it.requiredDays } ?: dynamicBadges.firstOrNull() ?: CustomBadge("", "None", 0, "None")
    val nextBadge = dynamicBadges.firstOrNull { days < it.requiredDays }

    val nextDaysReq = nextBadge?.requiredDays ?: 365
    val currDaysReq = currentBadge.requiredDays
    val range = (nextDaysReq - currDaysReq).toFloat()
    val progress = if (range > 0 && nextBadge != null) {
        (days - currDaysReq) / range
    } else 1f

    if (showBadgesMenu) {
        BadgesScreen(days, badgesViewModel, onClose = { showBadgesMenu = false })
        return
    }

    if (showResetDialog) {
        ResetDialog(
            onDismiss = { showResetDialog = false },
            onOldDate = {
                showResetDialog = false
                showDatePicker = true
            },
            onRightNow = {
                showResetDialog = false
                viewModel.resetTimer(dynamicBadges)
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialogMock(
            onDismiss = { showDatePicker = false },
            onDateSelected = { y, m, d ->
                viewModel.resetTimerToDate(y, m, d, dynamicBadges)
                showDatePicker = false
            }
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text("স্বাগতম, যোদ্ধা", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text("আপনার চারিত্রিক সঙ্কল্প অটুট রাখুন।", fontSize = 14.sp, color = Color.LightGray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Warning Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = RedAlert.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, RedAlert),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = LightRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("নিরাপত্তা সতর্কতা! অরক্ষিত ডিভাইস।", fontWeight = FontWeight.Bold, color = LightRed)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "১৮+ কন্টেন্ট ব্লক নিশ্চিত করতে নিচের দিকে স্ক্রোল করে VPN Shield এবং Settings Guard সচল করুন!",
                    fontSize = 12.sp, color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Timer Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBlue),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("বর্তমান ব্যাজ", color = CyanAccent, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.05f,
                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        )
                    )
                    
                    val glowAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 0.8f,
                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        )
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(scale)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(CyanAccent.copy(alpha = glowAlpha), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )
                    
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize().scale(scale),
                        color = CyanAccent,
                        trackColor = Color(0xFF374151),
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(144.dp)
                            .scale(scale)
                            .background(CyanAccent.copy(alpha=0.1f), CircleShape)
                            .border(4.dp, Color(0xFF2D3748), CircleShape)
                            .clip(CircleShape)
                    ) {
                        BadgeImage(
                            imageUrl = currentBadge.imageUrl,
                            modifier = Modifier.fillMaxSize(),
                            fallbackName = currentBadge.name
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(currentBadge.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(100.dp),
                        color = Color.White,
                        trackColor = Color(0xFF374151),
                        strokeWidth = 4.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(String.format("%02d", days), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("দিন (DAYS)", fontSize = 10.sp, color = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TimeBox(String.format("%02d", hours), "ঘন্টা (HOURS)")
                    TimeBox(String.format("%02d", mins), "মিনিট (MINS)")
                    TimeBox(String.format("%02d", secs), "সেকেন্ড (SECS)")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = RedAlert),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Menu, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("টাইমার থামান")
                    }
                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("রিসেট")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GreenText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("পরবর্তী: ${nextBadge?.name ?: "Max"} (আর মাত্র ${nextDaysReq - days} দিন বাকি)", color = Color.LightGray, fontSize = 12.sp)
                    }
                    TextButton(onClick = { showBadgesMenu = true }, contentPadding = PaddingValues(0.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("সমস্ত ব্যাজ >", color = CyanAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DNS Card
        var dnsEnabled by remember { mutableStateOf(false) }
        var showDnsInstructions by remember { mutableStateOf(false) }
        val context = LocalContext.current

        if (showDnsInstructions) {
            AlertDialog(
                onDismissRequest = { showDnsInstructions = false },
                title = { Text("কীভাবে DNS সেট করবেন?", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column {
                        Text("অ্যাডাল্ট সাইট ব্লক করার জন্য আপনাকে ফোনের সেটিংসে 'Private DNS' পরিবর্তন করতে হবে।", color = Color.White)
                        Spacer(modifier=Modifier.height(8.dp))
                        Text("১. আমরা 'family.adguard-dns.com' লেখাটি কপি করেছি।", color = Color.LightGray)
                        Text("২. 'সেটিংসে যান' বাটনে ক্লিক করলে আপনার ফোনের সেটিংস খুলবে।", color = Color.LightGray)
                        Text("৩. সেখানে 'Private DNS' বা 'প্রাইভেট ডিএনএস' লিখে সার্চ দিন।", color = Color.LightGray)
                        Text("৪. 'Private DNS provider hostname' সিলেক্ট করে কপি করা লেখাটি পেস্ট করে সেভ করুন।", color = Color.LightGray)
                        Spacer(modifier=Modifier.height(8.dp))
                        Text("বিকল্প সমাধান: আপনি যদি DNS ব্যবহার করতে না চান বা কাজ না করে, তবে আপনি গুগল প্লে-স্টোর থেকে 'BlockerX', 'Bulldog' বা 'OpenDNS' রাউটার সেটিংস ব্যবহার করতে পারেন।", color = Color.LightGray, fontSize = 12.sp)
                    }
                },
                containerColor = CardBlue,
                confirmButton = {
                    Button(onClick = {
                        showDnsInstructions = false
                        dnsEnabled = true
                        try {
                            context.startActivity(android.content.Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) })
                        } catch (e: Exception) {
                            try {
                                context.startActivity(android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) })
                            } catch (e2: Exception) { }
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)) {
                        Text("সেটিংসে যান", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDnsInstructions = false }) {
                        Text("বাদ দিন", color = Color.LightGray)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pin Lock Setting
        var hasPin by remember { mutableStateOf(LockManager.hasPin(context)) }
        var showPinDialog by remember { mutableStateOf(false) }
        var tempPin by remember { mutableStateOf("") }

        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = { Text(if(hasPin) "Change PIN" else "Set Security PIN", color = Color.White) },
                text = {
                    Column {
                        if (!hasPin) {
                            Text("উপদেশ: আপনার কোনো বিশ্বস্ত বন্ধু বা পরিবারের কাউকে দিয়ে এই পিনটি সেট করান, যাতে আপনার নিজের পিনটি অজানা থাকে।", color = LightRed, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        OutlinedTextField(
                            value = tempPin,
                            onValueChange = { tempPin = it },
                        label = { Text("Enter 4-digit PIN") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor=Color.White, unfocusedTextColor = Color.White)
                    )
                    }
                },
                containerColor = CardBlue,
                confirmButton = {
                    Button(onClick = {
                        if (tempPin.length >= 4) {
                            LockManager.setPin(context, tempPin)
                            hasPin = true
                            showPinDialog = false
                            android.widget.Toast.makeText(context, "PIN Saved successfully", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "PIN too short", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)) {
                        Text("Save PIN")
                    }
                },
                   dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) { Text("Cancel", color = Color.Gray) }
                }
            )
        }

        SettingCard(
            title = "Security PIN",
            desc = if (hasPin) "PIN is set. You will need it to disable VPN or change settings." else "No PIN set. Anyone can turn off protection.",
            buttonText = if (hasPin) "PIN Set (Locked)" else "Set PIN",
            isOn = hasPin,
            isWarning = !hasPin,
            icon = Icons.Filled.Lock,
            onClick = {
                if (!hasPin) {
                    tempPin = ""
                    showPinDialog = true
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Check if VPN is running/started natively.
        val vpnPrefs = context.getSharedPreferences("VpnPrefs", Context.MODE_PRIVATE)
        var vpnServiceRunning by remember { mutableStateOf(LocalDnsVpnService.isRunning) }
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    val preferRunning = vpnPrefs.getBoolean("vpn_is_running", false)
                    if (preferRunning) {
                        if (android.net.VpnService.prepare(context) == null) {
                            // We have permission. If not running, start it.
                            if (!LocalDnsVpnService.isRunning) {
                                try {
                                    context.startService(Intent(context, LocalDnsVpnService::class.java).apply { action = LocalDnsVpnService.ACTION_START })
                                } catch(e:Exception){}
                            }
                        } else {
                            // Permission revoked or given to another app!
                            vpnPrefs.edit().putBoolean("vpn_is_running", false).apply()
                        }
                    }
                    // small delay to let service update its static var
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        vpnServiceRunning = LocalDnsVpnService.isRunning
                    }, 200)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        var vpnLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                context.startService(Intent(context, LocalDnsVpnService::class.java).apply { action = LocalDnsVpnService.ACTION_START })
                vpnPrefs.edit().putBoolean("vpn_is_running", true).apply()
                vpnServiceRunning = true
                android.widget.Toast.makeText(context, "VPN Shield Activated", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        SettingCard(
            title = "Built-in VPN Shield (AdGuard DNS)",
            desc = "Routes traffic locally to filter adult content. Does not require external apps or complex settings.",
            buttonText = if (vpnServiceRunning) "VPN কানেক্টেড" else "VPN শিল্ড চালু করুন",
            isOn = vpnServiceRunning,
            isWarning = !vpnServiceRunning,
            onClick = {
                if (!hasPin) {
                    android.widget.Toast.makeText(context, "দয়া করে আগে Security PIN সেট করুন।", android.widget.Toast.LENGTH_SHORT).show()
                    return@SettingCard
                }
                if (!vpnServiceRunning) {
                    val intent = android.net.VpnService.prepare(context)
                    if (intent != null) {
                        try {
                            vpnLauncher.launch(intent)
                        } catch(e: Exception){}
                    } else {
                        context.startService(Intent(context, LocalDnsVpnService::class.java).apply { action = LocalDnsVpnService.ACTION_START })
                        vpnPrefs.edit().putBoolean("vpn_is_running", true).apply()
                        vpnServiceRunning = true
                        android.widget.Toast.makeText(context, "VPN Shield Activated", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Settings Guard Card
        var isAccessibilityEnabled by remember { mutableStateOf(false) }

        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
                    isAccessibilityEnabled = am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC).any {
                        it.resolveInfo.serviceInfo.packageName == context.packageName && it.resolveInfo.serviceInfo.name == "com.example.ProtectionService"
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
        
        SettingCard(
            title = "Settings Guard (Anti-Uninstall)",
            desc = if (isAccessibilityEnabled) "Guard is active. Your settings and app uninstallation are protected." else "আপনার কঠোর পরিশ্রমের স্ট্রিকটি যে কোনো সময় ডিলিট বা আনইনস্টল হয়ে যেতে পারে! এখনই সেটিংস গার্ড সচল করুন।",
            buttonText = if (isAccessibilityEnabled) "সচল রয়েছে" else "সেটিংস গার্ড চালু করুন",
            isOn = isAccessibilityEnabled,
            isWarning = !isAccessibilityEnabled,
            icon = Icons.Filled.Lock,
            onClick = {
                if (!vpnServiceRunning) {
                    android.widget.Toast.makeText(context, "দয়া করে আগে VPN Shield চালু করুন।", android.widget.Toast.LENGTH_SHORT).show()
                    return@SettingCard
                }
                if (!isAccessibilityEnabled) {
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                        android.widget.Toast.makeText(context, "Find 'Purity Tracker' and enable it.", android.widget.Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                        context.startActivity(intent)
                    }
                }
            }
        )
    }
}

@Composable
fun TimeBox(valStr: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = valStr,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = Color.LightGray)
    }
}

@Composable
fun SettingCard(
    title: String, desc: String, buttonText: String, isOn: Boolean, isWarning: Boolean, 
    icon: ImageVector = Icons.Filled.PlayArrow,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBlue),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = RedAlert)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .background(if (isOn) GreenText else RedAlert, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(if (isOn) "চালু" else "বন্ধ", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (title == "Settings Guard (Anti-Uninstall)") {
               Text("অরক্ষিত - বন্ধ আছে", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(start = 32.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(desc, color = Color.LightGray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onClick?.invoke() },
                colors = ButtonDefaults.buttonColors(containerColor = if (isOn) GreenText else RedAlert),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(currentDays: Int, badgesViewModel: BadgesViewModel, onClose: () -> Unit) {
    val dynamicBadges by badgesViewModel.badges.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("সকল মাইলস্টোন ও ব্যাজ", fontWeight = FontWeight.Bold)
                        Text("আত্ম-নিয়ন্ত্রণের উন্নতি", fontSize = 12.sp, color = Color.LightGray)
                    }
                },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = RedAlert)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlue, titleContentColor = Color.White)
            )
        },
        containerColor = DeepBlue,
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("ফিরে যান", color = Color.White)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dynamicBadges) { badge ->
                val isUnlocked = currentDays >= badge.requiredDays
                val isActive = dynamicBadges.lastOrNull { currentDays >= it.requiredDays }?.id == badge.id
                BadgeCardItemDynamic(badge, isUnlocked, isActive, currentDays)
            }
        }
    }
}

@Composable
fun BadgeCardItemDynamic(badge: CustomBadge, isUnlocked: Boolean, isActive: Boolean, currentDays: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBlue),
        shape = RoundedCornerShape(12.dp),
        border = if (isActive) BorderStroke(2.dp, CyanAccent) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(if (isUnlocked) CyanAccent.copy(alpha=0.1f) else Color.DarkGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (badge.imageUrl.isNotEmpty() && isUnlocked) {
                    BadgeImage(badge.imageUrl, modifier = Modifier.size(40.dp).clip(CircleShape), fallbackName = badge.name)
                } else {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = if (isUnlocked) Color(0xFFFFD700) else Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(badge.name, color = if (isUnlocked) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(badge.description, color = Color.LightGray, fontSize = 12.sp)
                if (!isUnlocked) {
                    val remaining = badge.requiredDays - currentDays
                    Text("আর $remaining দিন বাকি", color = LightRed, fontSize = 12.sp)
                }
            }
            if (isUnlocked) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Unlocked", tint = GreenText)
            } else {
                Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun BadgeCardItem(badge: Badge, isUnlocked: Boolean, isActive: Boolean, currentDays: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBlue),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = badge.imgRes),
                contentDescription = null,
                modifier = Modifier.size(64.dp).clip(CircleShape).background(if (!isUnlocked) Color.Gray else Color.Transparent)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(badge.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isUnlocked) Color.White else Color.Gray)
                }
                Text("${badge.days} দিন প্রয়োজনীয়", color = if (isUnlocked) GreenText else Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                if (!isUnlocked) {
                    Text("আনলক করতে আর মাত্র ${badge.days - currentDays} দিন বাকি!", color = CyanAccent, fontSize = 10.sp)
                } else {
                    Text("Keep your streak going to fully unlock the power of this ${badge.descName} milestone status.", color = Color.LightGray, fontSize = 10.sp, lineHeight = 14.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            val statusColor = if (isActive) CyanAccent else if (isUnlocked) GreenText else Color.Gray
            val statusText = if (isActive) "ACTIVE" else if (isUnlocked) "UNLOCKED" else "LOCKED"
            Box(
                modifier = Modifier.border(1.dp, statusColor, RoundedCornerShape(4.dp)).background(statusColor.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                 Text(statusText, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ResetDialog(onDismiss: () -> Unit, onOldDate: () -> Unit, onRightNow: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = RedAlert, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("কোথা থেকে শুরু করবেন?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "আপনি কি একদম এখন থেকে গণনা শুরু করতে চান, নাকি পূর্বে শুরু করা কোনো তারিখ লিস্ট করতে চান?",
                    textAlign = TextAlign.Center, color = Color.LightGray, fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onOldDate,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenText),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("পূর্বের তারিখ নির্বাচন করুন (Choose Old Date)")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRightNow,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenText),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("এখন থেকে (Right Now)")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogMock(onDismiss: () -> Unit, onDateSelected: (Int, Int, Int) -> Unit) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val cal = Calendar.getInstance()
                datePickerState.selectedDateMillis?.let { cal.timeInMillis = it }
                onDateSelected(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            }) {
                Text("পরবর্তী (সময়)", color = CyanAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল", color = Color.LightGray) }
        },
        colors = DatePickerDefaults.colors(containerColor = CardBlue)
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = CardBlue,
                titleContentColor = Color.White,
                headlineContentColor = Color.White,
                weekdayContentColor = Color.LightGray,
                dayContentColor = Color.White,
                selectedDayContainerColor = CyanAccent,
                todayDateBorderColor = CyanAccent,
                todayContentColor = CyanAccent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBadgeScreen(onBack: () -> Unit, badgesViewModel: BadgesViewModel = viewModel()) {
    val badges by badgesViewModel.badges.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editBadge: CustomBadge? by remember { mutableStateOf(null) }

    var badgeName by remember { mutableStateOf("") }
    var badgeDays by remember { mutableStateOf("") }
    var badgeImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        badgeImageUri = uri
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editBadge != null) "Edit Badge" else "New Badge") },
            text = {
                Column {
                    OutlinedTextField(
                        value = badgeName,
                        onValueChange = { badgeName = it },
                        label = { Text("Badge Name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = badgeDays,
                        onValueChange = { badgeDays = it },
                        label = { Text("Required Days") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Pick Image")
                    }
                    if (badgeImageUri != null) {
                        coil.compose.AsyncImage(
                            model = badgeImageUri,
                            contentDescription = "Selected image",
                            modifier = Modifier.size(64.dp)
                        )
                    } else {
                        BadgeImage(
                            imageUrl = editBadge?.imageUrl ?: "",
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            },
            confirmButton = {
                val context = LocalContext.current
                Button(onClick = {
                    val days = badgeDays.toIntOrNull() ?: 0
                    if (editBadge != null) {
                        badgesViewModel.updateBadge(context, editBadge!!.id, badgeName, days, badgeName, badgeImageUri, editBadge?.imageUrl ?: "")
                    } else {
                        badgesViewModel.addBadge(context, badgeName, days, badgeName, badgeImageUri)
                    }
                    showDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Badges", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlue)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editBadge = null
                badgeName = ""
                badgeDays = ""
                badgeImageUri = null
                showDialog = true
            }, containerColor = CyanAccent) {
                Icon(Icons.Filled.Add, contentDescription = "Add Badge")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding, modifier = Modifier.padding(16.dp)) {
            items(badges) { badge ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        BadgeImage(
                            imageUrl = badge.imageUrl,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            fallbackName = badge.name
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(badge.name, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Requires: ${badge.requiredDays} days", color = Color.LightGray, fontSize = 12.sp)
                        }
                        IconButton(onClick = {
                            editBadge = badge
                            badgeName = badge.name
                            badgeDays = badge.requiredDays.toString()
                            badgeImageUri = null
                            showDialog = true
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.Cyan)
                        }
                        IconButton(onClick = { badgesViewModel.deleteBadge(badge.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
