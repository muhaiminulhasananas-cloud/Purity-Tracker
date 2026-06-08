package com.example

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log

class LocalDnsVpnService : VpnService() {

    companion object {
        const val ACTION_START = "com.example.START_VPN"
        const val ACTION_STOP = "com.example.STOP_VPN"
        var isRunning = false

        fun isVpnInterfaceUp(): Boolean {
            try {
                val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
                if (interfaces != null) {
                    for (intf in java.util.Collections.list(interfaces)) {
                        if (intf.isUp && (intf.name.contains("tun") || intf.name.contains("ppp") || intf.name.contains("p2p") || intf.name.contains("vpn"))) {
                            return true
                        }
                    }
                }
            } catch (e: Exception) {}
            return false
        }
    }

    private var vpnInterface: ParcelFileDescriptor? = null

    private fun getSafePrefs(): android.content.SharedPreferences {
        val safeContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            createDeviceProtectedStorageContext()
        } else {
            this
        }
        return safeContext.getSharedPreferences("VpnPrefs", android.content.Context.MODE_PRIVATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If the service receives STOP, don't stop it if protection should be permanent!
        // The user wants it to be impossible to turn off normally.
        if (intent?.action == ACTION_STOP) {
            val prefs = getSafePrefs()
            val isLocked = prefs.getBoolean("vpn_is_running", true)
            // Still allow programmatic stop if we really need it, but keep state
            // Let's just always restart it.
        }
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (vpnInterface != null) return
        try {
            val builder = Builder()
            builder.setSession("Purity Shield VPN")
            // Configure AdGuard Family DNS (blocks adult content)
            builder.addDnsServer("94.140.14.15")
            builder.addDnsServer("94.140.15.16")
            
            // Give the VPN interface an IP address
            builder.addAddress("10.0.0.2", 32)
            // No full-catch routes so normal traffic still flows securely over default network
            // BUT we add allowBypass() so system doesn't block traffic that doesn't go through VPN
            builder.allowBypass()
            // Add a completely unused dummy route just to make Android think the VPN is "valid"
            builder.addRoute("10.255.255.255", 32)
            
            val configureIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                action = "com.example.MOTIVATION"
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                this, 0, configureIntent, 
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            builder.setConfigureIntent(pendingIntent)

            vpnInterface = builder.establish()

            // Start a backend thread to drain the interface.
            // On some devices, failing to read from the established ParcelFileDescriptor
            // causes the system to close the interface, resulting in the VPN turning off.
            val fd = vpnInterface?.fileDescriptor
            if (fd != null) {
                Thread {
                    try {
                        val input = java.io.FileInputStream(fd)
                        val buffer = ByteArray(32768)
                        while (this.vpnInterface != null) {
                            val read = input.read(buffer)
                            if (read <= 0) {
                                Thread.sleep(200)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("VPN_SERVICE", "Packet drain loop stopped", e)
                    }
                }.start()
            }

            // Save state so BootReceiver knows to restart it
            val prefs = getSafePrefs()
            prefs.edit().putBoolean("vpn_is_running", true).apply()

            // START FOREGROUND NOTIFICATION
            val channelId = "vpn_active_guard"
            val nm = getSystemService(android.app.NotificationManager::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(channelId, "Active Protection", android.app.NotificationManager.IMPORTANCE_LOW)
                nm.createNotificationChannel(channel)
            }
            val notif = androidx.core.app.NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setContentTitle("Guardian Shield is Active")
                .setContentText("Your internet is protected. Tap to meditate for 15 minutes.")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
                
            if (android.os.Build.VERSION.SDK_INT >= 34) {
                startForeground(1, notif, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(1, notif)
            }

            Log.d("VPN_SERVICE", "VPN Started Successfully (Lightweight DNS Mode)")
        } catch (e: Exception) {
            Log.e("VPN_SERVICE", "Error starting VPN", e)
        }
        isRunning = vpnInterface != null
    }

    private fun stopVpn() {
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e("VPN_SERVICE", "Error stopping VPN", e)
        }
        isRunning = false
    }

    override fun onRevoke() {
        super.onRevoke()
        Log.d("VPN_SERVICE", "VPN Revoked by user!")
        stopVpn()
        
        // Immediately restart the VPN if user tried to revoke it! Force protection!
        val prefs = getSafePrefs()
        prefs.edit().putBoolean("vpn_is_running", true).apply()
        startVpn()
        
        // Fallback notification in case background start fails
        try {
            val channelId = "vpn_guard"
            val nm = getSystemService(android.app.NotificationManager::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(channelId, "Protection Alerts", android.app.NotificationManager.IMPORTANCE_HIGH)
                nm.createNotificationChannel(channel)
            }
            val intent = Intent()
            val pi = android.app.PendingIntent.getActivity(this, 100, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
            val notif = androidx.core.app.NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("শিল্ড ব্যাকগ্রাউন্ডে চলছে!")
                .setContentText("আপনি VPN বন্ধ করার চেষ্টা করেছেন, কিন্তু শিল্ড ব্যাকগ্রাউন্ডে পুনরায় চালু হয়েছে।")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()
            nm.notify(999, notif)
        } catch (e: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        val prefs = getSafePrefs()
        val shouldBeRunning = prefs.getBoolean("vpn_is_running", false)
        if (shouldBeRunning) {
            Log.d("VPN_SERVICE", "VPN destroyed unexpectedly! Restarting to enforce protection.")
            val restartIntent = Intent(this, LocalDnsVpnService::class.java).apply {
                action = ACTION_START
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(restartIntent)
            } else {
                startService(restartIntent)
            }
        }
    }
}
