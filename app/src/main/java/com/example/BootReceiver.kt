package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("VpnPrefs", Context.MODE_PRIVATE)
        val isVpnRunning = prefs.getBoolean("vpn_is_running", false)
        
        if (isVpnRunning) {
            // Check if we have VPN permission
            val vpnIntent = android.net.VpnService.prepare(context)
            if (vpnIntent == null) {
                try {
                    val serviceIntent = Intent(context, LocalDnsVpnService::class.java).apply { action = LocalDnsVpnService.ACTION_START }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {}
            }
        }
    }
}
