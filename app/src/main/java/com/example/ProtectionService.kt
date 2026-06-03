package com.example

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class ProtectionService : AccessibilityService() {

    private var screenReceiver: android.content.BroadcastReceiver? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        this.serviceInfo = info
        
        // Ensure VPN is running as early as possible after a reboot or restart
        val prefs = applicationContext.getSharedPreferences("VpnPrefs", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("vpn_is_running", false) && !LocalDnsVpnService.isRunning) {
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
                    val prefs = applicationContext.getSharedPreferences("VpnPrefs", android.content.Context.MODE_PRIVATE)
                    if (prefs.getBoolean("vpn_is_running", false) && !LocalDnsVpnService.isRunning) {
                        try {
                            val startIntent = Intent(context, LocalDnsVpnService::class.java).apply {
                                action = LocalDnsVpnService.ACTION_START
                            }
                            startService(startIntent)
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
        return super.onUnbind(intent)
    }

    private fun getAllText(node: android.view.accessibility.AccessibilityNodeInfo?, depth: Int = 0): String {
        if (node == null || depth > 40) return ""
        val sb = java.lang.StringBuilder()
        try {
            val text = node.text?.toString()?.lowercase()
            val desc = node.contentDescription?.toString()?.lowercase()
            if (text != null) sb.append(text).append(" ")
            if (desc != null) sb.append(desc).append(" ")
            
            val childCount = node.childCount
            for (i in 0 until childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    sb.append(getAllText(child, depth + 1))
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
        // Enforce always-on VPN if it was killed by OS
        val prefs = applicationContext.getSharedPreferences("VpnPrefs", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("vpn_is_running", false) && !LocalDnsVpnService.isRunning) {
            try {
                val startIntent = Intent(this, LocalDnsVpnService::class.java).apply {
                    action = LocalDnsVpnService.ACTION_START
                }
                startService(startIntent)
                LocalDnsVpnService.isRunning = true // Optimistic flag
            } catch (e: Exception) {}
        }
        
        val pkgName = event.packageName?.toString() ?: ""
        if (pkgName == this.packageName) {
            return
        }
        
        // Check if user has temporarily unlocked settings (30 seconds)
        if (LockManager.isCurrentlyUnlocked(this)) {
            return // Allow access safely
        }
        
        // Always scan rootInActiveWindow to see full screen context (very crucial to see app name alongside uninstall/disconnect buttons!)
        val sourceNode = rootInActiveWindow ?: event.source
        val allText = if (sourceNode != null) {
            (getAllText(sourceNode) + " " + event.text.toString().lowercase())
        } else {
            event.text.toString().lowercase()
        }

        // To ensure we ONLY block when it relates to our app, verify the app's name is on screen
        val isTargetApp = allText.contains("purity tracker") || allText.contains("purity shield") || allText.contains("purity shield vpn")
        
        if (!isTargetApp) {
            return // Never block if our app's name has not been detected on the screen!
        }

        // CASE B: VPN Disconnect system dialog (com.android.vpndialogs or containing disconnect/configure buttons)
        val isVpnDialog = pkgName.contains("vpndialogs") || pkgName.contains("vpn")
        val isVpnAction = allText.contains("disconnect") || allText.contains("configure") || allText.contains("connection request")
        
        val shouldBlockVpn = isVpnDialog || isVpnAction

        if (shouldBlockVpn) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBlockTime > 1000) { // 1 second cooldown
                lastBlockTime = currentTime
                
                performGlobalAction(GLOBAL_ACTION_BACK) // Auto back to dismiss the disconnect dialog
                
                triggerGuardianAlert("attempted to disconnect or configure the protection shield")
                
                // Open MainActivity with the meditation dialog to cool down their emotion
                val intent = Intent(this, MainActivity::class.java).apply {
                    action = "com.example.MOTIVATION"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
            }
            return
        }

        // CASE A: App Settings / App Info / Uninstaller Dialog / Clear Data attempts (Force stop, Force uninstall etc.)
        val hasAppModification = allText.contains("uninstall") || allText.contains("delete") || allText.contains("আনইনস্টল") || 
            allText.contains("force stop") || allText.contains("clear data") || allText.contains("clear cache") || 
            allText.contains("clear storage") || allText.contains("device admin") || allText.contains("app info") || 
            allText.contains("accessibility") || allText.contains("অ্যাক্সেসিবিলিটি") || allText.contains("purity tracker use") || 
            allText.contains("use purity tracker")

        if (hasAppModification) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBlockTime > 1000) { // 1 second coercion cooldown
                lastBlockTime = currentTime
                
                performGlobalAction(GLOBAL_ACTION_BACK) // Auto back to cancel force stop, clear data, or uninstall dialog
                
                triggerGuardianAlert()
                
                if (LockManager.hasPin(this)) {
                    val intent = Intent(this, PinLockActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    startActivity(intent)
                } else {
                    performGlobalAction(GLOBAL_ACTION_HOME) // Go home as fallback if no security pin is set
                    val intent = Intent(this, MainActivity::class.java).apply {
                        action = "com.example.MOTIVATION"
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun triggerGuardianAlert(reason: String = "attempted to disable protection shield or uninstall the app") {
        GuardianManager.triggerGuardianAlert(this, reason)
    }

    override fun onInterrupt() {}
}
