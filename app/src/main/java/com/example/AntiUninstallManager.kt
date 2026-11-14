package com.example

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import android.widget.Toast

object AntiUninstallManager {

    private const val PREFS_NAME = "ProtectionPrefs"
    private const val KEY_GUARD_EXPECTED_ON = "guard_enabled_by_user"
    private const val KEY_LAST_ACTIVE_ELAPSED = "last_active_elapsed_realtime"
    private const val KEY_OEM_AUTOSTART_VISITED = "oem_autostart_visited"
    private const val KEY_RECENTS_LOCK_CONFIRMED = "recents_lock_confirmed"

    // 5 minutes maximum staleness window before an enabled service is considered dead-but-enabled
    const val MAX_HEARTBEAT_STALE_MS = 5 * 60 * 1000L

    private fun getSafePrefs(context: Context) = try {
        val safeCtx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createDeviceProtectedStorageContext()
        } else {
            context
        }
        safeCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    } catch (e: Exception) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setGuardExpectedToBeOn(context: Context, expected: Boolean) {
        try {
            getSafePrefs(context).edit().putBoolean(KEY_GUARD_EXPECTED_ON, expected).apply()
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_GUARD_EXPECTED_ON, expected).apply()
        } catch (e: Exception) {}
    }

    fun isGuardExpectedToBeOn(context: Context): Boolean {
        return try {
            getSafePrefs(context).getBoolean(KEY_GUARD_EXPECTED_ON, false) ||
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .getBoolean(KEY_GUARD_EXPECTED_ON, false)
        } catch (e: Exception) {
            false
        }
    }

    fun recordServiceHeartbeat(context: Context, elapsedRealtime: Long = SystemClock.elapsedRealtime()) {
        try {
            getSafePrefs(context).edit()
                .putLong(KEY_LAST_ACTIVE_ELAPSED, elapsedRealtime)
                .putBoolean(KEY_GUARD_EXPECTED_ON, true)
                .apply()
        } catch (e: Exception) {}
    }

    fun getLastRecordedHeartbeatElapsed(context: Context): Long {
        val inMemory = ProtectionService.lastActiveElapsedRealtime
        if (inMemory > 0L) return inMemory
        return try {
            val stored = getSafePrefs(context).getLong(KEY_LAST_ACTIVE_ELAPSED, 0L)
            val now = SystemClock.elapsedRealtime()
            // If phone rebooted since `stored` was saved, `stored` might be > `now`
            if (stored > now) 0L else stored
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Checks if Device Administrator is currently active.
     */
    fun isDeviceAdminActive(context: Context): Boolean {
        return try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false
            val adminComponent = ComponentName(context, AntiUninstallAdminReceiver::class.java)
            dpm.isAdminActive(adminComponent)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Launch the system screen to activate Device Administrator.
     */
    fun requestDeviceAdmin(context: Context) {
        try {
            val adminComponent = ComponentName(context, AntiUninstallAdminReceiver::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    if (LanguageStore.currentLanguage.value == "bn")
                        "Purity Tracker আনইনস্টল হওয়া স্থায়ীভাবে রোধ করার জন্য ডিভাইস অ্যাডমিন সক্রিয় করুন।"
                    else
                        "Activate Device Admin to permanently prevent uninstallation of Purity Tracker."
                )
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {}
        }
    }

    /**
     * Honest LIVENESS check for ProtectionService.
     * Never trusts the Settings toggle alone!
     * Returns true ONLY if:
     * 1. The toggle is enabled in Android Accessibility Settings, AND
     * 2. `ProtectionService.isServiceRunning` is true in-process, AND
     * 3. `lastActiveElapsedRealtime` is fresh (updated within the last 5 minutes).
     */
    fun isAccessibilityServiceAlive(context: Context): Boolean {
        if (!isAccessibilityConfiguredInSystem(context)) return false
        if (!ProtectionService.isServiceRunning) return false
        val lastActive = ProtectionService.lastActiveElapsedRealtime
        if (lastActive <= 0L) return false
        val ageMs = SystemClock.elapsedRealtime() - lastActive
        return ageMs in 0..MAX_HEARTBEAT_STALE_MS
    }

    /**
     * Returns true when Android Settings shows Purity Tracker's Accessibility toggle as ON,
     * but the service process is NOT alive or its heartbeat is stale (> 5 minutes).
     * On OPPO/ColorOS/Realme/MIUI, only toggling OFF -> ON in Accessibility Settings rebinds it.
     */
    fun isAccessibilityDeadButEnabled(context: Context): Boolean {
        return isAccessibilityConfiguredInSystem(context) && !isAccessibilityServiceAlive(context)
    }

    /**
     * Checks whether the ProtectionService instance is actively bound, connected, and alive.
     * Does NOT return true merely because the Settings toggle is ON.
     */
    fun isAccessibilityServiceConnected(context: Context? = null): Boolean {
        if (!ProtectionService.isServiceRunning) return false
        val lastActive = ProtectionService.lastActiveElapsedRealtime
        if (lastActive <= 0L) return false
        val ageMs = SystemClock.elapsedRealtime() - lastActive
        if (ageMs < 0L || ageMs > MAX_HEARTBEAT_STALE_MS) return false
        return if (context != null) {
            isAccessibilityConfiguredInSystem(context)
        } else {
            true
        }
    }

    /**
     * Checks if Accessibility Service toggle is enabled in Android Settings.
     * NOTE: This only reflects the stored setting in Android, NOT whether the process is alive!
     */
    fun isAccessibilityConfiguredInSystem(context: Context): Boolean {
        try {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            if (am != null) {
                val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                for (service in enabledServices) {
                    val sInfo = service.resolveInfo?.serviceInfo
                    if (sInfo != null &&
                        (sInfo.packageName == context.packageName || sInfo.packageName == "com.example") &&
                        (sInfo.name == ProtectionService::class.java.name || sInfo.name.contains("ProtectionService"))
                    ) {
                        return true
                    }
                }
            }

            // Fallback: Check Settings.Secure string directly
            val expectedComponentName = ComponentName(context, ProtectionService::class.java).flattenToString()
            val shortComponentName = "${context.packageName}/.ProtectionService"
            val legacyComponentName = "com.example/.ProtectionService"
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)
            while (colonSplitter.hasNext()) {
                val componentNameString = colonSplitter.next()
                if (componentNameString.equals(expectedComponentName, ignoreCase = true) ||
                    componentNameString.equals(shortComponentName, ignoreCase = true) ||
                    componentNameString.equals(legacyComponentName, ignoreCase = true) ||
                    (componentNameString.contains("ProtectionService", ignoreCase = true) &&
                        (componentNameString.contains(context.packageName, ignoreCase = true) ||
                            componentNameString.contains("com.example", ignoreCase = true)))
                ) {
                    return true
                }
            }
        } catch (e: Exception) {}
        return false
    }

    /**
     * Honest check if ProtectionService (Accessibility Service) is both enabled AND alive.
     * Never pretends the guard is ON when the service is unbound/dead.
     */
    fun isAccessibilityServiceActive(context: Context): Boolean {
        return isAccessibilityServiceAlive(context)
    }

    /**
     * Indicates whether the service is configured in Settings but was disconnected/killed by the OS.
     */
    fun isAccessibilityNeedsRestart(context: Context): Boolean {
        return isAccessibilityDeadButEnabled(context)
    }

    /**
     * Open system settings for Accessibility Service directly.
     */
    fun requestAccessibilityService(context: Context) {
        setGuardExpectedToBeOn(context, true)
        if (context is Activity) {
            try {
                SettingsGuardWatchdogService.startIfInForeground(context)
            } catch (e: Exception) {}
        }

        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {}
        }
    }

    /**
     * Opens the OEM-specific Auto-Start / Background Startup management screen
     * with a fallback chain:
     * 1. ColorOS / OPPO (`com.coloros.safecenter` startup manager)
     * 2. Realme / OPlus (`com.oplus.safecenter` / `com.coloros.safecenter`)
     * 3. MIUI / Xiaomi (`com.miui.securitycenter` AutoStartManagementActivity)
     * 4. Vivo / iQOO (`com.vivo.permissionmanager` / `com.iqoo.secure`)
     * 5. Samsung (`com.samsung.android.lool` / `com.samsung.android.sm`)
     * 6. Generic App Info (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`)
     */
    fun openOemAutoStartSettings(context: Context) {
        markOemAutoStartVisited(context)

        val candidateIntents = listOf(
            // 1. ColorOS / OPPO
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.privacypermissionsentry.PermissionTopActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.oppo.safe",
                    "com.oppo.safe.permission.startup.StartupAppListActivity"
                )
            ),
            // 2. Realme / OPlus
            Intent().setComponent(
                ComponentName(
                    "com.oplus.safecenter",
                    "com.oplus.safecenter.permission.startup.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.oplus.safecenter",
                    "com.oplus.safecenter.startupapp.StartupAppListActivity"
                )
            ),
            // 3. Xiaomi / MIUI / HyperOS
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            ),
            Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT),
            // 4. Vivo / iQOO
            Intent().setComponent(
                ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                )
            ),
            // 5. Samsung (Device Care / Battery)
            Intent().setComponent(
                ComponentName(
                    "com.samsung.android.lool",
                    "com.samsung.android.sm.battery.ui.BatteryActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.samsung.android.sm",
                    "com.samsung.android.sm.ui.battery.BatteryActivity"
                )
            )
        )

        for (candidate in candidateIntents) {
            try {
                if (context !is Activity) {
                    candidate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (context.packageManager.resolveActivity(candidate, 0) != null) {
                    context.startActivity(candidate)
                    return
                }
            } catch (e: Exception) {}
        }

        // 6. On stock Android / Pixel without a vendor Auto-Start manager, do NOT open App Info
        // (which would expose the Uninstall button); instead open Battery Optimization settings or confirm Auto-start.
        if (!isBatteryOptimizationIgnored(context)) {
            requestIgnoreBatteryOptimization(context)
        } else {
            Toast.makeText(
                context,
                "Auto-start is active on this device.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun openAutoStartSettings(context: Context) {
        if (!isBatteryOptimizationIgnored(context)) {
            requestIgnoreBatteryOptimization(context)
        } else {
            openOemAutoStartSettings(context)
        }
    }

    fun markOemAutoStartVisited(context: Context) {
        try {
            getSafePrefs(context).edit().putBoolean(KEY_OEM_AUTOSTART_VISITED, true).apply()
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_OEM_AUTOSTART_VISITED, true).apply()
        } catch (e: Exception) {}
    }

    fun isOemAutoStartVisited(context: Context): Boolean {
        return try {
            getSafePrefs(context).getBoolean(KEY_OEM_AUTOSTART_VISITED, false) ||
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .getBoolean(KEY_OEM_AUTOSTART_VISITED, false)
        } catch (e: Exception) {
            false
        }
    }

    fun markRecentsLockConfirmed(context: Context, confirmed: Boolean = true) {
        try {
            getSafePrefs(context).edit().putBoolean(KEY_RECENTS_LOCK_CONFIRMED, confirmed).apply()
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_RECENTS_LOCK_CONFIRMED, confirmed).apply()
        } catch (e: Exception) {}
    }

    fun isRecentsLockConfirmed(context: Context): Boolean {
        return try {
            getSafePrefs(context).getBoolean(KEY_RECENTS_LOCK_CONFIRMED, false) ||
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .getBoolean(KEY_RECENTS_LOCK_CONFIRMED, false)
        } catch (e: Exception) {
            false
        }
    }

    fun getDeviceBrandName(): String {
        val manufacturer = (Build.MANUFACTURER ?: "").trim()
        val brand = (Build.BRAND ?: "").trim()
        return when {
            manufacturer.contains("oppo", ignoreCase = true) || brand.contains("oppo", ignoreCase = true) -> "OPPO (ColorOS)"
            manufacturer.contains("realme", ignoreCase = true) || brand.contains("realme", ignoreCase = true) -> "Realme UI"
            manufacturer.contains("oneplus", ignoreCase = true) || brand.contains("oneplus", ignoreCase = true) -> "OnePlus (OxygenOS)"
            manufacturer.contains("xiaomi", ignoreCase = true) || brand.contains("xiaomi", ignoreCase = true) || brand.contains("redmi", ignoreCase = true) || brand.contains("poco", ignoreCase = true) -> "Xiaomi / Redmi (MIUI / HyperOS)"
            manufacturer.contains("vivo", ignoreCase = true) || brand.contains("vivo", ignoreCase = true) || brand.contains("iqoo", ignoreCase = true) -> "Vivo / iQOO (FuntouchOS)"
            manufacturer.contains("samsung", ignoreCase = true) || brand.contains("samsung", ignoreCase = true) -> "Samsung (One UI)"
            else -> manufacturer.replaceFirstChar { it.uppercase() }.ifBlank { "Android" }
        }
    }

    fun getBrandRecentsLockInstruction(isBn: Boolean): String {
        val m = "${Build.MANUFACTURER} ${Build.BRAND}".lowercase()
        return when {
            m.contains("oppo") || m.contains("realme") || m.contains("oneplus") || m.contains("oplus") -> {
                if (isBn) {
                    "১. ফোনের Recent Apps (মাল্টিটাস্কিং) বাটনে চাপুন।\n" +
                    "২. Purity Tracker কার্ডের উপরে থাকা তিন ডট (⋮) মেনুতে ট্যাপ করে 'Lock' (🔒) সিলেক্ট করুন।\n" +
                    "৩. App Info → Battery usage → 'Allow background activity' এবং 'Allow auto launch' চালু রাখুন।"
                } else {
                    "1. Open Recent Apps (multitasking switcher).\n" +
                    "2. Tap the three dots (⋮) at the top of Purity Tracker and tap 'Lock' (🔒).\n" +
                    "3. In App Info → Battery usage, enable 'Allow background activity' and 'Allow auto launch'."
                }
            }
            m.contains("xiaomi") || m.contains("redmi") || m.contains("poco") -> {
                if (isBn) {
                    "১. Recent Apps খুলুন এবং Purity Tracker কার্ডটি লং-প্রেস করে তালা (🔒 Lock) আইকনে ট্যাপ করুন।\n" +
                    "২. App Info → Battery saver → 'No restrictions' এবং 'Autostart' চালু করুন।"
                } else {
                    "1. Open Recent Apps, long-press the Purity Tracker card, and tap the Lock (🔒) icon.\n" +
                    "2. In App Info → Battery saver, select 'No restrictions' and enable 'Autostart'."
                }
            }
            m.contains("vivo") || m.contains("iqoo") -> {
                if (isBn) {
                    "১. Recent Apps খুলুন এবং Purity Tracker-এর নামের পাশের অ্যারোতে ট্যাপ করে 'Lock' (🔒) করুন।\n" +
                    "২. Battery settings → 'High background power consumption' এবং 'Autostart' চালু করুন।"
                } else {
                    "1. Open Recent Apps, tap the arrow next to Purity Tracker, and select 'Lock' (🔒).\n" +
                    "2. In Battery settings, enable 'High background power consumption' and 'Autostart'."
                }
            }
            m.contains("samsung") -> {
                if (isBn) {
                    "১. Recent Apps খুলুন, Purity Tracker আইকনে ট্যাপ করে 'Keep open' (🔒) সিলেক্ট করুন।\n" +
                    "২. App Info → Battery → 'Unrestricted' সিলেক্ট করুন।"
                } else {
                    "1. Open Recent Apps, tap the Purity Tracker icon at the top, and select 'Keep open' (🔒).\n" +
                    "2. In App Info → Battery, select 'Unrestricted'."
                }
            }
            else -> {
                if (isBn) {
                    "১. ফোনের Recent Apps (মাল্টিটাস্কিং) স্ক্রিনে গিয়ে Purity Tracker অ্যাপটি 'Lock' (🔒) করে রাখুন।\n" +
                    "২. App Info → Battery-তে গিয়ে 'Unrestricted' / 'Allow background activity' চালু করুন।"
                } else {
                    "1. Open Recent Apps and Lock (🔒) Purity Tracker so the system never clears it.\n" +
                    "2. In App Info → Battery, set battery usage to 'Unrestricted' / 'Allow background activity'."
                }
            }
        }
    }

    /**
     * Checks if OS-level Battery Optimization is ignored.
     * MUST return ONLY the real system value from `PowerManager.isIgnoringBatteryOptimizations()`.
     * Never falls back to a SharedPreferences flag!
     */
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        return isSystemBatteryOptimizationIgnored(context)
    }

    /**
     * Checks if raw OS-level battery optimization exemption is granted via PowerManager.
     */
    fun isSystemBatteryOptimizationIgnored(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
            } catch (e: Exception) {
                false
            }
        } else {
            true
        }
    }

    /**
     * Request battery optimization exemption so the OS does not sleep or kill the protection after 1 day.
     */
    fun requestIgnoreBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                if (pm != null && !pm.isIgnoringBatteryOptimizations(context.packageName)) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return
                }
            } catch (e: Exception) {}
            try {
                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {}
        }
    }

    /**
     * Overall Anti-Uninstall protection status.
     * True if Device Admin is active OR Accessibility Service is alive.
     */
    fun isAntiUninstallActive(context: Context): Boolean {
        return isDeviceAdminActive(context) || isAccessibilityServiceAlive(context)
    }
}
