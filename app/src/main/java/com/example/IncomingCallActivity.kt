package com.example

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.GlassBackground

class IncomingCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        showWhenLockedAndTurnScreenOn()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val callSession by VoiceCallManager.callSession.collectAsState()

            if (callSession == null || callSession?.status == CallStatus.ENDED || callSession?.status == CallStatus.IDLE) {
                finish()
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF090D16)
            ) {
                GlassCallOverlay(
                    callSession = callSession,
                    onAccept = {
                        VoiceCallManager.acceptCall(this@IncomingCallActivity)
                    },
                    onEndCall = {
                        VoiceCallManager.endCall()
                        finish()
                    },
                    onToggleMute = {
                        VoiceCallManager.toggleMute()
                    },
                    onToggleSpeaker = {
                        VoiceCallManager.toggleSpeaker()
                    }
                )
            }
        }
    }

    private fun showWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, IncomingCallActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            context.startActivity(intent)
        }
    }
}
