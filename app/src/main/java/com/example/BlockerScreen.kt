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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.theme.glassCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Purity Tracker v1.3.4 — Compact Blocker Screen + Partner-Authorized PIN Recovery.
 *
 * Layout:
 * - Single optional status banner at the top (status messages appear ONLY here; errors appear ONLY as a single toast).
 * - ONE compact list card of 5 small rows:
 *   1) Accountability Partner (email, chip "Locked")
 *   2) App Security PIN (chip "PIN Set" + "Forgot PIN?" link)
 *   3) Block adult content (toggle)
 *   4) Accessibility (chip "Alive")
 *   5) Device Admin (chip "Active")
 * - Small "Keep-Alive" group with 2 rows:
 *   1) Battery exemption
 *   2) Auto-start
 */
@Composable
fun BlockerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descColor = if (isLightMode) Color(0xFF334155) else Color(0xFF94A3B8)

    BackHandler {
        onBack()
    }

    // Single top status banner state (status messages ONLY here; errors ONLY via Toast)
    var topStatusBanner by remember { mutableStateOf<String?>(null) }

    // 1. Partner Email state
    var savedPartnerEmail by remember { mutableStateOf(BlockerManager.getPartnerEmail(context)) }
    var partnerEmailInput by remember { mutableStateOf("") }
    var pendingPartnerChangeId by remember { mutableStateOf(BlockerManager.getPendingPartnerChangeRequestId(context)) }
    var showChangePartnerDialog by remember { mutableStateOf(false) }
    var isSendingPartnerChangeRequest by remember { mutableStateOf(false) }

    // 2. PIN & Forgot PIN state
    var hasPin by remember { mutableStateOf(LockManager.hasPin(context)) }
    var showSetPinModal by remember { mutableStateOf(false) }
    var pendingPinResetId by remember { mutableStateOf(BlockerManager.getPendingPinResetRequestId(context)) }
    var isSendingPinResetRequest by remember { mutableStateOf(false) }

    // 3. Adult Blocker Switch state
    var isBlockerEnabled by remember { mutableStateOf(BlockerManager.isAdultBlockerEnabled(context)) }
    var pendingRequestId by remember { mutableStateOf(BlockerManager.getPendingRequestId(context)) }
    var showTurnOffDialog by remember { mutableStateOf(false) }
    var isSendingDisableRequest by remember { mutableStateOf(false) }
    var pendingTurnOnAfterAccessibility by remember { mutableStateOf(false) }

    // 4 & 5 & Keep-Alive live system states
    var isAccessibilityAlive by remember { mutableStateOf(false) }
    var isAccessibilityDeadButEnabled by remember { mutableStateOf(false) }
    var isBatteryUnrestricted by remember { mutableStateOf(false) }
    var isOemAutoStartOn by remember { mutableStateOf(false) }
    var isDeviceAdminActive by remember { mutableStateOf(false) }

    suspend fun refreshSystemStates() {
        val states = withContext(Dispatchers.IO) {
            val pin = LockManager.hasPin(context)
            val partner = BlockerManager.getPartnerEmail(context)
            val pendingPartnerChange = BlockerManager.getPendingPartnerChangeRequestId(context)
            val pendingPinReset = BlockerManager.getPendingPinResetRequestId(context)
            val blocker = BlockerManager.isAdultBlockerEnabled(context)
            val pendingReq = BlockerManager.getPendingRequestId(context)
            val accAlive = AntiUninstallManager.isAccessibilityServiceAlive(context)
            val accDead = AntiUninstallManager.isAccessibilityDeadButEnabled(context)
            val battery = AntiUninstallManager.isBatteryOptimizationIgnored(context)
            val autoStart = AntiUninstallManager.isOemAutoStartVisited(context)
            val admin = AntiUninstallManager.isDeviceAdminActive(context)
            GuardHealthWorker.checkNowAndNotify(context)
            listOf(pin, partner, pendingPartnerChange, pendingPinReset, blocker, pendingReq, accAlive, accDead, battery, autoStart, admin)
        }
        hasPin = states[0] as Boolean
        savedPartnerEmail = states[1] as String
        pendingPartnerChangeId = states[2] as String?
        pendingPinResetId = states[3] as String?
        isBlockerEnabled = states[4] as Boolean
        pendingRequestId = states[5] as String?
        val nowAccAlive = states[6] as Boolean
        isAccessibilityAlive = nowAccAlive
        isAccessibilityDeadButEnabled = states[7] as Boolean
        isBatteryUnrestricted = states[8] as Boolean
        isOemAutoStartOn = states[9] as Boolean
        isDeviceAdminActive = states[10] as Boolean

        if (pendingTurnOnAfterAccessibility && nowAccAlive && hasPin && BlockerManager.hasValidPartnerEmail(context)) {
            pendingTurnOnAfterAccessibility = false
            BlockerManager.enableAdultBlocker(context)
            isBlockerEnabled = true
            topStatusBanner = "Block adult content is now ON."
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            refreshSystemStates()
            BlockerManager.ensurePartnerApprovalListener(context) { status ->
                if (status == "approved") {
                    isBlockerEnabled = false
                    pendingRequestId = null
                    topStatusBanner = "Partner approved: Block adult content turned OFF."
                } else if (status == "denied") {
                    pendingRequestId = null
                    topStatusBanner = "Partner denied the disable request. Blocker remains ON."
                }
            }
            BlockerManager.ensurePartnerChangeApprovalListener(context) { status ->
                if (status == "approved") {
                    savedPartnerEmail = ""
                    partnerEmailInput = ""
                    pendingPartnerChangeId = null
                    topStatusBanner = "Partner approved: Enter your new accountability partner email."
                } else if (status == "denied") {
                    pendingPartnerChangeId = null
                    topStatusBanner = "Partner denied the email change request."
                }
            }
            BlockerManager.ensurePinResetApprovalListener(context) { status ->
                if (status == "approved") {
                    hasPin = false
                    pendingPinResetId = null
                    topStatusBanner = "Partner authorized PIN reset. Set your new 4-digit PIN."
                    showSetPinModal = true
                } else if (status == "denied") {
                    pendingPinResetId = null
                    topStatusBanner = "Partner denied the PIN reset request."
                }
            }
            delay(1200L)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPin = LockManager.hasPin(context)
                savedPartnerEmail = BlockerManager.getPartnerEmail(context)
                pendingPartnerChangeId = BlockerManager.getPendingPartnerChangeRequestId(context)
                pendingPinResetId = BlockerManager.getPendingPinResetRequestId(context)
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
                    topStatusBanner = "Block adult content is now ON."
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val activeBannerText = topStatusBanner ?: when {
        !pendingRequestId.isNullOrBlank() && isBlockerEnabled ->
            "Disable request sent to $savedPartnerEmail. Waiting for partner approval."
        !pendingPartnerChangeId.isNullOrBlank() ->
            "Partner change request sent to $savedPartnerEmail. Waiting for partner approval."
        !pendingPinResetId.isNullOrBlank() && hasPin ->
            "PIN reset request sent to $savedPartnerEmail. Waiting for partner approval."
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                    .testTag("blocker_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = titleColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Blocker",
                color = titleColor,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Single Top Status Banner (ONLY one banner on the screen)
        if (!activeBannerText.isNullOrBlank()) {
            Surface(
                color = Color(0xFF10B981).copy(alpha = 0.14f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("blocker_top_status_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeBannerText,
                            color = if (isLightMode) Color(0xFF065F46) else Color(0xFF34D399),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp
                        )
                    }
                    if (topStatusBanner != null) {
                        IconButton(
                            onClick = { topStatusBanner = null },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        val isPartnerLocked = savedPartnerEmail.isNotBlank() && BlockerManager.hasValidPartnerEmail(context)

        // =========================================================================
        // ONE COMPACT LIST CARD OF 5 SMALL ROWS
        // =========================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 18.dp, glassAlpha = 0.95f, hasSheen = false),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // -----------------------------------------------------------------
                // ROW 1: Accountability Partner (email, chip "Locked")
                // -----------------------------------------------------------------
                if (isPartnerLocked) {
                    CompactBlockerRow(
                        icon = Icons.Outlined.Email,
                        iconTint = Color(0xFF38BDF8),
                        title = "Accountability Partner",
                        subtitle = savedPartnerEmail,
                        titleColor = titleColor,
                        subtitleColor = descColor,
                        modifier = Modifier
                            .testTag("partner_active_status")
                            .clickable { showChangePartnerDialog = true },
                        trailing = {
                            CompactStatusChip(
                                text = "Locked",
                                isPositive = true,
                                modifier = Modifier
                                    .testTag("request_partner_change_button")
                                    .clickable { showChangePartnerDialog = true }
                            )
                        }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                CompactRowIcon(icon = Icons.Outlined.Email, tint = Color(0xFF38BDF8))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Accountability Partner",
                                        color = titleColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Enter partner email",
                                        color = descColor,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                            CompactStatusChip(text = "Required", isPositive = false)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = partnerEmailInput,
                                onValueChange = { partnerEmailInput = it },
                                placeholder = { Text("partner@example.com", fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("partner_email_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.24f),
                                    focusedTextColor = titleColor,
                                    unfocusedTextColor = titleColor
                                )
                            )
                            Surface(
                                color = Color(0xFF10B981),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .testTag("save_partner_email_button")
                                    .clickable {
                                        val err = BlockerManager.validateAndSavePartnerEmail(context, partnerEmailInput)
                                        if (err != null) {
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        } else {
                                            savedPartnerEmail = BlockerManager.getPartnerEmail(context)
                                            partnerEmailInput = ""
                                            topStatusBanner = "Accountability partner email saved and locked."
                                        }
                                    }
                            ) {
                                Text(
                                    text = "Save",
                                    color = Color(0xFF061018),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // -----------------------------------------------------------------
                // ROW 2: App Security PIN (chip "PIN Set" + "Forgot PIN?" link)
                // -----------------------------------------------------------------
                CompactBlockerRow(
                    icon = Icons.Outlined.Key,
                    iconTint = Color(0xFFFBBF24),
                    title = "App Security PIN",
                    subtitle = if (hasPin) "4-digit PIN locked" else "Tap to set 4-digit PIN",
                    titleColor = titleColor,
                    subtitleColor = descColor,
                    modifier = Modifier
                        .testTag("blocker_set_pin_button")
                        .clickable {
                            if (!hasPin) {
                                showSetPinModal = true
                            }
                        },
                    trailing = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (hasPin) {
                                Text(
                                    text = if (isSendingPinResetRequest) "Sending..." else "Forgot PIN?",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier
                                        .testTag("forgot_pin_link")
                                        .clickable(enabled = !isSendingPinResetRequest) {
                                            if (!BlockerManager.hasValidPartnerEmail(context)) {
                                                Toast.makeText(
                                                    context,
                                                    "Save an accountability partner email first.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                return@clickable
                                            }
                                            isSendingPinResetRequest = true
                                            BlockerManager.sendPinResetRequestToPartner(context) { success, message ->
                                                isSendingPinResetRequest = false
                                                pendingPinResetId = BlockerManager.getPendingPinResetRequestId(context)
                                                if (success) {
                                                    topStatusBanner = message
                                                } else {
                                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                )
                            }
                            CompactStatusChip(
                                text = if (hasPin) "PIN Set" else "Set PIN",
                                isPositive = hasPin,
                                modifier = if (!hasPin) {
                                    Modifier.clickable { showSetPinModal = true }
                                } else {
                                    Modifier
                                }
                            )
                        }
                    }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // -----------------------------------------------------------------
                // ROW 3: Block adult content (toggle)
                // -----------------------------------------------------------------
                CompactBlockerRow(
                    icon = Icons.Filled.Shield,
                    iconTint = if (isBlockerEnabled) Color(0xFF34D399) else Color(0xFFF87171),
                    title = "Block adult content",
                    subtitle = if (isBlockerEnabled) "Active across browsers" else "Blocks adult sites",
                    titleColor = titleColor,
                    subtitleColor = descColor,
                    trailing = {
                        Switch(
                            checked = isBlockerEnabled,
                            onCheckedChange = { wantToEnable ->
                                if (wantToEnable) {
                                    if (!BlockerManager.hasValidPartnerEmail(context)) {
                                        Toast.makeText(
                                            context,
                                            "Save your Accountability Partner email first",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@Switch
                                    }
                                    if (!hasPin) {
                                        showSetPinModal = true
                                        Toast.makeText(
                                            context,
                                            "Set your 4-digit PIN first",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@Switch
                                    }
                                    if (!isAccessibilityAlive) {
                                        pendingTurnOnAfterAccessibility = true
                                        Toast.makeText(
                                            context,
                                            "Enable Purity Tracker in Accessibility Settings first",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        AntiUninstallManager.requestAccessibilityService(context)
                                        return@Switch
                                    }

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
                                    topStatusBanner = "Block adult content is now ON."
                                } else {
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
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // -----------------------------------------------------------------
                // ROW 4: Accessibility (chip "Alive")
                // -----------------------------------------------------------------
                CompactBlockerRow(
                    icon = Icons.Filled.Security,
                    iconTint = Color(0xFF2DD4BF),
                    title = "Accessibility",
                    subtitle = when {
                        isAccessibilityAlive -> "Guards browsers & settings"
                        isAccessibilityDeadButEnabled -> "Stopped — tap to restart"
                        else -> "Tap to enable service"
                    },
                    titleColor = titleColor,
                    subtitleColor = if (isAccessibilityDeadButEnabled) Color(0xFFF87171) else descColor,
                    modifier = Modifier
                        .testTag("turn_on_accessibility_button")
                        .clickable {
                            AntiUninstallManager.requestAccessibilityService(context)
                        },
                    trailing = {
                        CompactStatusChip(
                            text = when {
                                isAccessibilityAlive -> "Alive"
                                isAccessibilityDeadButEnabled -> "Stopped"
                                else -> "OFF"
                            },
                            isPositive = isAccessibilityAlive
                        )
                    }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // -----------------------------------------------------------------
                // ROW 5: Device Admin (chip "Active")
                // -----------------------------------------------------------------
                CompactBlockerRow(
                    icon = Icons.Outlined.VerifiedUser,
                    iconTint = Color(0xFF818CF8),
                    title = "Device Admin",
                    subtitle = if (isDeviceAdminActive) "Blocks app uninstall" else "Tap to activate",
                    titleColor = titleColor,
                    subtitleColor = descColor,
                    modifier = Modifier
                        .testTag("activate_device_admin_button")
                        .clickable {
                            if (!isDeviceAdminActive) {
                                AntiUninstallManager.requestDeviceAdmin(context)
                            }
                        },
                    trailing = {
                        CompactStatusChip(
                            text = if (isDeviceAdminActive) "Active" else "Inactive",
                            isPositive = isDeviceAdminActive
                        )
                    }
                )
            }
        }

        // =========================================================================
        // SMALL "KEEP-ALIVE" GROUP WITH 2 ROWS: Battery exemption, Auto-start
        // =========================================================================
        Text(
            text = "Keep-Alive",
            color = descColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerSize = 18.dp, glassAlpha = 0.95f, hasSheen = false),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // Keep-Alive Row 1: Battery exemption
                CompactBlockerRow(
                    icon = Icons.Outlined.PowerSettingsNew,
                    iconTint = Color(0xFF38BDF8),
                    title = "Battery exemption",
                    subtitle = if (isBatteryUnrestricted) "Unrestricted active" else "Tap to allow unrestricted",
                    titleColor = titleColor,
                    subtitleColor = descColor,
                    modifier = Modifier
                        .testTag("keep_alive_battery_button")
                        .clickable {
                            AntiUninstallManager.requestIgnoreBatteryOptimization(context)
                        },
                    trailing = {
                        CompactStatusChip(
                            text = if (isBatteryUnrestricted) "Unrestricted" else "Allow",
                            isPositive = isBatteryUnrestricted
                        )
                    }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Keep-Alive Row 2: Auto-start
                CompactBlockerRow(
                    icon = Icons.Filled.Refresh,
                    iconTint = Color(0xFFFBBF24),
                    title = "Auto-start",
                    subtitle = if (isOemAutoStartOn) "Background auto-start on" else "Tap to enable auto-start",
                    titleColor = titleColor,
                    subtitleColor = descColor,
                    modifier = Modifier
                        .testTag("keep_alive_autostart_button")
                        .clickable {
                            AntiUninstallManager.openOemAutoStartSettings(context)
                            isOemAutoStartOn = true
                        },
                    trailing = {
                        CompactStatusChip(
                            text = if (isOemAutoStartOn) "ON" else "Enable",
                            isPositive = isOemAutoStartOn
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
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
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "A request will be sent to your accountability partner ($savedPartnerEmail).",
                    fontSize = 13.5.sp,
                    color = Color.White
                )
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
                            if (success) {
                                topStatusBanner = message
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isSendingDisableRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("turn_off_send_request_button")
                ) {
                    if (isSendingDisableRequest) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF061018)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "Send an approval request to your current partner ($savedPartnerEmail) to unlock changing the email.",
                    fontSize = 13.5.sp,
                    color = Color.White
                )
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
                        BlockerManager.sendPartnerChangeRequestToCurrentPartner(context) { success, message ->
                            isSendingPartnerChangeRequest = false
                            showChangePartnerDialog = false
                            pendingPartnerChangeId = BlockerManager.getPendingPartnerChangeRequestId(context)
                            if (success) {
                                topStatusBanner = message
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isSendingPartnerChangeRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("change_partner_send_request_button")
                ) {
                    if (isSendingPartnerChangeRequest) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF061018)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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
    // DIALOG: Set 4-Digit Security PIN (Only when no PIN is set or after Partner reset approval)
    // =========================================================================
    if (showSetPinModal && !hasPin) {
        BlockerPinSetupDialog(
            onDismiss = { showSetPinModal = false },
            onPinSaved = {
                hasPin = LockManager.hasPin(context)
                showSetPinModal = false
                topStatusBanner = "4-digit Security PIN saved and locked."
            }
        )
    }
}

@Composable
private fun CompactBlockerRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    titleColor: Color,
    subtitleColor: Color,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        ) {
            CompactRowIcon(icon = icon, tint = iconTint)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = titleColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = subtitleColor,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailing()
    }
}

@Composable
private fun CompactRowIcon(
    icon: ImageVector,
    tint: Color
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(tint.copy(alpha = 0.15f), RoundedCornerShape(9.dp))
            .border(1.dp, tint.copy(alpha = 0.4f), RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun CompactStatusChip(
    text: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val chipColor = if (isPositive) Color(0xFF10B981) else Color(0xFFF59E0B)
    Box(
        modifier = modifier
            .background(chipColor.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
            .border(1.dp, chipColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = if (isPositive) Color(0xFF34D399) else Color(0xFFFBBF24),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BlockerPinSetupDialog(
    onDismiss: () -> Unit,
    onPinSaved: () -> Unit
) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFCBD5E1),
        title = {
            Text(
                text = "Set 4-Digit Security PIN",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            OutlinedTextField(
                value = pinInput,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                label = { Text("4-Digit PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pinInput.length != 4) {
                        Toast.makeText(context, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                    } else {
                        val (saved, _) = LockManager.setPin(context, pinInput)
                        if (saved) {
                            onPinSaved()
                        } else {
                            Toast.makeText(context, "Could not save PIN", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Save PIN",
                    color = Color(0xFF061018),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
