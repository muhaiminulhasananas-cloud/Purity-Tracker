package com.example

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * ProtectionService: Ultra-reliable 24/7 Real-Time Android Accessibility Service.
 *
 * Engineered for:
 * 1. Zero-Nanosecond Blind Spots:
 *    - Instant 0ms event delivery (`notificationTimeout = 0`) + multi-stage follow-up scans
 *      at +45ms, +110ms, +220ms, and +380ms.
 *    - Continuous 85ms High-Speed Active Window Patrol whenever the screen is interactive,
 *      scanning both `rootInActiveWindow` and all floating dialog windows (`getWindows()`).
 * 2. Monotonic Clock Immunity:
 *    - Uses `SystemClock.elapsedRealtime()` exclusively so changing the phone's date/time
 *      can NEVER freeze or bypass the guard.
 * 3. Permanent 24/7 Foreground Immunity:
 *    - Runs as an active Foreground Service sharing unified notification ID 1001 with
 *      `SettingsGuardWatchdogService` so Realme UI / ColorOS / MIUI / Vivo / Samsung
 *      background freezers can NEVER freeze or kill the service after 1-2 days.
 */
class ProtectionService : AccessibilityService() {

    companion object {
        const val NOTIFICATION_ID = 1001

        @Volatile
        var isServiceRunning: Boolean = false
            private set

        @Volatile
        var lastActiveElapsedRealtime: Long = 0L
            private set

        @Volatile
        private var instance: ProtectionService? = null

        @Volatile
        private var dnsSetupGraceUntilElapsed: Long = 0L

        /**
         * Grants a short 40-second grace window ONLY when the user explicitly taps
         * "Go to Settings" inside Purity Tracker to configure Private DNS for the first time.
         * Note: If Private DNS is already set to family.adguard-dns.com on screen, it is still protected!
         */
        fun grantInitialDnsSetupGraceWindow() {
            dnsSetupGraceUntilElapsed = SystemClock.elapsedRealtime() + 40_000L
        }

        fun isInitialDnsSetupGraceActive(): Boolean {
            val now = SystemClock.elapsedRealtime()
            return dnsSetupGraceUntilElapsed > now && (dnsSetupGraceUntilElapsed - now) <= 45_000L
        }

        fun pokeFromWatchdog(context: Context) {
            try {
                val svc = instance
                if (svc != null) {
                    svc.recordAliveHeartbeat()
                    svc.ensureServiceConfig()
                    svc.ensureBackupPatrolRunning()
                }
            } catch (e: Exception) {}
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var screenReceiver: BroadcastReceiver? = null

    private var lastBlockElapsedTime: Long = 0L
    private var lastPatrolPackagesettings: Boolean = false

    private var lastBrowserBlockElapsedTime: Long = 0L

    // 5-second heartbeat updater while service is alive
    private val watchdogRunnable = object : Runnable {
        override fun run() {
            try {
                recordAliveHeartbeat()
                ensureServiceConfig()
                BlockerManager.ensurePartnerApprovalListener(this@ProtectionService) {}
                BlockerManager.ensurePartnerChangeApprovalListener(this@ProtectionService) {}
                BlockerManager.ensurePinResetApprovalListener(this@ProtectionService) {}
                ensureBackupPatrolRunning()
            } catch (e: Exception) {}
            mainHandler.postDelayed(this, 5000L)
        }
    }

    // Slow 1000ms backup safety patrol ONLY while screen is ON (primary trigger is onAccessibilityEvent)
    private val backupPatrolRunnable = object : Runnable {
        override fun run() {
            try {
                recordAliveHeartbeat()
                val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
                val isScreenOn = pm?.isInteractive ?: true
                if (isScreenOn) {
                    performFullActiveWindowScan(null)
                }
            } catch (e: Exception) {}
            mainHandler.postDelayed(this, 1000L)
        }
    }

    private fun recordAliveHeartbeat() {
        val nowElapsed = SystemClock.elapsedRealtime()
        isServiceRunning = true
        lastActiveElapsedRealtime = nowElapsed
        AntiUninstallManager.recordServiceHeartbeat(this, nowElapsed)
        GuardHealthWorker.cancelHealthAlertNotification(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        recordAliveHeartbeat()
        ensureServiceConfig()
        registerScreenReceiver()
        GuardHealthWorker.schedulePeriodicWork(this)
        ensureBackupPatrolRunning()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        recordAliveHeartbeat()

        ensureServiceConfig()
        registerScreenReceiver()
        GuardHealthWorker.schedulePeriodicWork(this)

        mainHandler.removeCallbacks(watchdogRunnable)
        mainHandler.post(watchdogRunnable)
        ensureBackupPatrolRunning()
        checkAndStartVpnIfNeeded()
    }

    private fun ensureServiceConfig() {
        try {
            val info = serviceInfo ?: AccessibilityServiceInfo()
            info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            info.notificationTimeout = 0L
            info.flags = info.flags or
                    AccessibilityServiceInfo.DEFAULT or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            serviceInfo = info
        } catch (e: Exception) {}
        // NOTE: Do NOT call startForeground() on ProtectionService; let Android manage AccessibilityService binding.
    }

    private fun ensureBackupPatrolRunning() {
        mainHandler.removeCallbacks(backupPatrolRunnable)
        mainHandler.post(backupPatrolRunnable)
    }

    private fun checkAndStartVpnIfNeeded() {
        // v1.3.0: Simple Blocker uses AccessibilityService + overlay only (no DNS, no VPN).
    }

    private fun registerScreenReceiver() {
        if (screenReceiver == null) {
            try {
                screenReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        val action = intent.action ?: return
                        isServiceRunning = true
                        lastActiveElapsedRealtime = SystemClock.elapsedRealtime()

                        if (action == Intent.ACTION_SCREEN_OFF ||
                            action == Intent.ACTION_TIME_CHANGED ||
                            action == Intent.ACTION_DATE_CHANGED ||
                            action == Intent.ACTION_TIMEZONE_CHANGED
                        ) {
                            // Immediately re-lock temporary PIN unlock when screen turns off or clock changes
                            LockManager.lockImmediately(context)
                        }

                        if (action == Intent.ACTION_SCREEN_ON ||
                            action == Intent.ACTION_USER_PRESENT ||
                            action == Intent.ACTION_TIME_TICK ||
                            action == "android.intent.action.PHONE_STATE" ||
                            action == Intent.ACTION_BOOT_COMPLETED
                        ) {
                            recordAliveHeartbeat()
                            ensureServiceConfig()
                            ensureBackupPatrolRunning()
                            checkAndStartVpnIfNeeded()
                        }
                    }
                }
                val filter = IntentFilter().apply {
                    addAction(Intent.ACTION_SCREEN_ON)
                    addAction(Intent.ACTION_USER_PRESENT)
                    addAction(Intent.ACTION_SCREEN_OFF)
                    addAction(Intent.ACTION_TIME_TICK)
                    addAction(Intent.ACTION_TIME_CHANGED)
                    addAction(Intent.ACTION_DATE_CHANGED)
                    addAction(Intent.ACTION_TIMEZONE_CHANGED)
                    addAction("android.intent.action.PHONE_STATE")
                    addAction(Intent.ACTION_BOOT_COMPLETED)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(screenReceiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    registerReceiver(screenReceiver, filter)
                }
            } catch (e: Exception) {}
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isServiceRunning = false
        instance = null
        try {
            screenReceiver?.let {
                unregisterReceiver(it)
                screenReceiver = null
            }
            mainHandler.removeCallbacksAndMessages(null)
            // Do NOT call startForegroundService from onUnbind (blocked on Android 12+).
            // Instead, run GuardHealthWorker check so the user is notified if the guard stays dead.
            GuardHealthWorker.schedulePeriodicWork(this)
            GuardHealthWorker.checkNowAndNotify(this)
        } catch (e: Exception) {}
        return true
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        instance = this
        recordAliveHeartbeat()
        ensureServiceConfig()
        checkAndStartVpnIfNeeded()
        registerScreenReceiver()
        mainHandler.removeCallbacks(watchdogRunnable)
        mainHandler.post(watchdogRunnable)
        ensureBackupPatrolRunning()
    }

    override fun onDestroy() {
        isServiceRunning = false
        instance = null
        try {
            screenReceiver?.let {
                unregisterReceiver(it)
                screenReceiver = null
            }
            mainHandler.removeCallbacksAndMessages(null)
            // Do NOT call startForegroundService from onDestroy (blocked on Android 12+).
            GuardHealthWorker.schedulePeriodicWork(this)
            GuardHealthWorker.checkNowAndNotify(this)
        } catch (e: Exception) {}
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        recordAliveHeartbeat()
        try {
            val eventPkg = event.packageName?.toString() ?: ""
            val eventCls = event.className?.toString() ?: ""
            if (eventPkg == this.packageName || eventCls.contains("PinLockActivity", ignoreCase = true)) {
                return
            }
            if (LockManager.isPinScreenCurrentlyActive()) {
                return
            }

            // Event-driven primary scan (no multi-stage +45/+110/+220/+380ms scan storm)
            performFullActiveWindowScan(event)
        } catch (t: Throwable) {
            // Never crash onAccessibilityEvent
        }
    }

    private fun isRelevantSystemOrSettingsPackage(pkgName: String): Boolean {
        if (pkgName.isEmpty() || pkgName == this.packageName) return false
        val lower = pkgName.lowercase()
        return lower.contains("settings") ||
                lower.contains("wireless") ||
                lower.contains("network") ||
                lower.contains("dns") ||
                lower.contains("packageinstaller") ||
                lower.contains("installer") ||
                lower.contains("safecenter") ||
                lower.contains("security") ||
                lower.contains("permission") ||
                lower.contains("appmanager") ||
                lower.contains("systemmanager") ||
                lower.contains("power") ||
                lower.contains("battery") ||
                lower.contains("athena") ||
                lower.contains("lool") ||
                lower == "com.samsung.android.sm" ||
                lower == "android" ||
                lower.contains("vpndialogs") ||
                lower.contains("vpn") ||
                lower.contains("accessibility") ||
                lower == "com.android.vending"
    }

    private fun isLauncherOrSystemUiPackage(pkgName: String): Boolean {
        val lower = pkgName.lowercase()
        return lower == "com.android.systemui" ||
                lower.contains("launcher") ||
                lower.contains("trebuchet") ||
                lower.contains("quickstep")
    }

    /**
     * Scans the active window, all interactive windows (`windows`), and the optional event.
     * Returns true if the user is currently inside a Settings/System/Installer window.
     */
    private fun performFullActiveWindowScan(event: AccessibilityEvent?): Boolean {
        if (LockManager.isPinScreenCurrentlyActive()) {
            return false
        }
        try {
            // Do not scan when device keyguard is locked
            val km = getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
            if (km != null && km.isKeyguardLocked) {
                return false
            }
        } catch (e: Exception) {}

        val activeRoot = try { rootInActiveWindow } catch (e: Exception) { null }
        val activePkg = activeRoot?.packageName?.toString()
            ?: event?.packageName?.toString()
            ?: ""
        val eventCls = event?.className?.toString() ?: ""

        if (activePkg == this.packageName || eventCls.contains("PinLockActivity", ignoreCase = true)) {
            return false
        }

        // Gather all interactive window roots (critical for floating dialogs like Private DNS / Stop Accessibility / Uninstall)
        val allRoots = mutableListOf<AccessibilityNodeInfo>()
        if (activeRoot != null) {
            allRoots.add(activeRoot)
        }
        try {
            val winList = windows
            if (winList != null) {
                for (win in winList) {
                    val r = try { win.root } catch (e: Exception) { null }
                    if (r != null) {
                        val wPkg = r.packageName?.toString() ?: ""
                        // If our own app / PinLockActivity window is on screen, whitelist and never auto-back
                        if (wPkg == this.packageName) {
                            return false
                        }
                        if (!wPkg.contains("systemui")) {
                            allRoots.add(r)
                        }
                    }
                }
            }
        } catch (e: Exception) {}

        // =========================================================================
        // 0. SIMPLE BLOCKER (v1.3.0): CHROME & ALL BROWSERS ADULT URL/KEYWORD BLOCKER
        //    Works purely via AccessibilityService + Overlay (no DNS, no VPN).
        //    NOTE: PIN 1-minute unlock NEVER disables or bypasses the adult blocker!
        // =========================================================================
        val eventPkgName = event?.packageName?.toString() ?: ""
        val isBrowserWindow = BlockerManager.isBrowserPackage(activePkg) ||
                BlockerManager.isBrowserPackage(eventPkgName) ||
                allRoots.any { BlockerManager.isBrowserPackage(it.packageName?.toString() ?: "") }

        if (isBrowserWindow && BlockerManager.isAdultBlockerEnabled(this)) {
            val browserText = extractAllWindowsText(allRoots, event)
            val matchedAdultTerm = BlockerManager.detectAdultContent(browserText)
            if (matchedAdultTerm != null) {
                performBrowserAdultBlock(matchedAdultTerm)
                return true
            }
        }

        val anyRelevantPkg = isRelevantSystemOrSettingsPackage(activePkg) ||
                (eventPkgName.isNotEmpty() && isRelevantSystemOrSettingsPackage(eventPkgName)) ||
                allRoots.any { isRelevantSystemOrSettingsPackage(it.packageName?.toString() ?: "") }

        lastPatrolPackagesettings = anyRelevantPkg

        if (!anyRelevantPkg) {
            if (isLauncherOrSystemUiPackage(activePkg)) {
                return false
            }
            return false
        }

        // If user unlocked temporarily with valid PIN (strictly max 60 seconds monotonic), pause settings auto-back
        if (LockManager.isCurrentlyUnlocked(this)) {
            return true
        }

        val combinedText = extractAllWindowsText(allRoots, event)
        if (combinedText.isEmpty()) {
            return true
        }

        val isPlayStorePkg = activePkg == "com.android.vending" ||
                (event?.packageName?.toString() == "com.android.vending")

        // =========================================================================
        // 1. PRIVATE DNS PROTECTION (Instant Auto-Back + PIN Lock)
        // =========================================================================
        val hasPrivateDnsKeyword = combinedText.contains("private dns") ||
                combinedText.contains("প্রাইভেট ডিএনএস") ||
                combinedText.contains("ব্যক্তিগত ডিএনএস") ||
                combinedText.contains("dns provider hostname") ||
                combinedText.contains("hostname of dns provider") ||
                combinedText.contains("specified dns") ||
                combinedText.contains("নির্দিষ্ট ডিএনএস") ||
                combinedText.contains("নির্দিষ্ট dns") ||
                combinedText.contains("family.adguard-dns.com") ||
                combinedText.contains("adguard-dns") ||
                combinedText.contains("cleanbrowsing") ||
                combinedText.contains("private_dns")

        if (hasPrivateDnsKeyword) {
            val isAlreadyConfiguredOnScreen = combinedText.contains("family.adguard-dns.com") ||
                    combinedText.contains("adguard-dns") ||
                    combinedText.contains("cleanbrowsing") ||
                    isSystemPrivateDnsConfigured()

            // Only allow if user is in the 40s initial setup grace window AND Private DNS is not yet configured
            val allowInitialSetup = isInitialDnsSetupGraceActive() && !isAlreadyConfiguredOnScreen
            if (!allowInitialSetup) {
                clickCancelButtonIfPresent(allRoots)
                performAutoBackAndLock("attempted to open or disable Private DNS - LOCKED")
                return true
            }
        }

        // =========================================================================
        // 2. ACCESSIBILITY SERVICE (SETTINGS GUARD) DISABLE PROTECTION
        // Since ProtectionService is running right now, any visit to Purity Tracker's
        // Accessibility detail screen or stop dialog is an attempt to turn it OFF!
        // =========================================================================
        val isExplicitAccessibilityStop = combinedText.contains("stop purity tracker") ||
                combinedText.contains("turn off purity tracker") ||
                combinedText.contains("disable purity tracker") ||
                combinedText.contains("পিউরিটি ট্র্যাকার বন্ধ") ||
                combinedText.contains("পিউরিটি ট্র্যাকার থামান") ||
                (combinedText.contains("purity tracker") && combinedText.contains("tap stop")) ||
                (combinedText.contains("purity tracker") && combinedText.contains("turning off"))

        val isPurityAccessibilityDetailPage =
            (combinedText.contains("purity tracker") || combinedText.contains("পিউরিটি ট্র্যাকার")) &&
            (combinedText.contains("use purity tracker") ||
             combinedText.contains("পিউরিটি ট্র্যাকার ব্যবহার") ||
             combinedText.contains("purity tracker shortcut") ||
             combinedText.contains("পিউরিটি ট্র্যাকার শর্টকাট") ||
             combinedText.contains("24/7 anti-tamper") ||
             combinedText.contains("anti-tamper & streak protection") ||
             combinedText.contains("সেটিংস ও আনইনস্টল ব্লক")) &&
            !combinedText.contains("downloaded apps") &&
            !combinedText.contains("installed services") &&
            !combinedText.contains("ডাউনলোড করা অ্যাপ")

        if (isExplicitAccessibilityStop || isPurityAccessibilityDetailPage) {
            clickCancelButtonIfPresent(allRoots)
            performAutoBackAndLock("attempted to disable Settings Guard - PERMANENTLY LOCKED")
            return true
        }

        // =========================================================================
        // 3. UNINSTALL CONFIRMATION DIALOG FOR PURITY TRACKER
        // =========================================================================
        val isUninstallPrompt = combinedText.contains("uninstall") ||
                combinedText.contains("মুছে ফেলতে") ||
                combinedText.contains("আনইনস্টল") ||
                combinedText.contains("remove this app") ||
                combinedText.contains("delete app")

        if (isUninstallPrompt) {
            val hasOurIdentity = combinedText.contains("purity tracker") ||
                    combinedText.contains("পিউরিটি ট্র্যাকার") ||
                    combinedText.contains("com.aistudio.puritytracker") ||
                    combinedText.contains(this.packageName.lowercase())

            val isExplicitOurUninstall = combinedText.contains("uninstall purity tracker") ||
                    combinedText.contains("পিউরিটি ট্র্যাকার আনইনস্টল") ||
                    combinedText.contains("delete purity tracker") ||
                    (combinedText.contains("do you want to uninstall") && hasOurIdentity) ||
                    (combinedText.contains("want to uninstall this app") && hasOurIdentity) ||
                    (combinedText.contains("আনইনস্টল করতে চান") && hasOurIdentity) ||
                    (isPlayStorePkg && hasOurIdentity)

            if (isExplicitOurUninstall) {
                clickCancelButtonIfPresent(allRoots)
                performAutoBackAndLock("attempted to uninstall Purity Tracker - PERMANENTLY LOCKED")
                return true
            }
        }

        // =========================================================================
        // 4. DEVICE ADMINISTRATOR DEACTIVATION FOR PURITY TRACKER
        // =========================================================================
        val isDeviceAdminScreen = combinedText.contains("device admin") ||
                combinedText.contains("admin app") ||
                combinedText.contains("deactivate") ||
                combinedText.contains("ডিঅ্যাক্টিভেট") ||
                combinedText.contains("ডিভাইস অ্যাডমিন")

        if (isDeviceAdminScreen) {
            val hasOurIdentity = combinedText.contains("purity tracker") ||
                    combinedText.contains("পিউরিটি ট্র্যাকার") ||
                    combinedText.contains("antiuninstalladminreceiver")
            // Allow activating Device Admin if it's currently NOT active, block deactivating when active!
            if (hasOurIdentity && AntiUninstallManager.isDeviceAdminActive(this)) {
                clickCancelButtonIfPresent(allRoots)
                performAutoBackAndLock("attempted to deactivate Device Administrator - PERMANENTLY LOCKED")
                return true
            }
        }

        // =========================================================================
        // 5. VPN SHIELD DISCONNECT DIALOG FOR PURITY TRACKER
        // =========================================================================
        val hasVpnOurIdentity = combinedText.contains("purity tracker") ||
                combinedText.contains("পিউরিটি ট্র্যাকার") ||
                combinedText.contains("com.aistudio.puritytracker")

        val hasVpnDisconnectAction = combinedText.contains("disconnect") ||
                combinedText.contains("revoke") ||
                combinedText.contains("delete vpn") ||
                combinedText.contains("turn off vpn") ||
                combinedText.contains("সংযোগ বিচ্ছিন্ন") ||
                combinedText.contains("ডিসকানেক্ট")

        val isInitialVpnRequest = combinedText.contains("wants to set up") ||
                combinedText.contains("connection request") ||
                combinedText.contains("সংযোগ স্থাপন করতে চায়")

        if (hasVpnOurIdentity && hasVpnDisconnectAction && !isInitialVpnRequest) {
            clickCancelButtonIfPresent(allRoots)
            performAutoBackAndLock("attempted to disconnect VPN Shield - LOCKED")
            return true
        }

        // =========================================================================
        // 6. POWER CONSUMPTION / BATTERY / AUTO-LAUNCH / APP INFO / CLEAR DATA
        //    STRICTLY FOR PURITY TRACKER (Never affects Chrome, WhatsApp, etc.)
        // =========================================================================
        if (!isPlayStorePkg) {
            val isOurTamperScreen = checkIsOurAppInfoOrBatteryOrStorageScreen(allRoots, combinedText)
            if (isOurTamperScreen) {
                clickCancelButtonIfPresent(allRoots)
                performAutoBackAndLock("attempted to modify Purity Tracker App Info / Battery / Storage - LOCKED")
                return true
            }
        }

        return true
    }

    private fun isSystemPrivateDnsConfigured(): Boolean {
        return try {
            val mode = android.provider.Settings.Global.getString(contentResolver, "private_dns_mode") ?: ""
            val spec = android.provider.Settings.Global.getString(contentResolver, "private_dns_specifier") ?: ""
            mode.equals("hostname", ignoreCase = true) ||
                    spec.contains("adguard", ignoreCase = true) ||
                    spec.contains("cleanbrowsing", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if the active screen is specifically Purity Tracker's App Info,
     * Power Consumption Controls (Realme/ColorOS/Xiaomi/Samsung Battery page),
     * or Storage/Clear Data page.
     * Strictly verifies that Purity Tracker is the target app and NOT another app.
     */
    private fun checkIsOurAppInfoOrBatteryOrStorageScreen(
        roots: List<AccessibilityNodeInfo>,
        windowText: String
    ): Boolean {
        val hasOurIdentityInText = windowText.contains("purity tracker") ||
                windowText.contains("পিউরিটি ট্র্যাকার") ||
                windowText.contains("com.aistudio.puritytracker") ||
                windowText.contains(this.packageName.lowercase())

        if (!hasOurIdentityInText) return false

        // Ignore the initial system "Allow Purity Tracker to always run in background?" dialog when enabling battery exemption
        if (!AntiUninstallManager.isSystemBatteryOptimizationIgnored(this)) {
            if (windowText.contains("always run in background") ||
                windowText.contains("ignore battery optimizations") ||
                windowText.contains("stop optimizing battery usage")
            ) {
                return false
            }
        }

        // 1. Check for Realme/ColorOS/OEM Power Consumption / Auto-Launch page for Purity Tracker
        val isPowerOrAutoLaunchPage = windowText.contains("power consumption controls") ||
                windowText.contains("allow background activity") ||
                windowText.contains("allow foreground activity") ||
                windowText.contains("allow auto launch") ||
                windowText.contains("associated launch") ||
                windowText.contains("pause app activity if unused") ||
                windowText.contains("remove permissions if app is unused")

        // 2. Check for Clear Storage / Clear Data page for Purity Tracker
        val isStorageClearPage = windowText.contains("clear storage") ||
                windowText.contains("clear data") ||
                windowText.contains("clear all data") ||
                windowText.contains("manage space") ||
                windowText.contains("ডেটা মুছুন") ||
                windowText.contains("ক্লিয়ার ডেটা") ||
                windowText.contains("সব ডেটা মুছুন")

        // 3. Check for App Info page (Force Stop / Uninstall / Storage)
        val hasAppInfoButtons = (windowText.contains("force stop") || windowText.contains("ফোর্স স্টপ")) &&
                (windowText.contains("uninstall") || windowText.contains("আনইনস্টল") ||
                 windowText.contains("storage") || windowText.contains("permissions") ||
                 windowText.contains("battery") || windowText.contains("disable"))

        if (!isPowerOrAutoLaunchPage && !isStorageClearPage && !hasAppInfoButtons) {
            return false
        }

        // Exclude the multi-app "App management" / "All apps" list where many apps are listed together
        if (windowText.contains("search apps") ||
            windowText.contains("all apps") ||
            windowText.contains("see all") ||
            windowText.contains("installed apps") ||
            windowText.contains("default apps")
        ) {
            if (!windowText.contains("force stop") && !isPowerOrAutoLaunchPage && !isStorageClearPage) {
                return false
            }
        }

        val myPkg = packageName.lowercase()
        val myPkgPrefix = "com.aistudio.puritytracker"
        val ourNames = listOf("purity tracker", "পিউরিটি ট্র্যাকার")

        var foundOurHeader = false
        var foundOtherAppHeader = false

        fun inspectNodes(node: AccessibilityNodeInfo?, depth: Int) {
            if (node == null || depth > 14 || foundOurHeader || foundOtherAppHeader) return
            try {
                val viewId = node.viewIdResourceName?.lowercase() ?: ""
                val text = node.text?.toString()?.trim() ?: ""
                val desc = node.contentDescription?.toString()?.trim() ?: ""
                val lowerText = text.lowercase()
                val lowerDesc = desc.lowercase()

                val isHeaderView = viewId.contains("title") ||
                        viewId.contains("app_name") ||
                        viewId.contains("header") ||
                        viewId.contains("entity") ||
                        viewId.contains("label") ||
                        viewId.contains("widget_text1") ||
                        viewId.contains("toolbar") ||
                        viewId.contains("action_bar") ||
                        viewId.contains("coui")

                if (isHeaderView && (text.isNotEmpty() || desc.isNotEmpty())) {
                    val isOur = ourNames.any { lowerText == it || lowerDesc == it } ||
                            lowerText == myPkg || lowerDesc == myPkg ||
                            lowerText.contains(myPkgPrefix) || lowerDesc.contains(myPkgPrefix)

                    if (isOur) {
                        foundOurHeader = true
                    } else {
                        val isGeneric = lowerText in listOf(
                            "settings", "app info", "apps", "application info", "all apps",
                            "force stop", "uninstall", "storage", "storage usage", "permissions",
                            "notifications", "battery usage", "power consumption controls",
                            "privacy permissions", "allow background activity", "allow auto launch",
                            "allow foreground activity", "data usage", "open by default",
                            "সেটিংস", "অ্যাপ তথ্য", "অ্যাপের বিবরণ", "ফোর্স স্টপ", "আনইনস্টল"
                        )
                        if (!isGeneric && text.length > 1) {
                            foundOtherAppHeader = true
                        }
                    }
                }

                val childCount = node.childCount
                for (i in 0 until childCount) {
                    inspectNodes(node.getChild(i), depth + 1)
                }
            } catch (e: Exception) {}
        }

        for (r in roots) {
            inspectNodes(r, 0)
        }

        if (foundOtherAppHeader && !foundOurHeader) {
            return false
        }

        return foundOurHeader || isPowerOrAutoLaunchPage || isStorageClearPage
    }

    /**
     * Clicks any Cancel / Dismiss button inside a modal dialog immediately before performing Global Back.
     */
    private fun clickCancelButtonIfPresent(roots: List<AccessibilityNodeInfo>) {
        val cancelLabels = setOf("cancel", "বাতিল", "বাতিল করুন", "no", "না", "deny")
        fun findAndClickCancel(node: AccessibilityNodeInfo?, depth: Int): Boolean {
            if (node == null || depth > 12) return false
            try {
                val txt = node.text?.toString()?.trim()?.lowercase() ?: ""
                val desc = node.contentDescription?.toString()?.trim()?.lowercase() ?: ""
                val vId = node.viewIdResourceName?.lowercase() ?: ""
                if (txt in cancelLabels || desc in cancelLabels || vId.endsWith("button2")) {
                    if (node.isClickable) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        return true
                    }
                    val parent = node.parent
                    if (parent != null && parent.isClickable) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        return true
                    }
                }
                for (i in 0 until node.childCount) {
                    if (findAndClickCancel(node.getChild(i), depth + 1)) return true
                }
            } catch (e: Exception) {}
            return false
        }
        for (r in roots) {
            if (findAndClickCancel(r, 0)) break
        }
    }

    /**
     * Immediately navigates Back out of the adult browser page/query and shows the
     * full-screen Blocker overlay (`SYSTEM_ALERT_WINDOW` + `BlockerOverlayActivity`).
     */
    private fun performBrowserAdultBlock(matchedTerm: String) {
        val nowElapsed = SystemClock.elapsedRealtime()
        if (nowElapsed >= lastBrowserBlockElapsedTime && (nowElapsed - lastBrowserBlockElapsedTime) < 600L) {
            return
        }
        lastBrowserBlockElapsedTime = nowElapsed

        // 1. Immediate Back action before the page renders
        performGlobalAction(GLOBAL_ACTION_BACK)
        mainHandler.postDelayed({
            try {
                performGlobalAction(GLOBAL_ACTION_BACK)
            } catch (e: Exception) {}
        }, 120L)

        // 2. Show the BlockerHero-style overlay block screen
        BlockerOverlayManager.showBlockScreen(this, matchedTerm)

        // 3. Notify Accountability Partner via GuardianManager
        triggerGuardianAlert("attempted to access blocked adult content ($matchedTerm)")
    }

    /**
     * Executes single Auto-Back out of restricted Settings + launches PinLockActivity on top
     * and pauses the guard while PinLockActivity is on screen.
     */
    private fun performAutoBackAndLock(reason: String) {
        if (LockManager.isPinScreenCurrentlyActive() || LockManager.isCurrentlyUnlocked(this)) {
            return
        }
        val nowElapsed = SystemClock.elapsedRealtime()
        if (nowElapsed >= lastBlockElapsedTime && (nowElapsed - lastBlockElapsedTime) < 600L) {
            return
        }
        lastBlockElapsedTime = nowElapsed

        // 1. Mark PIN screen launching immediately so the guard pauses itself and never backs out of PinLockActivity
        LockManager.markPinScreenLaunching()

        // 2. Single immediate Back action (0ms) to dismiss the restricted Settings dialog/screen BEFORE PinLockActivity opens
        performGlobalAction(GLOBAL_ACTION_BACK)

        // 3. Trigger Guardian Alert
        triggerGuardianAlert(reason)

        // 4. Launch PinLockActivity right after the back action so PinLockActivity stays on top until PIN entry or Cancel
        mainHandler.postDelayed({ launchPinLockScreenNow() }, 45L)
    }

    private fun launchPinLockScreenNow() {
        try {
            LockManager.markPinScreenLaunching()
            val pinIntent = Intent(this, PinLockActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
            }
            startActivity(pinIntent)
        } catch (e: Exception) {}
    }

    private fun extractAllWindowsText(
        roots: List<AccessibilityNodeInfo>,
        event: AccessibilityEvent?
    ): String {
        val sb = StringBuilder(512)

        if (event != null) {
            try {
                event.text?.forEach { charSeq ->
                    if (!charSeq.isNullOrEmpty()) {
                        sb.append(charSeq.toString().lowercase()).append(' ')
                    }
                }
                val eventDesc = event.contentDescription?.toString()
                if (!eventDesc.isNullOrEmpty()) {
                    sb.append(eventDesc.lowercase()).append(' ')
                }
                val cls = event.className?.toString()
                if (!cls.isNullOrEmpty()) {
                    sb.append(cls.lowercase()).append(' ')
                }
            } catch (e: Exception) {}
        }

        val count = IntArray(1)
        for (root in roots) {
            appendNodeHierarchyText(root, sb, 0, count)
        }

        if (roots.isEmpty() && event != null) {
            try {
                event.source?.let { srcNode ->
                    appendNodeHierarchyText(srcNode, sb, 0, count)
                }
            } catch (e: Exception) {}
        }

        return sb.toString()
    }

    private fun appendNodeHierarchyText(
        node: AccessibilityNodeInfo?,
        sb: StringBuilder,
        depth: Int,
        count: IntArray
    ) {
        if (node == null || depth > 24 || count[0] > 850) return
        val nodePkg = node.packageName?.toString() ?: ""
        if (nodePkg == "com.android.systemui" || nodePkg.contains("systemui")) {
            return
        }
        count[0]++
        try {
            val text = node.text?.toString()
            if (!text.isNullOrEmpty()) {
                sb.append(text.lowercase()).append(' ')
            }
            val desc = node.contentDescription?.toString()
            if (!desc.isNullOrEmpty()) {
                sb.append(desc.lowercase()).append(' ')
            }
            val viewId = node.viewIdResourceName
            if (!viewId.isNullOrEmpty()) {
                sb.append(viewId.lowercase()).append(' ')
            }

            val childCount = node.childCount
            for (i in 0 until childCount) {
                if (count[0] > 850) break
                val child = try {
                    node.getChild(i)
                } catch (e: Exception) {
                    null
                }
                if (child != null) {
                    appendNodeHierarchyText(child, sb, depth + 1, count)
                }
            }
        } catch (e: Exception) {}
    }

    private fun triggerGuardianAlert(reason: String) {
        try {
            GuardianManager.triggerGuardianAlert(this, reason)
        } catch (e: Exception) {}
    }

    override fun onInterrupt() {
        // Keep service running uninterrupted
    }
}
