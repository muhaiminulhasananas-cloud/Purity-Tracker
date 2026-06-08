package com.example

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class ProtectionService : AccessibilityService() {

    private var screenReceiver: android.content.BroadcastReceiver? = null
    private val vpnHandler = android.os.Handler(android.os.Looper.getMainLooper())
    
    private fun getSafePrefs(): android.content.SharedPreferences {
        val safeContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            applicationContext.createDeviceProtectedStorageContext()
        } else {
            applicationContext
        }
        return safeContext.getSharedPreferences("VpnPrefs", android.content.Context.MODE_PRIVATE)
    }

    private val vpnCheckRunnable = object : Runnable {
        override fun run() {
            try {
                val prefs = getSafePrefs()
                val isVpnPreferred = prefs.getBoolean("vpn_is_running", false)
                if (isVpnPreferred) {
                    if (!LocalDnsVpnService.isVpnInterfaceUp()) {
                        if (android.net.VpnService.prepare(applicationContext) == null) {
                            val startIntent = Intent(applicationContext, LocalDnsVpnService::class.java).apply {
                                action = LocalDnsVpnService.ACTION_START
                            }
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                startForegroundService(startIntent)
                            } else {
                                startService(startIntent)
                            }
                            LocalDnsVpnService.isRunning = true
                        }
                    }
                }
            } catch (e: Exception) {}
            vpnHandler.postDelayed(this, 1000) // Continually check and enforce VPN every 1 second
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        this.serviceInfo = info
        
        // Start persistent vpn status guard loop
        vpnHandler.post(vpnCheckRunnable)
        
        // Ensure VPN is running as early as possible after a reboot or restart
        val prefs = getSafePrefs()
        if (prefs.getBoolean("vpn_is_running", false) && !LocalDnsVpnService.isVpnInterfaceUp()) {
            try {
                if (android.net.VpnService.prepare(this) == null) {
                    val startIntent = Intent(this, LocalDnsVpnService::class.java).apply {
                        action = LocalDnsVpnService.ACTION_START
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        startForegroundService(startIntent)
                    } else {
                        startService(startIntent)
                    }
                    LocalDnsVpnService.isRunning = true
                }
            } catch (e: Exception) {}
        }
        
        // Register screen on receiver
        screenReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context, intent: Intent) {
                if (intent.action == Intent.ACTION_SCREEN_ON || intent.action == Intent.ACTION_USER_PRESENT) {
                    val prefs = getSafePrefs()
                    if (prefs.getBoolean("vpn_is_running", false) && !LocalDnsVpnService.isVpnInterfaceUp()) {
                        try {
                            val startIntent = Intent(context, LocalDnsVpnService::class.java).apply {
                                action = LocalDnsVpnService.ACTION_START
                            }
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                context.startForegroundService(startIntent)
                            } else {
                                context.startService(startIntent)
                            }
                            LocalDnsVpnService.isRunning = true
                        } catch (e: Exception) {}
                    }
                }
            }
        }
        val filter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        screenReceiver?.let { unregisterReceiver(it) }
        vpnHandler.removeCallbacks(vpnCheckRunnable)
        return super.onUnbind(intent)
    }

    private fun getAllText(node: android.view.accessibility.AccessibilityNodeInfo?, activePkg: String, depth: Int = 0): String {
        if (node == null || depth > 40) return ""
        val sb = java.lang.StringBuilder()
        try {
            val nodePkg = node.packageName?.toString() ?: ""
            // Discard status bar and notification shade content if we are not actively inspecting systemui window
            if (activePkg != "com.android.systemui" && nodePkg == "com.android.systemui") {
                return ""
            }
            
            val text = node.text?.toString()?.lowercase()
            val desc = node.contentDescription?.toString()?.lowercase()
            if (text != null) sb.append(text).append(" ")
            if (desc != null) sb.append(desc).append(" ")
            
            val childCount = node.childCount
            for (i in 0 until childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    sb.append(getAllText(child, activePkg, depth + 1))
                }
            }
        } catch (e: Exception) {
            // Safe traversal
        }
        return sb.toString()
    }

    companion object {
        private var lastBlockTime = 0L
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Enforce always-on VPN if it was killed by OS, checking cross-process live interface state
        val prefs = getSafePrefs()
        if (prefs.getBoolean("vpn_is_running", false) && !LocalDnsVpnService.isVpnInterfaceUp()) {
            try {
                val startIntent = Intent(this, LocalDnsVpnService::class.java).apply {
                    action = LocalDnsVpnService.ACTION_START
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(startIntent)
                } else {
                    startService(startIntent)
                }
            } catch (e: Exception) {}
        }
        
        val pkgName = event.packageName?.toString() ?: ""
        if (pkgName == this.packageName) {
            return
        }

        // 1. Skip systemui and launchers to prevent blocking system notifications or normal phone activities.
        if (pkgName == "com.android.systemui" || pkgName.contains("launcher") || pkgName.contains("systemui")) {
            return
        }

        // 2. Prevent scanning or blocking elements on the system lockscreen / keyguard to resolve lockscreen auto-back issues.
        try {
            val km = getSystemService(android.content.Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            if (km.isKeyguardLocked) {
                return // Safe early return
            }
        } catch (e: Exception) {}
        
        // 3. STRICT PRE-FILTERING: Limit protection scanner strictly to settings, installer and VPN packages.
        val isSettingsPkg = pkgName.contains("settings") || pkgName.contains("safecenter") || 
                pkgName.contains("securitycenter") || pkgName.contains("securityadd") || 
                pkgName.contains("safeguard") || pkgName.contains("secure") || pkgName.contains("systemmanager")
        val isInstallerPkg = pkgName.contains("packageinstaller") || pkgName.contains("installer") || pkgName == "android" 
        val isVpnPkg = pkgName.contains("vpndialogs") || pkgName.contains("vpn") || pkgName == "android"
        
        if (!isSettingsPkg && !isInstallerPkg && !isVpnPkg) {
            return // Extremely safe! Return instantly to completely eliminate false-positives
        }
        
        // Check if user has temporarily unlocked settings (30 seconds)
        if (LockManager.isCurrentlyUnlocked(this)) {
            return // Allow access safely
        }
        
        // Always scan rootInActiveWindow to see full screen context (very crucial to see app name alongside uninstall/disconnect buttons!)
        val sourceNode = rootInActiveWindow ?: event.source
        val windowText = if (sourceNode != null) {
            getAllText(sourceNode, pkgName)
        } else {
            ""
        }

        // PRECISE CASE 1: VPN Disconnect system or configure dialog matching our app icon/name
        val isVpnDialogPkg = pkgName == "com.android.vpndialogs" || pkgName.contains("settings") || pkgName == "android"
        if (isVpnDialogPkg) {
            val hasVpnNameOnScreen = windowText.contains("purity tracker") || windowText.contains("purity shield")
            if (hasVpnNameOnScreen) {
                val hasDisconnectKeywords = windowText.contains("disconnect") || 
                        windowText.contains("configure") || 
                        windowText.contains("duration") || 
                        windowText.contains("conversation") || 
                        windowText.contains("00:") ||
                        windowText.contains("সংযোগ বিচ্ছিন্ন") || 
                        windowText.contains("ডিসকানেক্ট") || 
                        windowText.contains("কনফিগার") ||
                        windowText.contains("সময়কাল")
                
                // Ensure we do NOT block the initial Connection Request dialog when user manually starts it
                val isConnectionRequest = windowText.contains("wants to set up") || 
                        windowText.contains("connection request") || 
                        windowText.contains("অনুরোধ") || 
                        windowText.contains("সংযোগ স্থাপন করতে চায়")
                
                if (hasDisconnectKeywords && !isConnectionRequest) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBlockTime > 1500) { // 1.5 seconds cooldown
                        lastBlockTime = currentTime
                        
                        performGlobalAction(GLOBAL_ACTION_HOME) // Go home to be safe
                        performGlobalAction(GLOBAL_ACTION_BACK) // Auto back to dismiss the disconnect dialog
                        
                        triggerGuardianAlert("attempted to disconnect or configure the protection shield")
                        
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            // Open MainActivity with the meditation dialog to cool down their emotion
                            val intent = Intent(this, MainActivity::class.java).apply {
                                action = "com.example.MOTIVATION"
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }
                            startActivity(intent)
                        }, 500)
                    }
                    return
                }
            }
        }

        // To protect the app from being uninstalled or modified, let's setup precise targets.
        // We only trigger when "purity tracker" or "purity shield" is explicitly listed as the active configuration subject.
        val hasAppNameOnScreen = windowText.contains("purity tracker") || windowText.contains("purity shield")
        if (!hasAppNameOnScreen) {
            return // Never block if our app name is not prominently shown on the active screen!
        }

        // PRECISE CASE 2: App Info page of Purity Tracker specifically in active screen
        val isOurAppInfoPage = (pkgName.contains("settings") || pkgName.contains("safecenter") || pkgName.contains("securitycenter") || pkgName.contains("packageinstaller") || pkgName == "android") &&
                windowText.contains("force stop") &&
                (windowText.contains("uninstall") || windowText.contains("storage usage") || windowText.contains("data usage") || windowText.contains("permissions") || windowText.contains("battery") || windowText.contains("version 1.0"))

        // PRECISE CASE 3: Uninstall Confirmation dialog specifically for Purity Tracker
        val isOurAppUninstallDialog = (pkgName.contains("packageinstaller") || pkgName.contains("settings") || pkgName.contains("installer") || pkgName == "android") &&
                (windowText.contains("uninstall") || windowText.contains("delete") || windowText.contains("আনইনস্টল") || windowText.contains("করতে চান")) &&
                (windowText.contains("do you want to") || windowText.contains("uninstallation") || windowText.contains("this app") || windowText.contains("আপনি কি"))

        // PRECISE CASE 4: Accessibility settings screen to turn off Purity Tracker
        val isOurAccessibilitySettings = (pkgName.contains("settings") || pkgName.contains("accessibility")) &&
                (windowText.contains("use purity tracker") || windowText.contains("purity tracker use") || windowText.contains("stop purity tracker") || windowText.contains("turn off purity") || windowText.contains("shortcut"))

        // PRECISE CASE 5: Storage Details settings screen to Clear Data / Clear Cache of Purity Tracker
        val isOurStorageSettings = (pkgName.contains("settings") || pkgName.contains("safecenter")) &&
                (windowText.contains("clear data") || windowText.contains("clear storage") || windowText.contains("clear cache") || windowText.contains("manage space"))

        // PRECISE CASE 6: Device Administration screen to deactivate or disable permissions for Purity Tracker
        val isOurDeviceAdminSettings = (pkgName.contains("settings") || pkgName.contains("safecenter") || pkgName == "android") &&
                (windowText.contains("device admin") || windowText.contains("admin app") || windowText.contains("deactivate"))

        val shouldBlockModification = isOurAppInfoPage || isOurAppUninstallDialog || isOurAccessibilitySettings || isOurStorageSettings || isOurDeviceAdminSettings

        if (shouldBlockModification) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBlockTime > 1500) { // Coercion cooldown
                lastBlockTime = currentTime
                
                performGlobalAction(GLOBAL_ACTION_HOME) // Safely exit the settings screen entirely
                performGlobalAction(GLOBAL_ACTION_BACK) // Auto back to cancel settings modification or uninstall screen
                
                triggerGuardianAlert()
                
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    if (LockManager.hasPin(this)) {
                        val intent = Intent(this, PinLockActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            action = "com.example.MOTIVATION"
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        startActivity(intent)
                    }
                }, 600) // 600ms delay to make sure BACK and HOME events are completely consumed
            }
        }
    }

    private fun triggerGuardianAlert(reason: String = "attempted to disable protection shield or uninstall the app") {
        GuardianManager.triggerGuardianAlert(this, reason)
    }

    override fun onInterrupt() {}
}
