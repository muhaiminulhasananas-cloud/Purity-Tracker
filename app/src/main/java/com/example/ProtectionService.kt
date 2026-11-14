package com.example

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color as AndroidColor
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.text.InputFilter
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/**
 * ProtectionService: 24/7 Real-Time Android Accessibility Service for Purity Tracker.
 *
 * Protects ONLY Purity Tracker (`com.aistudio.puritytracker.abcxyz` / "Purity Tracker"):
 * 1. System App info screen for Purity Tracker (ONLY after entering the Settings App info page,
 *    never on a launcher shortcut/long-press popup menu).
 * 2. Uninstall confirmation popups for Purity Tracker (from home-screen long-press → Uninstall
 *    or App info → Uninstall, across all launchers and package installers).
 * 3. Accessibility service stop/disable screen for Purity Tracker.
 * 4. Device Admin deactivation screen for Purity Tracker.
 * 5. Adult content in browsers when the blocker is ON.
 *
 * Never blocks Settings in general, launcher long-press menus, other apps' App info,
 * or other apps' uninstall dialogs.
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

        fun isPinOverlayCurrentlyShowing(): Boolean {
            return try {
                instance?.isPinOverlayActuallyShowing() == true
            } catch (e: Exception) {
                false
            }
        }

        fun dismissAccessibilityPinOverlayIfPresent() {
            try {
                instance?.dismissPinOverlayInternal()
            } catch (e: Exception) {}
        }

        /**
         * Normalizes text for matching across different Android brands and locales:
         * lowercases, strips straight/smart quotes, and collapses punctuation/whitespace.
         */
        fun normalizeForMatch(raw: String): String {
            if (raw.isEmpty()) return ""
            val sb = StringBuilder(raw.length)
            var lastWasSpace = true
            for (ch in raw.lowercase()) {
                when (ch) {
                    '"', '\'', '“', '”', '‘', '’', '`', '«', '»' -> {
                        // Strip quotes so 'Delete "Purity Tracker"?' becomes 'delete purity tracker'
                    }
                    '?', '!', ',', '.', ':', ';', '(', ')', '[', ']', '\n', '\r', '\t' -> {
                        if (!lastWasSpace) {
                            sb.append(' ')
                            lastWasSpace = true
                        }
                    }
                    ' ' -> {
                        if (!lastWasSpace) {
                            sb.append(' ')
                            lastWasSpace = true
                        }
                    }
                    else -> {
                        sb.append(ch)
                        lastWasSpace = false
                    }
                }
            }
            return sb.toString().trim()
        }

        /**
         * Returns true if the package name belongs to a Home Launcher, SystemUI, or IME keyboard.
         * A launcher package (including its long-press shortcut menu) must NEVER be treated
         * as the Settings App info page.
         */
        fun isLauncherOrSystemUiPackageName(
            pkgName: String,
            resolvedHomePackage: String = ""
        ): Boolean {
            if (pkgName.isEmpty()) return false
            val lower = pkgName.lowercase()
            if (resolvedHomePackage.isNotEmpty() && lower == resolvedHomePackage.lowercase()) {
                return true
            }
            return lower == "com.android.systemui" ||
                    lower.contains("systemui") ||
                    lower.contains("launcher") ||
                    lower.contains("trebuchet") ||
                    lower.contains("quickstep") ||
                    lower.contains("miui.home") ||
                    lower.contains("globallauncher") ||
                    lower.contains("oppo.launcher") ||
                    lower.contains("oplus.launcher") ||
                    lower.contains("homescreen") ||
                    lower.contains("inputmethod") ||
                    lower.contains("keyboard") ||
                    lower.contains("gboard")
        }

        /**
         * Returns true ONLY for Settings / App-info / System-manager packages.
         * Never returns true for launchers, popup menus in launchers, SystemUI, or browsers.
         */
        fun isSettingsOrAppInfoPackageName(
            pkgName: String,
            appPackageName: String = "com.aistudio.puritytracker.abcxyz",
            resolvedHomePackage: String = ""
        ): Boolean {
            if (pkgName.isEmpty()) return false
            val lower = pkgName.lowercase()
            if (lower == appPackageName.lowercase() ||
                isLauncherOrSystemUiPackageName(lower, resolvedHomePackage) ||
                BlockerManager.isBrowserPackage(lower)
            ) {
                return false
            }
            return lower.contains("settings") ||
                    lower.contains("appmanager") ||
                    lower.contains("applications") ||
                    lower.contains("appplatform") ||
                    lower.contains("appdetail") ||
                    lower.contains("installedapp") ||
                    lower.contains("safecenter") ||
                    lower.contains("securitycenter") ||
                    lower.contains("security") ||
                    lower.contains("systemmanager") ||
                    lower.contains("phonemanager") ||
                    lower.contains("mobilemanager") ||
                    lower.contains("power") ||
                    lower.contains("battery") ||
                    lower.contains("athena") ||
                    lower.contains("lool") ||
                    lower == "com.samsung.android.sm" ||
                    lower.contains("permission")
        }

        /**
         * Pure helper for unit-testing & runtime detection:
         * Determines whether a window's raw text represents an Uninstall / Delete confirmation
         * popup specifically for Purity Tracker (and NEVER for any other app or a launcher long-press menu).
         */
        fun isOurUninstallPopupText(
            rawWindowText: String,
            pkgName: String = "",
            appPackageName: String = "com.aistudio.puritytracker.abcxyz"
        ): Boolean {
            val lower = rawWindowText.lowercase()
            val norm = normalizeForMatch(rawWindowText)
            if (norm.isEmpty()) return false

            val hasOurIdentity = norm.contains("purity tracker") ||
                    lower.contains("পিউরিটি ট্র্যাকার") ||
                    lower.contains("com.aistudio.puritytracker") ||
                    lower.contains(appPackageName.lowercase())

            if (!hasOurIdentity) return false

            // 1. Explicit confirmation title/phrase naming Purity Tracker directly
            if (norm.contains("delete purity tracker") ||
                norm.contains("uninstall purity tracker") ||
                norm.contains("remove purity tracker") ||
                lower.contains("পিউরিটি ট্র্যাকার আনইনস্টল") ||
                lower.contains("পিউরিটি ট্র্যাকার মুছে") ||
                lower.contains("পিউরিটি ট্র্যাকার ডিলিট")
            ) {
                return true
            }

            // 2. Uninstall dialog body/prompt phrases when paired with Purity Tracker's identity
            val hasUninstallDialogPhrase =
                norm.contains("after uninstallation") ||
                norm.contains("all app data will be cleared") ||
                norm.contains("all data will be deleted") ||
                norm.contains("app data will be deleted") ||
                norm.contains("also delete its data") ||
                norm.contains("delete its data") ||
                norm.contains("deleting this app") ||
                norm.contains("do you want to uninstall") ||
                norm.contains("want to uninstall this app") ||
                norm.contains("uninstall this app") ||
                norm.contains("do you want to delete") ||
                norm.contains("want to delete this app") ||
                norm.contains("delete this app") ||
                norm.contains("remove this app") ||
                norm.contains("will be uninstalled") ||
                (norm.contains("keep ") && norm.contains(" of app data")) ||
                lower.contains("আনইনস্টল করতে চান") ||
                lower.contains("মুছে ফেলতে চান") ||
                lower.contains("ডেটাও মুছে যাবে")

            if (hasUninstallDialogPhrase) {
                return true
            }

            // 3. Dedicated packageinstaller / uninstaller window for Purity Tracker
            val lowerPkg = pkgName.lowercase()
            if ((lowerPkg.contains("packageinstaller") || lowerPkg.contains("uninstaller")) &&
                (norm.contains("uninstall") || norm.contains("delete") || lower.contains("আনইনস্টল") || lower.contains("মুছে"))
            ) {
                return true
            }

            return false
        }

        /**
         * Pure helper for unit-testing & runtime detection:
         * Determines whether a Settings window's text represents the actual App Info page,
         * Battery Usage sub-page, or Storage / Clear Data sub-page specifically for Purity Tracker.
         *
         * Strictly requires:
         * (a) Not a launcher package (so launcher long-press menus with "App info" + "Uninstall" NEVER trigger).
         * (b) App-info-page-only content for Purity Tracker (such as "Version 1.3.x" + page rows like
         *     "Force stop" / "Storage usage" / "Open", or "Force stop" + App info rows, or "Clear data"/"Clear storage").
         *     Generic words like "storage", "battery", "permissions", "notifications", or "App info" + "Uninstall"
         *     alone NEVER trigger the guard.
         */
        fun isOurAppInfoOrTamperText(
            rawWindowText: String,
            appPackageName: String = "com.aistudio.puritytracker.abcxyz",
            pkgName: String = ""
        ): Boolean {
            if (pkgName.isNotEmpty() && isLauncherOrSystemUiPackageName(pkgName)) {
                return false
            }

            val lower = rawWindowText.lowercase()
            val norm = normalizeForMatch(rawWindowText)
            if (norm.isEmpty()) return false

            val hasOurIdentity = norm.contains("purity tracker") ||
                    lower.contains("পিউরিটি ট্র্যাকার") ||
                    lower.contains("com.aistudio.puritytracker") ||
                    lower.contains(appPackageName.lowercase())

            if (!hasOurIdentity) return false

            // Single-app detail markers that ONLY appear inside the actual Settings App Info / Storage / Battery page
            // and NEVER appear on a launcher long-press shortcut menu or multi-app list
            val hasForceStop = norm.contains("force stop") ||
                    lower.contains("ফোর্স স্টপ") ||
                    norm.contains("force quit")

            val hasVersionLabel = Regex("\\bversion\\s+[0-9]").containsMatchIn(norm) ||
                    norm.contains("version 1") ||
                    lower.contains("সংস্করণ")

            val hasClearStorage = norm.contains("clear storage") ||
                    norm.contains("clear data") ||
                    norm.contains("clear all data") ||
                    norm.contains("clear cache") ||
                    norm.contains("manage space") ||
                    lower.contains("ডেটা মুছুন") ||
                    lower.contains("ক্লিয়ার ডেটা") ||
                    lower.contains("সব ডেটা মুছুন")

            val hasPowerControls = norm.contains("power consumption controls") ||
                    norm.contains("allow background activity") ||
                    norm.contains("allow foreground activity") ||
                    norm.contains("allow auto launch") ||
                    norm.contains("associated launch") ||
                    norm.contains("pause app activity if unused") ||
                    norm.contains("remove permissions if app is unused")

            val hasAppInfoHeader = norm.contains("app info") ||
                    norm.contains("application info") ||
                    lower.contains("অ্যাপ তথ্য") ||
                    lower.contains("অ্যাপের বিবরণ")

            // Specific App-info page rows (never matched by a simple launcher menu containing only "App info" + "Uninstall")
            val hasSpecificAppInfoRows =
                    norm.contains("storage usage") ||
                    norm.contains("storage & cache") ||
                    norm.contains("storage and cache") ||
                    norm.contains("data usage") ||
                    norm.contains("battery usage") ||
                    norm.contains("manage notifications") ||
                    norm.contains("display over other apps") ||
                    norm.contains("appear on top") ||
                    norm.contains("open by default") ||
                    norm.contains("set as default") ||
                    lower.contains("স্টোরেজ ব্যবহার") ||
                    lower.contains("ব্যাটারি ব্যবহার")

            val isMultiAppListOnly = (norm.contains("search apps") ||
                    norm.contains("all apps") ||
                    norm.contains("installed apps") ||
                    norm.contains("app list")) &&
                    !hasForceStop && !hasVersionLabel && !hasClearStorage && !hasPowerControls

            if (isMultiAppListOnly) return false

            // 1. Actual App Info page: requires Version label + (Force stop or page rows or Open/Uninstall)
            //    OR Force stop + (Version label or App info header or specific App info rows)
            val isFullAppInfoPage =
                (hasVersionLabel && (hasForceStop || hasSpecificAppInfoRows || norm.contains("open") || norm.contains("uninstall"))) ||
                (hasForceStop && (hasVersionLabel || hasAppInfoHeader || hasSpecificAppInfoRows || norm.contains("storage") || norm.contains("permissions")))

            // 2. Storage / Clear data sub-page or Battery / Power controls sub-page for Purity Tracker
            return isFullAppInfoPage || hasClearStorage || hasPowerControls
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var screenReceiver: BroadcastReceiver? = null
    private var pinOverlayView: View? = null

    @Volatile
    private var cachedHomeLauncherPkg: String = ""

    private var lastBlockElapsedTime: Long = 0L
    private var lastBrowserBlockElapsedTime: Long = 0L

    private val followUpScanRunnable = Runnable {
        try {
            if (!LockManager.isPinScreenCurrentlyActive() && !isPinOverlayActuallyShowing()) {
                performFullActiveWindowScan(null)
            }
        } catch (e: Exception) {}
    }

    /**
     * Delayed PIN presentation runnable (runs ~220ms after GLOBAL_ACTION_BACK so the
     * Settings App info exit animation / Back dispatch finishes before the PIN screen appears).
     */
    private val presentPinRunnable = Runnable {
        try {
            if (!LockManager.isCurrentlyUnlocked(this)) {
                presentPinScreenGuaranteed()
            }
        } catch (e: Exception) {}
    }

    /**
     * ~520ms follow-up check after a block:
     * 1. If the screen underneath is STILL Purity Tracker's App info or Uninstall popup, backs out again.
     * 2. If the PIN screen is not visible yet, launches it again so no block ever swallows the PIN prompt.
     */
    private val followUpVerifyPinRunnable = Runnable {
        try {
            if (LockManager.isCurrentlyUnlocked(this)) return@Runnable

            // Check if the underlying window is still Purity Tracker's restricted screen
            if (!isPinOverlayActuallyShowing() && !LockManager.isPinActivityVisible()) {
                val activeRoot = try { rootInActiveWindow } catch (e: Exception) { null }
                val activePkg = activeRoot?.packageName?.toString() ?: ""
                val roots = mutableListOf<AccessibilityNodeInfo>()
                if (activeRoot != null) roots.add(activeRoot)
                try {
                    windows?.forEach { w ->
                        val r = try { w.root } catch (e: Exception) { null }
                        if (r != null && r.packageName?.toString() != this.packageName) {
                            roots.add(r)
                        }
                    }
                } catch (e: Exception) {}

                val stillOnRestrictedScreen =
                    checkIsOurUninstallPopup(roots, null, activePkg) ||
                    checkIsOurAppInfoOrBatteryOrStorageScreen(roots, activePkg, null)

                if (stillOnRestrictedScreen) {
                    clickCancelButtonIfPresent(roots)
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    mainHandler.postDelayed({
                        if (!LockManager.isCurrentlyUnlocked(this@ProtectionService)) {
                            presentPinScreenGuaranteed()
                        }
                    }, 200L)
                    return@Runnable
                }
            }

            if (!isPinOverlayActuallyShowing() && !LockManager.isPinActivityVisible()) {
                presentPinScreenGuaranteed()
            }
        } catch (e: Exception) {}
    }

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

    // 450ms active window patrol while screen is interactive
    private val backupPatrolRunnable = object : Runnable {
        override fun run() {
            try {
                recordAliveHeartbeat()
                val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
                val isScreenOn = pm?.isInteractive ?: true
                if (isScreenOn && !LockManager.isPinScreenCurrentlyActive() && !isPinOverlayActuallyShowing()) {
                    performFullActiveWindowScan(null)
                }
            } catch (e: Exception) {}
            mainHandler.postDelayed(this, 450L)
        }
    }

    private fun resolveHomeLauncherPkg(): String {
        if (cachedHomeLauncherPkg.isNotEmpty()) return cachedHomeLauncherPkg
        return try {
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = packageManager.resolveActivity(homeIntent, 0)
            val pkg = resolveInfo?.activityInfo?.packageName.orEmpty()
            if (pkg.isNotEmpty() && pkg != "android") {
                cachedHomeLauncherPkg = pkg
            }
            pkg
        } catch (e: Exception) {
            ""
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
        resolveHomeLauncherPkg()
        recordAliveHeartbeat()
        ensureServiceConfig()
        registerScreenReceiver()
        GuardHealthWorker.schedulePeriodicWork(this)
        ensureBackupPatrolRunning()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        resolveHomeLauncherPkg()
        recordAliveHeartbeat()

        ensureServiceConfig()
        registerScreenReceiver()
        GuardHealthWorker.schedulePeriodicWork(this)

        mainHandler.removeCallbacks(watchdogRunnable)
        mainHandler.post(watchdogRunnable)
        ensureBackupPatrolRunning()
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
    }

    private fun ensureBackupPatrolRunning() {
        mainHandler.removeCallbacks(backupPatrolRunnable)
        mainHandler.post(backupPatrolRunnable)
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
        dismissPinOverlayInternal()
        instance = null
        try {
            screenReceiver?.let {
                unregisterReceiver(it)
                screenReceiver = null
            }
            mainHandler.removeCallbacksAndMessages(null)
            GuardHealthWorker.schedulePeriodicWork(this)
            GuardHealthWorker.checkNowAndNotify(this)
        } catch (e: Exception) {}
        return true
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        instance = this
        resolveHomeLauncherPkg()
        recordAliveHeartbeat()
        ensureServiceConfig()
        registerScreenReceiver()
        mainHandler.removeCallbacks(watchdogRunnable)
        mainHandler.post(watchdogRunnable)
        ensureBackupPatrolRunning()
    }

    override fun onDestroy() {
        isServiceRunning = false
        dismissPinOverlayInternal()
        instance = null
        try {
            screenReceiver?.let {
                unregisterReceiver(it)
                screenReceiver = null
            }
            mainHandler.removeCallbacksAndMessages(null)
            GuardHealthWorker.schedulePeriodicWork(this)
            GuardHealthWorker.checkNowAndNotify(this)
        } catch (e: Exception) {}
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        recordAliveHeartbeat()
        try {
            if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                return
            }
            val eventPkg = event.packageName?.toString() ?: ""
            val eventCls = event.className?.toString() ?: ""
            if (eventPkg == this.packageName || eventCls.contains("PinLockActivity", ignoreCase = true)) {
                return
            }
            if (LockManager.isPinScreenCurrentlyActive() || isPinOverlayActuallyShowing()) {
                return
            }

            performFullActiveWindowScan(event)

            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            ) {
                mainHandler.removeCallbacks(followUpScanRunnable)
                mainHandler.postDelayed(followUpScanRunnable, 140L)
            }
        } catch (t: Throwable) {
            // Never crash onAccessibilityEvent
        }
    }

    private fun isLauncherOrSystemUiPackage(pkgName: String): Boolean {
        return isLauncherOrSystemUiPackageName(pkgName, resolveHomeLauncherPkg())
    }

    private fun isSettingsOrAppManagementPackage(pkgName: String): Boolean {
        return isSettingsOrAppInfoPackageName(pkgName, this.packageName, resolveHomeLauncherPkg())
    }

    private fun isRelevantSystemOrSettingsPackage(pkgName: String): Boolean {
        if (pkgName.isEmpty() || pkgName == this.packageName) return false
        if (isLauncherOrSystemUiPackage(pkgName) || BlockerManager.isBrowserPackage(pkgName)) {
            return false
        }
        val lower = pkgName.lowercase()
        if (isSettingsOrAppManagementPackage(lower) ||
            lower.contains("wireless") ||
            lower.contains("network") ||
            lower.contains("dns") ||
            lower.contains("packageinstaller") ||
            lower.contains("uninstaller") ||
            lower.contains("installer") ||
            lower == "android" ||
            lower.contains("vpndialogs") ||
            lower.contains("vpn") ||
            lower.contains("accessibility") ||
            lower == "com.android.vending"
        ) {
            return true
        }
        return false
    }

    /**
     * Scans the active window, all interactive windows (`windows`), and the optional event.
     */
    private fun performFullActiveWindowScan(event: AccessibilityEvent?): Boolean {
        if (LockManager.isPinScreenCurrentlyActive() || isPinOverlayActuallyShowing()) {
            return false
        }
        try {
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

        // Gather all interactive window roots (critical for floating dialogs like Uninstall / Stop Accessibility)
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
                        // If our own app / PinLockActivity / overlay window is on screen, whitelist and never auto-back
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
        // 0. SIMPLE BLOCKER: CHROME & ALL BROWSERS ADULT URL/KEYWORD BLOCKER
        //    NOTE: PIN 60-second unlock NEVER disables or bypasses the adult blocker!
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

        // If user unlocked temporarily with valid partner PIN (max 60 seconds), pause guard for Settings / Uninstall
        if (LockManager.isCurrentlyUnlocked(this)) {
            return true
        }

        // =========================================================================
        // 1. UNINSTALL CONFIRMATION POPUP FOR PURITY TRACKER (ALL PACKAGES & LAUNCHERS)
        //    Covers BOTH entry points on every Android phone:
        //    - Home-screen long-press → Uninstall ("Delete Purity Tracker" / "Uninstall Purity Tracker")
        //    - App info → Uninstall
        //    Strictly scoped ONLY to Purity Tracker; ignores launcher long-press menus and other apps.
        // =========================================================================
        if (checkIsOurUninstallPopup(allRoots, event, activePkg)) {
            clickCancelButtonIfPresent(allRoots)
            performAutoBackAndLock("attempted to uninstall Purity Tracker - PIN REQUIRED")
            return true
        }

        val anyRelevantPkg = isRelevantSystemOrSettingsPackage(activePkg) ||
                (eventPkgName.isNotEmpty() && isRelevantSystemOrSettingsPackage(eventPkgName)) ||
                allRoots.any { isRelevantSystemOrSettingsPackage(it.packageName?.toString() ?: "") }

        if (!anyRelevantPkg) {
            return false
        }

        val nonLauncherRoots = allRoots.filter {
            !isLauncherOrSystemUiPackage(it.packageName?.toString() ?: "")
        }
        val combinedText = extractAllWindowsText(nonLauncherRoots, event)
        if (combinedText.isEmpty()) {
            return true
        }

        val isPlayStorePkg = activePkg == "com.android.vending" ||
                (event?.packageName?.toString() == "com.android.vending")

        // =========================================================================
        // 2. PRIVATE DNS PROTECTION
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

            val allowInitialSetup = isInitialDnsSetupGraceActive() && !isAlreadyConfiguredOnScreen
            if (!allowInitialSetup) {
                clickCancelButtonIfPresent(nonLauncherRoots)
                performAutoBackAndLock("attempted to open or disable Private DNS - LOCKED")
                return true
            }
        }

        // =========================================================================
        // 3. ACCESSIBILITY SERVICE (SETTINGS GUARD) DISABLE PROTECTION
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
            clickCancelButtonIfPresent(nonLauncherRoots)
            performAutoBackAndLock("attempted to disable Settings Guard - PERMANENTLY LOCKED")
            return true
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
                clickCancelButtonIfPresent(nonLauncherRoots)
                performAutoBackAndLock("attempted to deactivate Device Administrator - PERMANENTLY LOCKED")
                return true
            }
        }

        // =========================================================================
        // 5. SYSTEM APP INFO / BATTERY / STORAGE / CLEAR DATA FOR PURITY TRACKER
        //    STRICTLY FOR PURITY TRACKER AFTER ENTERING SETTINGS APP INFO PAGE
        //    (Never triggers on launcher long-press menus, other apps, or general Settings)
        // =========================================================================
        if (!isPlayStorePkg) {
            val isOurTamperScreen = checkIsOurAppInfoOrBatteryOrStorageScreen(allRoots, activePkg, event)
            if (isOurTamperScreen) {
                clickCancelButtonIfPresent(nonLauncherRoots)
                performAutoBackAndLock("attempted to open Purity Tracker App Info / Battery / Storage - PIN REQUIRED")
                return true
            }
        }

        return true
    }

    /**
     * Checks each window root and event for an Uninstall / Delete confirmation popup
     * belonging specifically to Purity Tracker.
     * Works across all launchers (home-screen long-press → Uninstall) and package installers,
     * while NEVER triggering on a launcher long-press menu that only shows "App info" + "Uninstall".
     */
    private fun checkIsOurUninstallPopup(
        roots: List<AccessibilityNodeInfo>,
        event: AccessibilityEvent?,
        activePkg: String
    ): Boolean {
        val myPkg = this.packageName

        // 1. Check each individual window root so background launcher icons don't mix with another app's dialog
        for (root in roots) {
            val rootPkg = root.packageName?.toString() ?: activePkg
            val sb = StringBuilder(256)
            val count = IntArray(1)
            appendNodeHierarchyText(root, sb, 0, count)
            val rootText = sb.toString()
            val normRoot = normalizeForMatch(rootText)

            // Direct confirmation title match ("Delete Purity Tracker", "Uninstall Purity Tracker", etc.)
            if (normRoot.contains("delete purity tracker") ||
                normRoot.contains("uninstall purity tracker") ||
                normRoot.contains("remove purity tracker") ||
                rootText.contains("পিউরিটি ট্র্যাকার আনইনস্টল") ||
                rootText.contains("পিউরিটি ট্র্যাকার মুছে") ||
                rootText.contains("পিউরিটি ট্র্যাকার ডিলিট")
            ) {
                return true
            }

            // If this window is a dialog / packageinstaller / small window (or non-launcher window),
            // check if it has both Purity Tracker's identity and an explicit uninstall dialog prompt
            val isDialogOrNonLauncherWindow = count[0] <= 55 || !isLauncherOrSystemUiPackage(rootPkg)
            if (isDialogOrNonLauncherWindow && isOurUninstallPopupText(rootText, rootPkg, myPkg)) {
                return true
            }

            // Even inside a larger launcher window, if an uninstall confirmation dialog subtree has our identity:
            if (findUninstallDialogSubtreeForOurApp(root, myPkg)) {
                return true
            }
        }

        // 2. Also check the event's own text/source (important when an AlertDialog just popped up)
        if (event != null) {
            val evPkg = event.packageName?.toString() ?: activePkg
            if (evPkg != "com.android.systemui" && evPkg != myPkg) {
                val evSb = StringBuilder(128)
                try {
                    event.text?.forEach { cs ->
                        if (!cs.isNullOrEmpty()) evSb.append(cs.toString().lowercase()).append(' ')
                    }
                    event.contentDescription?.let { cd ->
                        if (cd.isNotEmpty()) evSb.append(cd.toString().lowercase()).append(' ')
                    }
                } catch (e: Exception) {}
                val evText = evSb.toString()
                if (isOurUninstallPopupText(evText, evPkg, myPkg)) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Finds any dialog container subtree that contains an uninstall confirmation prompt and checks if
     * that dialog container specifically names Purity Tracker (ignoring background home screen icons).
     */
    private fun findUninstallDialogSubtreeForOurApp(root: AccessibilityNodeInfo?, myPkg: String): Boolean {
        if (root == null) return false

        fun searchPromptNode(node: AccessibilityNodeInfo?, depth: Int): AccessibilityNodeInfo? {
            if (node == null || depth > 14) return null
            try {
                val txt = (node.text?.toString() ?: "").lowercase()
                val desc = (node.contentDescription?.toString() ?: "").lowercase()
                val combined = "$txt $desc"
                if (combined.contains("after uninstallation") ||
                    combined.contains("all app data will be cleared") ||
                    combined.contains("do you want to uninstall") ||
                    combined.contains("want to uninstall this app") ||
                    combined.contains("deleting this app") ||
                    combined.contains("also delete its data") ||
                    combined.contains("all data will be deleted") ||
                    combined.contains("আনইনস্টল করতে চান") ||
                    combined.contains("মুছে ফেলতে চান")
                ) {
                    return node
                }
                for (i in 0 until node.childCount) {
                    val found = searchPromptNode(node.getChild(i), depth + 1)
                    if (found != null) return found
                }
            } catch (e: Exception) {}
            return null
        }

        val promptNode = searchPromptNode(root, 0) ?: return false
        var dialogContainer: AccessibilityNodeInfo = promptNode
        for (step in 0 until 3) {
            val p = try { dialogContainer.parent } catch (e: Exception) { null } ?: break
            dialogContainer = p
        }
        val sb = StringBuilder(256)
        val count = IntArray(1)
        appendNodeHierarchyText(dialogContainer, sb, 0, count)
        val subtreeText = sb.toString()
        return isOurUninstallPopupText(subtreeText, "", myPkg)
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
     * Checks if the active screen is specifically Purity Tracker's Settings App Info page,
     * Power Consumption Controls / Battery page, or Storage/Clear Data page.
     *
     * Requires BOTH:
     * (a) The window belongs to a Settings / App-management package (NEVER a launcher package or launcher popup menu).
     * (b) That same Settings window contains Purity Tracker's identity AND App-info-page-only content
     *     (Version label + rows like Force stop / Storage usage / Open, or Clear data / Power controls).
     */
    private fun checkIsOurAppInfoOrBatteryOrStorageScreen(
        roots: List<AccessibilityNodeInfo>,
        activePkg: String,
        event: AccessibilityEvent?
    ): Boolean {
        // (a) Never trigger if the active foreground package is a launcher
        if (activePkg.isNotEmpty() && isLauncherOrSystemUiPackage(activePkg)) {
            return false
        }

        val myPkg = packageName.lowercase()
        val myPkgPrefix = "com.aistudio.puritytracker"

        fun hasOurNodeInSubtree(node: AccessibilityNodeInfo?, depth: Int): Boolean {
            if (node == null || depth > 18) return false
            try {
                val txt = node.text?.toString()?.trim()?.lowercase() ?: ""
                val desc = node.contentDescription?.toString()?.trim()?.lowercase() ?: ""
                if (txt == "purity tracker" || desc == "purity tracker" ||
                    txt == "পিউরিটি ট্র্যাকার" || desc == "পিউরিটি ট্র্যাকার" ||
                    txt.contains("purity tracker") || desc.contains("purity tracker") ||
                    txt.contains(myPkgPrefix) || desc.contains(myPkgPrefix) ||
                    txt == myPkg || desc == myPkg
                ) {
                    return true
                }
                for (i in 0 until node.childCount) {
                    if (hasOurNodeInSubtree(node.getChild(i), depth + 1)) return true
                }
            } catch (e: Exception) {}
            return false
        }

        // Inspect each Settings / App-management window root individually so background launcher windows never mix in
        for (root in roots) {
            val rootPkg = root.packageName?.toString() ?: activePkg
            if (!isSettingsOrAppManagementPackage(rootPkg)) {
                continue
            }

            val sb = StringBuilder(512)
            val count = IntArray(1)
            appendNodeHierarchyText(root, sb, 0, count)
            val rootText = sb.toString()

            // A full Settings App Info / Storage / Battery page has multiple nodes (never a tiny 2-item popup)
            if (count[0] < 5) {
                continue
            }

            // Ignore the initial system "Allow Purity Tracker to always run in background?" dialog when enabling battery exemption
            if (!AntiUninstallManager.isSystemBatteryOptimizationIgnored(this)) {
                if (rootText.contains("always run in background") ||
                    rootText.contains("ignore battery optimizations") ||
                    rootText.contains("stop optimizing battery usage")
                ) {
                    continue
                }
            }

            if (isOurAppInfoOrTamperText(rootText, this.packageName, rootPkg) &&
                hasOurNodeInSubtree(root, 0)
            ) {
                return true
            }
        }

        // Fallback only when root nodes are not yet populated on a Settings window event
        if (roots.isEmpty() && event != null) {
            val evPkg = event.packageName?.toString() ?: activePkg
            if (isSettingsOrAppManagementPackage(evPkg)) {
                val evText = extractAllWindowsText(emptyList(), event)
                if (isOurAppInfoOrTamperText(evText, this.packageName, evPkg)) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Clicks any Cancel / Dismiss button inside a modal dialog immediately before performing Global Back.
     */
    private fun clickCancelButtonIfPresent(roots: List<AccessibilityNodeInfo>): Boolean {
        val cancelLabels = setOf("cancel", "বাতিল", "বাতিল করুন", "no", "না", "deny")
        fun findAndClickCancel(node: AccessibilityNodeInfo?, depth: Int): Boolean {
            if (node == null || depth > 14) return false
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
            if (findAndClickCancel(r, 0)) return true
        }
        return false
    }

    private fun performBrowserAdultBlock(matchedTerm: String) {
        val nowElapsed = SystemClock.elapsedRealtime()
        if (nowElapsed >= lastBrowserBlockElapsedTime && (nowElapsed - lastBrowserBlockElapsedTime) < 600L) {
            return
        }
        lastBrowserBlockElapsedTime = nowElapsed

        performGlobalAction(GLOBAL_ACTION_BACK)
        mainHandler.postDelayed({
            try {
                performGlobalAction(GLOBAL_ACTION_BACK)
            } catch (e: Exception) {}
        }, 120L)

        BlockerOverlayManager.showBlockScreen(this, matchedTerm)
        triggerGuardianAlert("attempted to access blocked adult content ($matchedTerm)")
    }

    /**
     * Returns true ONLY if `pinOverlayView` is non-null, currently attached to the WindowManager,
     * and visible on screen. Cleans up any stale/detached `pinOverlayView` reference automatically.
     */
    fun isPinOverlayActuallyShowing(): Boolean {
        val view = pinOverlayView ?: return false
        val showing = try {
            view.isAttachedToWindow && view.visibility == View.VISIBLE && view.windowVisibility == View.VISIBLE
        } catch (e: Exception) {
            false
        }
        if (!showing) {
            LockManager.markPinOverlayActive(false)
        }
        return showing
    }

    /**
     * Immediately backs out of Purity Tracker's App info / Uninstall popup AND guarantees
     * that the partner-PIN verification screen appears on top and stays until the correct PIN
     * is entered or the user cancels back to the Home screen.
     */
    private fun performAutoBackAndLock(reason: String) {
        if (LockManager.isCurrentlyUnlocked(this)) {
            return
        }

        // Clean up any stale overlay state so it never blocks a fresh PIN launch
        if (pinOverlayView != null && !isPinOverlayActuallyShowing()) {
            dismissPinOverlayInternal()
        }

        if (isPinOverlayActuallyShowing() || LockManager.isPinActivityVisible()) {
            return
        }

        val nowElapsed = SystemClock.elapsedRealtime()
        val recentlyBacked = nowElapsed >= lastBlockElapsedTime && (nowElapsed - lastBlockElapsedTime) < 450L

        if (!recentlyBacked) {
            lastBlockElapsedTime = nowElapsed

            // 1. Mark short launch grace so the guard does not auto-back its own transition
            LockManager.markPinScreenLaunching()

            // 2. Immediate Back action to close the restricted App info activity or Uninstall popup
            performGlobalAction(GLOBAL_ACTION_BACK)

            // 3. Notify Accountability Partner
            triggerGuardianAlert(reason)
        }

        // 4. Present the PIN screen after ~220ms (once GLOBAL_ACTION_BACK has finished closing App info)
        //    Never swallow this callback even if a repeated event arrived within the 450ms Back debounce!
        mainHandler.removeCallbacks(presentPinRunnable)
        mainHandler.postDelayed(presentPinRunnable, 220L)

        // 5. Follow-up verification at ~520ms and ~950ms: if the PIN screen is not visible, launch it again
        mainHandler.removeCallbacks(followUpVerifyPinRunnable)
        mainHandler.postDelayed(followUpVerifyPinRunnable, 520L)
        mainHandler.postDelayed({
            try {
                if (!LockManager.isCurrentlyUnlocked(this@ProtectionService) &&
                    !isPinOverlayActuallyShowing() &&
                    !LockManager.isPinActivityVisible()
                ) {
                    presentPinScreenGuaranteed()
                }
            } catch (e: Exception) {}
        }, 950L)
    }

    /**
     * Ensures the PIN verification UI is displayed exactly once on top of the screen:
     * 1. First attaches the `TYPE_ACCESSIBILITY_OVERLAY` PIN screen (works on all Android phones
     *    even when "Display over other apps" is OFF and is never covered by Settings activity transitions).
     * 2. If the overlay cannot attach for any reason, launches `PinLockActivity` as fallback.
     */
    private fun presentPinScreenGuaranteed() {
        if (LockManager.isCurrentlyUnlocked(this)) return
        if (isPinOverlayActuallyShowing() || LockManager.isPinActivityVisible()) return

        val overlayShown = showAccessibilityPinOverlay()
        if (!overlayShown) {
            launchPinLockScreenNow()
        } else {
            // Verify 120ms later that the overlay is attached and visible; if not, launch PinLockActivity fallback
            mainHandler.postDelayed({
                if (!LockManager.isCurrentlyUnlocked(this@ProtectionService) &&
                    !isPinOverlayActuallyShowing() &&
                    !LockManager.isPinActivityVisible()
                ) {
                    launchPinLockScreenNow()
                }
            }, 120L)
        }
    }

    private fun launchPinLockScreenNow() {
        if (LockManager.isCurrentlyUnlocked(this) || isPinOverlayActuallyShowing() || LockManager.isPinActivityVisible()) {
            return
        }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                try {
                    val options = ActivityOptions.makeBasic().apply {
                        setPendingIntentBackgroundActivityStartMode(
                            ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                        )
                    }
                    val pi = PendingIntent.getActivity(
                        this,
                        7701,
                        pinIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    pi.send(this, 0, null, null, null, null, options.toBundle())
                } catch (e: Exception) {}
            }
            startActivity(pinIntent)
        } catch (e: Exception) {}
    }

    /**
     * Displays the interactive partner-PIN screen using `TYPE_ACCESSIBILITY_OVERLAY`.
     * Works on 100% of Android devices (OPPO/ColorOS, Samsung, Xiaomi, Vivo, Pixel, OnePlus, Realme)
     * without requiring `SYSTEM_ALERT_WINDOW` ("Display over other apps") permission, and includes
     * a built-in numeric keypad so PIN entry never depends on soft-keyboard window layering.
     */
    private fun showAccessibilityPinOverlay(): Boolean {
        if (LockManager.isCurrentlyUnlocked(this)) return false
        if (isPinOverlayActuallyShowing()) return true
        if (pinOverlayView != null) {
            dismissPinOverlayInternal()
        }

        return try {
            val wm = getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return false
            val dp = { v: Int ->
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    v.toFloat(),
                    resources.displayMetrics
                ).toInt()
            }

            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            val rootContainer = object : FrameLayout(this) {
                override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
                    if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
                        // Never auto-back out of the PIN screen; require PIN or explicit Cancel
                        return true
                    }
                    return super.dispatchKeyEvent(event)
                }
            }.apply {
                setBackgroundColor(AndroidColor.parseColor("#F20F172A"))
                setPadding(dp(20), dp(20), dp(20), dp(20))
                isFocusable = true
                isFocusableInTouchMode = true
            }

            val scrollView = ScrollView(this).apply {
                isFillViewport = true
            }

            val centerWrapper = FrameLayout(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            }

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(24).toFloat()
                    setColor(AndroidColor.parseColor("#1E293B"))
                    setStroke(dp(1), AndroidColor.parseColor("#334155"))
                }
                setPadding(dp(20), dp(20), dp(20), dp(18))
            }

            val iconBadge = TextView(this).apply {
                text = "🔒"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(AndroidColor.parseColor("#2600FFCC"))
                }
                width = dp(52)
                height = dp(52)
            }

            val titleView = TextView(this).apply {
                text = "Security Verification"
                setTextColor(AndroidColor.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(0, dp(10), 0, dp(4))
            }

            val hasPartnerPinNow = LockManager.hasPin(this)
            val subtitleView = TextView(this).apply {
                text = if (hasPartnerPinNow) {
                    "Enter the security PIN set by your accountability partner to pause the guard for 60 seconds."
                } else {
                    "No partner PIN is set yet. Tap 'Forgot PIN?' below so your accountability partner can set your PIN."
                }
                setTextColor(AndroidColor.parseColor("#94A3B8"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(12))
            }

            val pinInput = EditText(this).apply {
                hint = "Enter PIN (4–6 digits)"
                setHintTextColor(AndroidColor.parseColor("#64748B"))
                setTextColor(AndroidColor.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                transformationMethod = PasswordTransformationMethod.getInstance()
                filters = arrayOf(InputFilter.LengthFilter(6))
                showSoftInputOnFocus = false
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(14).toFloat()
                    setColor(AndroidColor.parseColor("#0F172A"))
                    setStroke(dp(1), AndroidColor.parseColor("#00FFCC"))
                }
                setPadding(dp(16), dp(10), dp(16), dp(10))
            }

            val feedbackView = TextView(this).apply {
                text = ""
                setTextColor(AndroidColor.parseColor("#EF4444"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                gravity = Gravity.CENTER
                visibility = View.GONE
                setPadding(0, dp(6), 0, 0)
            }

            // Built-in numeric keypad (1-9, Clear, 0, Backspace) so PIN entry works on 100% of phones
            // even without soft-keyboard IME elevation above TYPE_ACCESSIBILITY_OVERLAY
            val keypadContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, dp(10), 0, dp(4))
            }

            val appendDigit = { digit: String ->
                val current = pinInput.text?.toString().orEmpty()
                if (current.length < 6) {
                    val updated = current + digit
                    pinInput.setText(updated)
                    pinInput.setSelection(updated.length)
                    feedbackView.visibility = View.GONE
                }
            }

            val keypadRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("CLR", "0", "⌫")
            )

            for (rowLabels in keypadRows) {
                val rowLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    weightSum = 3f
                }
                for (label in rowLabels) {
                    val keyBtn = Button(this).apply {
                        text = label
                        isAllCaps = false
                        setTextColor(
                            if (label == "CLR" || label == "⌫") AndroidColor.parseColor("#94A3B8")
                            else AndroidColor.WHITE
                        )
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, if (label == "CLR") 12f else 16f)
                        typeface = Typeface.DEFAULT_BOLD
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = dp(12).toFloat()
                            setColor(AndroidColor.parseColor("#0F172A"))
                            setStroke(dp(1), AndroidColor.parseColor("#334155"))
                        }
                        setPadding(0, dp(8), 0, dp(8))
                        setOnClickListener {
                            when (label) {
                                "CLR" -> {
                                    pinInput.setText("")
                                    feedbackView.visibility = View.GONE
                                }
                                "⌫" -> {
                                    val cur = pinInput.text?.toString().orEmpty()
                                    if (cur.isNotEmpty()) {
                                        val next = cur.dropLast(1)
                                        pinInput.setText(next)
                                        pinInput.setSelection(next.length)
                                    }
                                    feedbackView.visibility = View.GONE
                                }
                                else -> appendDigit(label)
                            }
                        }
                    }
                    val keyLp = LinearLayout.LayoutParams(0, dp(44), 1f).apply {
                        setMargins(dp(4), dp(3), dp(4), dp(3))
                    }
                    rowLayout.addView(keyBtn, keyLp)
                }
                keypadContainer.addView(
                    rowLayout,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }

            // Listen for partner PIN reset approval while the overlay is open
            BlockerManager.ensurePinResetApprovalListener(this) { status ->
                if (status == "approved") {
                    mainHandler.post {
                        try {
                            BlockerManager.consumePartnerPinResetNotice(this@ProtectionService)
                            subtitleView.text = "Enter the security PIN set by your accountability partner to pause the guard for 60 seconds."
                            feedbackView.setTextColor(AndroidColor.parseColor("#34D399"))
                            feedbackView.text = "Your PIN was reset by your partner"
                            feedbackView.visibility = View.VISIBLE
                        } catch (e: Exception) {}
                    }
                }
            }

            val unlockBtn = Button(this).apply {
                text = "Unlock Settings"
                isAllCaps = false
                setTextColor(AndroidColor.BLACK)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                typeface = Typeface.DEFAULT_BOLD
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(14).toFloat()
                    setColor(AndroidColor.parseColor("#00FFCC"))
                }
                setPadding(dp(20), dp(11), dp(20), dp(11))
                setOnClickListener {
                    val entered = pinInput.text?.toString().orEmpty()
                    if (!LockManager.hasPin(this@ProtectionService)) {
                        feedbackView.setTextColor(AndroidColor.parseColor("#EF4444"))
                        feedbackView.text = "No PIN set yet — tap 'Forgot PIN?' so your partner can set it."
                        feedbackView.visibility = View.VISIBLE
                        pinInput.setText("")
                    } else if (LockManager.verifyPin(this@ProtectionService, entered)) {
                        LockManager.unlockForSeconds(this@ProtectionService, 60)
                        dismissPinOverlayInternal()
                        PinLockActivity.finishIfOpen()
                        Toast.makeText(
                            this@ProtectionService,
                            "Guard paused for 60 seconds — you can now uninstall",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        feedbackView.setTextColor(AndroidColor.parseColor("#EF4444"))
                        feedbackView.text = "Incorrect PIN! Please try again."
                        feedbackView.visibility = View.VISIBLE
                        pinInput.setText("")
                    }
                }
            }

            val forgotBtn = Button(this).apply {
                text = "Forgot PIN?"
                isAllCaps = false
                setTextColor(AndroidColor.parseColor("#38BDF8"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                background = null
                setOnClickListener {
                    if (!BlockerManager.hasValidPartnerEmail(this@ProtectionService)) {
                        feedbackView.setTextColor(AndroidColor.parseColor("#EF4444"))
                        feedbackView.text = "Save an accountability partner email on the Blocker screen first."
                        feedbackView.visibility = View.VISIBLE
                    } else {
                        isEnabled = false
                        text = "Sending..."
                        BlockerManager.sendPinResetRequestToPartner(this@ProtectionService) { ok, msg ->
                            isEnabled = true
                            text = "Forgot PIN?"
                            feedbackView.setTextColor(
                                AndroidColor.parseColor(if (ok) "#34D399" else "#EF4444")
                            )
                            feedbackView.text = if (ok) "PIN request sent to your partner" else msg
                            feedbackView.visibility = View.VISIBLE
                            if (ok) {
                                Toast.makeText(
                                    this@ProtectionService,
                                    "PIN request sent to your partner",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }

            val cancelBtn = Button(this).apply {
                text = "Cancel (Go Home)"
                isAllCaps = false
                setTextColor(AndroidColor.parseColor("#94A3B8"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                background = null
                setOnClickListener {
                    dismissPinOverlayInternal()
                    PinLockActivity.finishIfOpen()
                    try {
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(homeIntent)
                    } catch (e: Exception) {}
                }
            }

            val matchWrap = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val unlockLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }

            card.addView(iconBadge)
            card.addView(titleView, matchWrap)
            card.addView(subtitleView, matchWrap)
            card.addView(pinInput, matchWrap)
            card.addView(feedbackView, matchWrap)
            card.addView(keypadContainer, matchWrap)
            card.addView(unlockBtn, unlockLp)
            card.addView(forgotBtn, matchWrap)
            card.addView(cancelBtn, matchWrap)

            val cardLp = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            centerWrapper.addView(card, cardLp)
            scrollView.addView(
                centerWrapper,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            )
            rootContainer.addView(
                scrollView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )

            var added = false
            try {
                wm.addView(rootContainer, params)
                added = true
            } catch (e: Exception) {
                // Fallback if TYPE_ACCESSIBILITY_OVERLAY fails and SYSTEM_ALERT_WINDOW is granted
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
                    try {
                        params.type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        } else {
                            @Suppress("DEPRECATION")
                            WindowManager.LayoutParams.TYPE_PHONE
                        }
                        wm.addView(rootContainer, params)
                        added = true
                    } catch (inner: Exception) {}
                }
            }

            if (added) {
                pinOverlayView = rootContainer
                LockManager.markPinOverlayActive(true)
                rootContainer.requestFocus()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun dismissPinOverlayInternal() {
        val view = pinOverlayView
        pinOverlayView = null
        LockManager.markPinOverlayActive(false)
        if (view != null) {
            try {
                val wm = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                wm?.removeViewImmediate(view)
            } catch (e: Exception) {}
        }
    }

    private fun extractAllWindowsText(
        roots: List<AccessibilityNodeInfo>,
        event: AccessibilityEvent?
    ): String {
        val sb = StringBuilder(512)

        if (event != null) {
            try {
                val evPkg = event.packageName?.toString() ?: ""
                if (evPkg != "com.android.systemui" &&
                    !evPkg.contains("systemui") &&
                    !isLauncherOrSystemUiPackage(evPkg) &&
                    evPkg != this.packageName
                ) {
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
                }
            } catch (e: Exception) {}
        }

        val count = IntArray(1)
        for (root in roots) {
            appendNodeHierarchyText(root, sb, 0, count)
        }

        if (roots.isEmpty() && event != null) {
            try {
                val evPkg = event.packageName?.toString() ?: ""
                if (evPkg != "com.android.systemui" &&
                    !evPkg.contains("systemui") &&
                    !isLauncherOrSystemUiPackage(evPkg) &&
                    evPkg != this.packageName
                ) {
                    event.source?.let { srcNode ->
                        appendNodeHierarchyText(srcNode, sb, 0, count)
                    }
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
