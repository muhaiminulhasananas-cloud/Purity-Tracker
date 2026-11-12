package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val callId = intent.getStringExtra("callId") ?: ""

        val nm = NotificationManagerCompat.from(context)
        nm.cancel(callId.hashCode())

        when (action) {
            "ACTION_ACCEPT_CALL" -> {
                VoiceCallManager.acceptCall(context)
                val fullScreenIntent = Intent(context, IncomingCallActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
                context.startActivity(fullScreenIntent)
            }
            "ACTION_DECLINE_CALL" -> {
                VoiceCallManager.endCall()
            }
        }
    }
}
