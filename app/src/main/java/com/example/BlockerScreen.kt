package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.theme.glassCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Purity Tracker v1.3.0 — Simple Blocker Page (BlockerHero-style), no DNS, no VPN.
 *
 * Exact top-to-bottom order required:
 * 1. Accountability Partner email (top)
 * 2. PIN ("Set PIN" — reuses existing LockManager PIN + Guardian Recovery Key reset)
 * 3. Block adult content switch (OFF by default on first install; turning OFF requires partner email approval)
 * 4. Accessibility ("Turn ON Accessibility" + inline Keep-alive: Battery unrestricted + OEM auto-start + live status row)
 * 5. Device Admin ("Activate Device Admin")
 */
@Composable
fun BlockerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF334155) else Color(0xFFCBD5E1)

    BackHandler {
        onBack()
    }

    // 1. Partner Email state (locked once saved; changing requires current-partner approval)
    var savedPartnerEmail by remember { mutableStateOf(BlockerManager.getPartnerEmail(context)) }
    var partnerEmailInput by remember { mutableStateOf("") }
    var partnerEmailWarning by remember { mutableStateOf<String?>(null) }
    var pendingPartnerChangeId by remember { mutableStateOf(BlockerManager.getPendingPartnerChangeRequestId(context)) }
    var showChangePartnerDialog by remember { mutableStateOf(false) }
    var isSendingPartnerChangeRequest by remember { mutableStateOf(false) }
    var partnerChangeFeedbackBanner by remember { mutableStateOf<String?>(null) }

    // 2. PIN state (reusing existing LockManager)
    var hasPin by remember { mutableStateOf(LockManager.hasPin(context)) }
    var showSetPinModal by remember { mutableStateOf(false) }

    // 3. Adult Blocker Switch state
    var isBlockerEnabled by remember { mutableStateOf(BlockerManager.isAdultBlockerEnabled(context)) }
    var pendingRequestId by remember { mutableStateOf(BlockerManager.getPendingRequestId(context)) }
    var showTurnOffDialog by remember { mutableStateOf(false) }
    var isSendingDisableRequest by remember { mutableStateOf(false) }
    var requestFeedbackBanner by remember { mutableStateOf<String?>(null) }
    var pendingTurnOnAfterAccessibility by remember { mutableStateOf(false) }

    // 4 & 6. Accessibility + Keep-alive live system states
    var isAccessibilityAlive by remember { mutableStateOf(false) }
    var isAccessibilityDeadButEnabled by remember { mutableStateOf(false) }
    var isBatteryUnrestricted by remember { mutableStateOf(false) }
    var isOemAutoStartOn by remember { mutableStateOf(false) }

    // 5. Device Admin live system state
    var isDeviceAdminActive by remember { mutableStateOf(false) }

    suspend fun refreshSystemStates() {
        val states = withContext(Dispatchers.IO) {
            val pin = LockManager.hasPin(context)
            val partner = BlockerManager.getPartnerEmail(context)
            val pendingPartnerChange = BlockerManager.getPendingPartnerChangeRequestId(context)
            val blocker = BlockerManager.isAdultBlockerEnabled(context)
            val pendingReq = BlockerManager.getPendingRequestId(context)
            val accAlive = AntiUninstallManager.isAccessibilityServiceAlive(context)
            val accDead = AntiUninstallManager.isAccessibilityDeadButEnabled(context)
            val battery = AntiUninstallManager.isBatteryOptimizationIgnored(context)
            val autoStart = AntiUninstallManager.isOemAutoStartVisited(context)
            val admin = AntiUninstallManager.isDeviceAdminActive(context)
            GuardHealthWorker.checkNowAndNotify(context)
            listOf(pin, partner, pendingPartnerChange, blocker, pendingReq, accAlive, accDead, battery, autoStart, admin)
        }
        hasPin = states[0] as Boolean
        savedPartnerEmail = states[1] as String
        pendingPartnerChangeId = states[2] as String?
        isBlockerEnabled = states[3] as Boolean
        pendingRequestId = states[4] as String?
        val nowAccAlive = states[5] as Boolean
        isAccessibilityAlive = nowAccAlive
        isAccessibilityDeadButEnabled = states[6] as Boolean
        isBatteryUnrestricted = states[7] as Boolean
        isOemAutoStartOn = states[8] as Boolean
        isDeviceAdminActive = states[9] as Boolean

        // If user tapped Switch ON while Accessibility was off and just returned after enabling Accessibility
        if (pendingTurnOnAfterAccessibility && nowAccAlive && hasPin && BlockerManager.hasValidPartnerEmail(context)) {
            pendingTurnOnAfterAccessibility = false
            BlockerManager.enableAdultBlocker(context)
            isBlockerEnabled = true
            Toast.makeText(context, "Block adult content is now ON", Toast.LENGTH_SHORT).show()
        }
    }

    // Continuous live poll for real system states
    LaunchedEffect(Unit) {
        while (true) {
            refreshSystemStates()
            BlockerManager.ensurePartnerApprovalListener(context) { status ->
                if (status == "approved") {
                    isBlockerEnabled = false
                    pendingRequestId = null
                    requestFeedbackBanner = "Your accountability partner approved turning OFF the blocker."
                    Toast.makeText(context, "Partner approved: Block adult content turned OFF", Toast.LENGTH_LONG).show()
                } else if (status == "denied") {
                    pendingRequestId = null
                    requestFeedbackBanner = "Your partner denied the request. Blocker remains ON."
                }
            }
            BlockerManager.ensurePartnerChangeApprovalListener(context) { status ->
                if (status == "approved") {
                    savedPartnerEmail = ""
                    partnerEmailInput = ""
                    pendingPartnerChangeId = null
                    partnerChangeFeedbackBanner = "Current partner approved the change. You may now enter a new partner email."
                    Toast.makeText(context, "Partner approved: You can now enter a new partner email", Toast.LENGTH_LONG).show()
                } else if (status == "denied") {
                    pendingPartnerChangeId = null
                    partnerChangeFeedbackBanner = "Your current partner denied the change request. Partner email remains locked."
                }
            }
            delay(1200L)
        }
    }

    // Also refresh immediately on ON_RESUME when returning from system Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPin = LockManager.hasPin(context)
                savedPartnerEmail = BlockerManager.getPartnerEmail(context)
                pendingPartnerChangeId = BlockerManager.getPendingPartnerChangeRequestId(context)
                isBlockerEnabled = BlockerManager.isAdultBlockerEnabled(context)
                pendingRequestId = BlockerManager.getPendingRequestId(context)
                isAccessibilityAlive = AntiUninstallManager.isAccessibilityServiceAlive(context)
                isAccessibilityDeadButEnabled = AntiUninstallManager.isAccessibilityDeadButEnabled(context)
                isBatteryUnrestricted = AntiUninstallManager.isBatteryOptimizationIgnored(context)
                isOemAutoStartOn = AntiUninstallManager.isOemAutoStartVisited(context)
                isDeviceAdminActive = AntiUninstallManager.isDeviceAdminActive(context)

                if (pendingTurnOnAfterAccessibility && isAccessibilityAlive && hasPin && BlockerManager.hasValidPartnerEmail(context)) {
                    pendingTurnOnAfterAccessibility = false
                    BlockerManager.enableAdultBlocker(context)
                    isBlockerEnabled = true
                    Toast.makeText(context, "Block adult content is now ON", Toast.LENGTH_SHORT).show()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                    .testTag("blocker_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = titleColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Blocker",
                    color = titleColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Simple 4-step protection • Accountability Partner guarded",
                    color = descColor,
                    fontSize = 12.sp
                )
            }
        }

        // =========================================================================
        // 1. ACCOUNTABILITY PARTNER EMAIL (TOP)
        // =========================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 20.dp, glassAlpha = 0.95f, hasSheen = false),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "1. Accountability Partner Email",
                                color = titleColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Required to approve turning off the blocker",
                                color = descColor,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                val isPartnerLocked = savedPartnerEmail.isNotBlank() && BlockerManager.hasValidPartnerEmail(context)

                if (isPartnerLocked) {
                    // Read-only locked view: show "Partner active: <email>" with NO edit box
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("partner_active_status")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Partner active: $savedPartnerEmail",
                                    color = if (isLightMode) Color(0xFF065F46) else Color(0xFF34D399),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    if (!pendingPartnerChangeId.isNullOrBlank()) {
                        Surface(
                            color = Color(0xFFF59E0B).copy(alpha = 0.14f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.HourglassTop,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Partner change request sent to $savedPartnerEmail. Locked until your current partner clicks Approve in their email.",
                                    color = if (isLightMode) Color(0xFF92400E) else Color(0xFFFDE68A),
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showChangePartnerDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF38BDF8).copy(alpha = 0.1f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("request_partner_change_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Change / Remove Partner (Requires Partner Approval)",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = partnerEmailInput,
                        onValueChange = {
                            partnerEmailInput = it
                            partnerEmailWarning = null
                        },
                        label = { Text("Partner's Email Address") },
                        placeholder = { Text("partner@example.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("partner_email_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = if (isLightMode) Color(0xFF94A3B8) else Color.White.copy(alpha = 0.28f),
                            focusedTextColor = titleColor,
                            unfocusedTextColor = titleColor,
                            focusedLabelColor = Color(0xFF38BDF8),
                            unfocusedLabelColor = descColor
                        )
                    )

                    if (partnerEmailWarning != null) {
                        Surface(
                            color = Color(0xFFEF4444).copy(alpha = 0.14f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = partnerEmailWarning!!,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val err = BlockerManager.validateAndSavePartnerEmail(context, partnerEmailInput)
                            if (err != null) {
                                partnerEmailWarning = err
                            } else {
                                partnerEmailWarning = null
                                partnerChangeFeedbackBanner = null
                                savedPartnerEmail = BlockerManager.getPartnerEmail(context)
                                partnerEmailInput = ""
                                Toast.makeText(context, "Partner email saved & locked", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("save_partner_email_button")
                    ) {
                        Text(
                            text = "Save",
                            color = Color(0xFF061018),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                if (partnerChangeFeedbackBanner != null) {
                    Text(
                        text = partnerChangeFeedbackBanner!!,
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // =========================================================================
        // 2. PIN ("Set PIN" — REUSES EXISTING LockManager PIN SYSTEM)
        // =========================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 20.dp, glassAlpha = 0.95f, hasSheen = false),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFFFBBF24).copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFFBBF24).copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Key,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "2. App Security PIN",
                                color = titleColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (hasPin) "PIN is active and locked (Guardian Recovery Key protected)"
                                else "Set your 4-digit PIN for Settings Guard protection",
                                color = descColor,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    StatusBadgePill(
                        isActive = hasPin,
                        activeText = "PIN Set",
                        inactiveText = "Not Set"
                    )
                }

                Button(
                    onClick = { showSetPinModal = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasPin) Color(0xFF10B981).copy(alpha = 0.22f) else Color(0xFFFBBF24)
                    ),
                    border = if (hasPin) BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.55f)) else null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("blocker_set_pin_button")
                ) {
                    Icon(
                        imageVector = if (hasPin) Icons.Filled.Lock else Icons.Filled.Key,
                        contentDescription = null,
                        tint = if (hasPin) Color(0xFF34D399) else Color(0xFF061018),
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set PIN",
                        color = if (hasPin) Color(0xFF34D399) else Color(0xFF061018),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // =========================================================================
        // 3. BLOCK ADULT CONTENT SWITCH (BLOCKERHERO-STYLE)
        // =========================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 20.dp, glassAlpha = 0.95f, hasSheen = false),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(
                1.5.dp,
                if (isBlockerEnabled) Color(0xFF10B981).copy(alpha = 0.6f)
                else Color.White.copy(alpha = 0.18f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isBlockerEnabled) Color(0xFF10B981).copy(alpha = 0.18f)
                                    else Color(0xFFEF4444).copy(alpha = 0.14f),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isBlockerEnabled) Color(0xFF10B981).copy(alpha = 0.5f)
                                    else Color(0xFFEF4444).copy(alpha = 0.4f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = if (isBlockerEnabled) Color(0xFF34D399) else Color(0xFFF87171),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Block adult content",
                                color = titleColor,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isBlockerEnabled) "Active across Chrome & all browsers"
                                else "OFF • Turn ON after setting Partner Email, PIN & Accessibility",
                                color = if (isBlockerEnabled) Color(0xFF34D399) else descColor,
                                fontSize = 12.sp,
                                fontWeight = if (isBlockerEnabled) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }

                    Switch(
                        checked = isBlockerEnabled,
                        onCheckedChange = { wantToEnable ->
                            if (wantToEnable) {
                                // Turning ON: require Partner Email + PIN + Accessibility
                                if (!BlockerManager.hasValidPartnerEmail(context)) {
                                    partnerEmailWarning = "Please enter and save a valid partner email (not your own) first"
                                    Toast.makeText(
                                        context,
                                        "Step 1 required: Save your Accountability Partner email first",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Switch
                                }
                                if (!hasPin) {
                                    showSetPinModal = true
                                    Toast.makeText(
                                        context,
                                        "Step 2 required: Please set your PIN first",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Switch
                                }
                                if (!isAccessibilityAlive) {
                                    pendingTurnOnAfterAccessibility = true
                                    Toast.makeText(
                                        context,
                                        "Please enable Purity Tracker in Accessibility Settings to turn ON the blocker",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    AntiUninstallManager.requestAccessibilityService(context)
                                    return@Switch
                                }

                                // Request overlay permission if not yet granted (non-blocking; BlockerOverlayActivity is automatic fallback)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                    try {
                                        val overlayIntent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        ).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(overlayIntent)
                                    } catch (e: Exception) {}
                                }

                                BlockerManager.enableAdultBlocker(context)
                                isBlockerEnabled = true
                                requestFeedbackBanner = null
                                Toast.makeText(context, "Block adult content is now ON", Toast.LENGTH_SHORT).show()
                            } else {
                                // Turning OFF: NEVER turn off directly and NEVER allow PIN to turn it off!
                                // Show the accountability partner request dialog.
                                showTurnOffDialog = true
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF061018),
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("block_adult_content_switch")
                    )
                }

                if (!pendingRequestId.isNullOrBlank() && isBlockerEnabled) {
                    Surface(
                        color = Color(0xFFF59E0B).copy(alpha = 0.14f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HourglassTop,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Disable request sent to $savedPartnerEmail. Blocker stays ON until your partner clicks Approve in their email.",
                                color = if (isLightMode) Color(0xFF92400E) else Color(0xFFFDE68A),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                if (requestFeedbackBanner != null) {
                    Text(
                        text = requestFeedbackBanner!!,
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // =========================================================================
        // 4. ACCESSIBILITY + 6. INLINE KEEP-ALIVE (BATTERY + OEM AUTO-START + LIVE STATUS ROW)
        // =========================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 20.dp, glassAlpha = 0.95f, hasSheen = false),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF2DD4BF).copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = Color(0xFF2DD4BF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "4. Accessibility & Keep-Alive",
                                color = titleColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when {
                                    isAccessibilityAlive -> "Accessibility service is ALIVE & protecting browsers + settings"
                                    isAccessibilityDeadButEnabled -> "Stopped by OS! Tap below to toggle Purity Tracker OFF & ON"
                                    else -> "Required for browser blocking & anti-uninstall protection"
                                },
                                color = if (isAccessibilityDeadButEnabled) Color(0xFFF87171) else descColor,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    StatusBadgePill(
                        isActive = isAccessibilityAlive,
                        activeText = "Alive",
                        inactiveText = if (isAccessibilityDeadButEnabled) "Stopped" else "OFF"
                    )
                }

                Button(
                    onClick = {
                        AntiUninstallManager.requestAccessibilityService(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAccessibilityAlive) Color(0xFF10B981).copy(alpha = 0.22f) else Color(0xFF2DD4BF)
                    ),
                    border = if (isAccessibilityAlive) BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.55f)) else null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("turn_on_accessibility_button")
                ) {
                    Icon(
                        imageVector = if (isAccessibilityAlive) Icons.Filled.CheckCircle else Icons.Filled.Security,
                        contentDescription = null,
                        tint = if (isAccessibilityAlive) Color(0xFF34D399) else Color(0xFF061018),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Turn ON Accessibility",
                        color = if (isAccessibilityAlive) Color(0xFF34D399) else Color(0xFF061018),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // 6. Keep-alive (inside the Accessibility step, no separate settings hunt):
                // Right after Accessibility is enabled (or always visible inside this card for 1-tap access),
                // ask in the same flow, one tap each: (a) Battery optimization exemption; (b) OEM auto-start.
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text(
                    text = if (isAccessibilityAlive && (!isBatteryUnrestricted || !isOemAutoStartOn))
                        "Next step in this flow — Keep Accessibility alive 24/7 (1 tap each):"
                    else
                        "Keep-Alive Protection (prevents OS from killing Accessibility):",
                    color = titleColor,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // (a) Battery optimization exemption
                    OutlinedButton(
                        onClick = {
                            AntiUninstallManager.requestIgnoreBatteryOptimization(context)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isBatteryUnrestricted) Color(0xFF10B981).copy(alpha = 0.55f)
                            else Color(0xFF38BDF8).copy(alpha = 0.65f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isBatteryUnrestricted) Color(0xFF10B981).copy(alpha = 0.12f)
                            else Color(0xFF38BDF8).copy(alpha = 0.12f)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("keep_alive_battery_button")
                    ) {
                        Icon(
                            imageVector = if (isBatteryUnrestricted) Icons.Filled.CheckCircle else Icons.Outlined.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (isBatteryUnrestricted) Color(0xFF34D399) else Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBatteryUnrestricted) "Battery Exempt ✓" else "1. Unrestricted Battery",
                            color = if (isBatteryUnrestricted) Color(0xFF34D399) else titleColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    // (b) OEM Auto-start
                    OutlinedButton(
                        onClick = {
                            AntiUninstallManager.openOemAutoStartSettings(context)
                            isOemAutoStartOn = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isOemAutoStartOn) Color(0xFF10B981).copy(alpha = 0.55f)
                            else Color(0xFFFBBF24).copy(alpha = 0.65f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isOemAutoStartOn) Color(0xFF10B981).copy(alpha = 0.12f)
                            else Color(0xFFFBBF24).copy(alpha = 0.12f)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("keep_alive_autostart_button")
                    ) {
                        Icon(
                            imageVector = if (isOemAutoStartOn) Icons.Filled.CheckCircle else Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = if (isOemAutoStartOn) Color(0xFF34D399) else Color(0xFFFBBF24),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOemAutoStartOn) "Auto-Start ON ✓" else "2. OEM Auto-Start",
                            color = if (isOemAutoStartOn) Color(0xFF34D399) else titleColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                // Live status row: Accessibility alive / Battery unrestricted / Auto-start on (using REAL system values)
                Surface(
                    color = Color.Black.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("keep_alive_live_status_row")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiveStatusDotItem(
                            label = "Accessibility alive",
                            isOk = isAccessibilityAlive
                        )
                        LiveStatusDotItem(
                            label = "Battery unrestricted",
                            isOk = isBatteryUnrestricted
                        )
                        LiveStatusDotItem(
                            label = "Auto-start on",
                            isOk = isOemAutoStartOn
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 5. DEVICE ADMIN ("Activate Device Admin")
        // =========================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 20.dp, glassAlpha = 0.95f, hasSheen = false),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF818CF8).copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF818CF8).copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "5. Device Admin",
                                color = titleColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isDeviceAdminActive) "Device Administrator is active (prevents uninstall)"
                                else "Blocks unauthorized app uninstallation",
                                color = descColor,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    StatusBadgePill(
                        isActive = isDeviceAdminActive,
                        activeText = "Active",
                        inactiveText = "Inactive"
                    )
                }

                Button(
                    onClick = {
                        if (!isDeviceAdminActive) {
                            AntiUninstallManager.requestDeviceAdmin(context)
                        } else {
                            Toast.makeText(context, "Device Admin is already active", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDeviceAdminActive) Color(0xFF10B981).copy(alpha = 0.22f) else Color(0xFF818CF8)
                    ),
                    border = if (isDeviceAdminActive) BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.55f)) else null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("activate_device_admin_button")
                ) {
                    Icon(
                        imageVector = if (isDeviceAdminActive) Icons.Filled.VerifiedUser else Icons.Filled.Security,
                        contentDescription = null,
                        tint = if (isDeviceAdminActive) Color(0xFF34D399) else Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Activate Device Admin",
                        color = if (isDeviceAdminActive) Color(0xFF34D399) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // =========================================================================
    // DIALOG: Turn OFF - Block adult content (Accountability Partner Request)
    // =========================================================================
    if (showTurnOffDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSendingDisableRequest) {
                    showTurnOffDialog = false
                }
            },
            containerColor = Color(0xFF0F172A),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCBD5E1),
            title = {
                Text(
                    text = "Turn OFF - Block adult content",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "A request will be sent to your accountability partner",
                        fontSize = 14.5.sp,
                        color = Color.White
                    )
                    if (savedPartnerEmail.isNotBlank()) {
                        Text(
                            text = "Partner: $savedPartnerEmail",
                            fontSize = 12.5.sp,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "The blocker will stay ON until your accountability partner clicks Approve in the email.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTurnOffDialog = false },
                    enabled = !isSendingDisableRequest,
                    modifier = Modifier.testTag("turn_off_cancel_button")
                ) {
                    Text("Cancel", color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSendingDisableRequest = true
                        BlockerManager.sendDisableRequestToPartner(context) { success, message ->
                            isSendingDisableRequest = false
                            showTurnOffDialog = false
                            pendingRequestId = BlockerManager.getPendingRequestId(context)
                            requestFeedbackBanner = message
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = !isSendingDisableRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("turn_off_send_request_button")
                ) {
                    if (isSendingDisableRequest) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF061018)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "Send Request",
                        color = Color(0xFF061018),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // =========================================================================
    // DIALOG: Change / Remove Partner Email (Requires Current Partner Approval)
    // =========================================================================
    if (showChangePartnerDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSendingPartnerChangeRequest) {
                    showChangePartnerDialog = false
                }
            },
            containerColor = Color(0xFF0F172A),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCBD5E1),
            title = {
                Text(
                    text = "Change Accountability Partner",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "A request will be sent to your accountability partner",
                        fontSize = 14.5.sp,
                        color = Color.White
                    )
                    if (savedPartnerEmail.isNotBlank()) {
                        Text(
                            text = "Current partner: $savedPartnerEmail",
                            fontSize = 12.5.sp,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Your partner email is locked. You can only enter a new partner email after your current partner clicks Approve in the email.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showChangePartnerDialog = false },
                    enabled = !isSendingPartnerChangeRequest,
                    modifier = Modifier.testTag("change_partner_cancel_button")
                ) {
                    Text("Cancel", color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSendingPartnerChangeRequest = true
                        BlockerManager.sendPartnerChangeRequestToCurrentPartner(context) { _, message ->
                            isSendingPartnerChangeRequest = false
                            showChangePartnerDialog = false
                            pendingPartnerChangeId = BlockerManager.getPendingPartnerChangeRequestId(context)
                            partnerChangeFeedbackBanner = message
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = !isSendingPartnerChangeRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("change_partner_send_request_button")
                ) {
                    if (isSendingPartnerChangeRequest) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF061018)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "Send Request",
                        color = Color(0xFF061018),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // =========================================================================
    // DIALOG: Set PIN / Guardian Recovery Key Reset (Reuses existing LockManager)
    // =========================================================================
    if (showSetPinModal) {
        BlockerPinSetupDialog(
            hasExistingPin = hasPin,
            onDismiss = { showSetPinModal = false },
            onPinUpdated = {
                hasPin = LockManager.hasPin(context)
                showSetPinModal = false
            }
        )
    }
}

@Composable
private fun StatusBadgePill(
    isActive: Boolean,
    activeText: String,
    inactiveText: String
) {
    val color = if (isActive) Color(0xFF10B981) else Color(0xFFF59E0B)
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isActive) activeText else inactiveText,
            color = if (isActive) Color(0xFF34D399) else Color(0xFFFBBF24),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LiveStatusDotItem(
    label: String,
    isOk: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isOk) Color(0xFF10B981) else Color(0xFFEF4444))
        )
        Text(
            text = label,
            color = if (isOk) Color(0xFF34D399) else Color(0xFFFCA5A5),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Reuses `LockManager` (same storage, same 4-digit PIN rules, same Guardian Recovery Key reset).
 */
@Composable
private fun BlockerPinSetupDialog(
    hasExistingPin: Boolean,
    onDismiss: () -> Unit,
    onPinUpdated: () -> Unit
) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    var recoveryKeyInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSendingKey by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFCBD5E1),
        title = {
            Text(
                text = if (!hasExistingPin) "Set 4-Digit Security PIN" else "Security PIN Locked",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!hasExistingPin) {
                    Text(
                        text = "Enter a 4-digit PIN. Once saved, it cannot be changed without your Guardian Recovery Key.",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("4-Digit PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFBBF24),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                } else {
                    Text(
                        text = "Your PIN is permanently locked. To reset it, request a 6-digit Recovery Key sent to your Accountability Partner's email.",
                        fontSize = 12.5.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    OutlinedButton(
                        onClick = {
                            val key = LockManager.getGuardianKey(context)
                            if (key.isNotEmpty()) {
                                GuardianManager.sendGuardianRecoveryKeyEmail(context, key)
                                errorMsg = "Recovery Key sent to your partner's email"
                            } else {
                                errorMsg = "No recovery key found"
                            }
                        },
                        enabled = !isSendingKey,
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isSendingKey) "Sending Recovery Key..." else "Send Recovery Key to Partner",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedTextField(
                        value = recoveryKeyInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) recoveryKeyInput = it },
                        label = { Text("6-Digit Partner Recovery Key") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinInput = it },
                        label = { Text("New 4-Digit PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFBBF24),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF94A3B8))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!hasExistingPin) {
                        if (pinInput.length != 4) {
                            errorMsg = "PIN must be 4 digits"
                        } else {
                            val (saved, guardianKey) = LockManager.setPin(context, pinInput)
                            if (saved) {
                                if (!guardianKey.isNullOrEmpty()) {
                                    GuardianManager.sendGuardianRecoveryKeyEmail(context, guardianKey)
                                }
                                Toast.makeText(context, "PIN saved & locked", Toast.LENGTH_SHORT).show()
                                onPinUpdated()
                            } else {
                                errorMsg = "Could not save PIN"
                            }
                        }
                    } else {
                        if (recoveryKeyInput.length != 6 || newPinInput.length != 4) {
                            errorMsg = "Enter the 6-digit recovery key and a new 4-digit PIN"
                        } else {
                            when (val res = LockManager.resetPinWithGuardianKey(context, recoveryKeyInput, newPinInput)) {
                                is ResetResult.Success -> {
                                    GuardianManager.sendGuardianRecoveryKeyEmail(context, res.newGuardianKey)
                                    Toast.makeText(context, "PIN reset successfully", Toast.LENGTH_SHORT).show()
                                    onPinUpdated()
                                }
                                is ResetResult.InvalidKey -> {
                                    errorMsg = "Incorrect Guardian Key (${res.attemptsRemaining} attempts left)"
                                }
                                is ResetResult.LockedOut -> {
                                    errorMsg = "Locked out for ${res.remainingMinutes} minutes"
                                }
                                is ResetResult.InvalidNewPin -> {
                                    errorMsg = "New PIN must be 4 digits"
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24))
            ) {
                Text(
                    text = if (!hasExistingPin) "Save PIN" else "Reset PIN",
                    color = Color(0xFF061018),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
