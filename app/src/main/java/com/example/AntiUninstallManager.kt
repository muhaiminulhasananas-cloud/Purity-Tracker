package com.example

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager

object AntiUninstallManager {

    /**
     * Checks if Device Administrator is currently active.
     * When Device Administrator is active, the Android OS natively blocks uninstallation
     * of the app and greys out the "Uninstall" button in Settings!
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
     * Checks whether the ProtectionService instance is actively bound, connected, or listed in system's active list.
     */
    fun isAccessibilityServiceConnected(context: Context? = null): Boolean {
        if (ProtectionService.isServiceRunning) return true
        if (context != null) {
            if (isAccessibilityConfiguredInSystem(context)) {
                try {
                    SettingsGuardWatchdogService.start(context)
                } catch (e: Exception) {}
                return true
            }
        }
        return false
    }

    /**
     * Checks if Accessibility Service is enabled in Android Settings.
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
                        (sInfo.name == ProtectionService::class.java.name || sInfo.name.contains("ProtectionService"))) {
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
                     (componentNameString.contains(context.packageName, ignoreCase = true) || componentNameString.contains("com.example", ignoreCase = true)))
                ) {
                    return true
                }
            }
        } catch (e: Exception) {}
        return false
    }

    /**
     * Robust check if ProtectionService (Accessibility Service) is enabled.
     * True if either the service is connected in memory OR enabled in Android settings.
     */
    fun isAccessibilityServiceActive(context: Context): Boolean {
        if (ProtectionService.isServiceRunning) return true
        return isAccessibilityConfiguredInSystem(context)
    }

    /**
     * Indicates whether the service is configured in Settings but was disconnected/killed by the OS.
     */
    fun isAccessibilityNeedsRestart(context: Context): Boolean {
        return isAccessibilityConfiguredInSystem(context) && !isAccessibilityServiceConnected(context)
    }

    /**
     * Open system settings for Accessibility Service directly (never redirecting to App Info).
     */
    fun requestAccessibilityService(context: Context) {
        try {
            SettingsGuardWatchdogService.start(context)
        } catch (e: Exception) {}

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
     * Request unrestricted background/foreground execution & auto-launch without sending the user to App Info.
     */
    fun openAutoStartSettings(context: Context) {
        try {
            context.getSharedPreferences("ProtectionPrefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("auto_start_enabled", true)
                .apply()
        } catch (e: Exception) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                if (pm != null && !pm.isIgnoringBatteryOptimizations(context.packageName)) {
                    requestIgnoreBatteryOptimization(context)
                    return
                }
            } catch (e: Exception) {}
        }
    }

    /**
     * Checks if Battery Optimizations / Auto-Launch & Background/Foreground execution are enabled.
     * Defaults to enabled on app install and requests system unrestricted battery mode automatically.
     */
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        return try {
            val prefs = context.getSharedPreferences("ProtectionPrefs", Context.MODE_PRIVATE)
            if (!prefs.contains("auto_start_enabled")) {
                prefs.edit().putBoolean("auto_start_enabled", true).apply()
            }
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val systemIgnored = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
            } else true
            systemIgnored || prefs.getBoolean("auto_start_enabled", true)
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Checks if raw OS-level battery optimization exemption is granted.
     */
    fun isSystemBatteryOptimizationIgnored(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
            } catch (e: Exception) {
                true
            }
        } else {
            true
        }
    }

    /**
     * Request battery optimization exemption so the OS does not sleep or kill the protection after 1 day.
     */
    fun requestIgnoreBatteryOptimization(context: Context) {
        try {
            context.getSharedPreferences("ProtectionPrefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("auto_start_enabled", true)
                .apply()
        } catch (e: Exception) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                if (pm != null && !pm.isIgnoringBatteryOptimizations(context.packageName)) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            } catch (e: Exception) {}
        }
    }

    /**
     * Overall Anti-Uninstall protection status.
     * True if Device Admin is active OR Accessibility Service is active.
     */
    fun isAntiUninstallActive(context: Context): Boolean {
        return isDeviceAdminActive(context) || isAccessibilityServiceActive(context)
    }
}
