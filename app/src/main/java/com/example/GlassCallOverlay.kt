package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.CyanAccent
import com.example.ui.theme.GlassTheme

@Composable
fun GlassCallOverlay(
    callSession: ActiveCallSession?,
    onAccept: () -> Unit,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit
) {
    AnimatedVisibility(
        visible = callSession != null,
        enter = fadeIn(tween(300)) + scaleIn(tween(300, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(250)) + scaleOut(tween(250))
    ) {
        if (callSession == null) return@AnimatedVisibility

        val context = LocalContext.current
        val isBn = LanguageStore.currentLanguage.value == "bn"
        val isLight = GlassTheme.isLightMode.value

        val peerName = if (callSession.role == CallRole.CALLER) callSession.receiverName else callSession.callerName
        val peerPhoto = if (callSession.role == CallRole.CALLER) callSession.receiverPhoto else callSession.callerPhoto

        val isConnected = callSession.status == CallStatus.CONNECTED
        val isIncomingRinging = callSession.role == CallRole.RECEIVER && callSession.status == CallStatus.RINGING

        // Pulse animation for avatar ring during calling/ringing
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 0.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )

        // Ultra Premium Smoked Glass Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF090D16).copy(alpha = 0.96f),
                            Color(0xFF0F172A).copy(alpha = 0.98f),
                            Color(0xFF05070C)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Glass Ambient Glow Orbs
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .scale(pulseScale)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                (if (isConnected) Color(0xFF10B981) else Color(0xFF00B4D8)).copy(alpha = pulseAlpha),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Status Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (callSession.isVideo) {
                                if (isBn) "রিয়েল-টাইম ভিডিও কল 📹" else "Real-time Video Call 📹"
                            } else {
                                if (isBn) "রিয়েল-টাইম অডিও কল 🎙️" else "Real-time Voice Call 🎙️"
                            },
                            color = CyanAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = peerName.ifBlank { if (isBn) "বন্ধু" else "Friend" },
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Call Status or Duration
                    val statusText = when (callSession.status) {
                        CallStatus.CALLING -> if (isBn) "কল করা হচ্ছে..." else "Calling..."
                        CallStatus.RINGING -> if (isBn) "রিং হচ্ছে..." else "Ringing..."
                        CallStatus.CONNECTED -> {
                            val mins = callSession.durationSeconds / 60
                            val secs = callSession.durationSeconds % 60
                            String.format("%02d:%02d", mins, secs)
                        }
                        CallStatus.ENDED -> if (isBn) "কল শেষ" else "Call Ended"
                        else -> ""
                    }

                    Text(
                        text = statusText,
                        color = if (isConnected) Color(0xFF34D399) else Color(0xFF94A3B8),
                        fontSize = if (isConnected) 20.sp else 15.sp,
                        fontWeight = if (isConnected) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Center Glass Avatar Card
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(170.dp)
                ) {
                    // Outer animated pulse ring
                    if (!isConnected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(pulseScale)
                                .border(
                                    2.dp,
                                    (if (isIncomingRinging) Color(0xFF10B981) else CyanAccent).copy(alpha = pulseAlpha),
                                    CircleShape
                                )
                        )
                    }

                    // Inner Glass Avatar Container
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(3.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (peerPhoto.isNotBlank()) {
                            BadgeImage(
                                imageUrl = peerPhoto,
                                modifier = Modifier.fillMaxSize(),
                                fallbackName = peerName
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = peerName,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(72.dp)
                            )
                        }
                    }
                }

                // Bottom Action Buttons Panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White.copy(alpha = 0.07f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    if (isIncomingRinging) {
                        // Incoming Call Actions: Accept (Green) and Decline (Red)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Decline
                            CallActionButton(
                                icon = Icons.Filled.CallEnd,
                                label = if (isBn) "কাটুন" else "Decline",
                                iconColor = Color.White,
                                containerColor = Color(0xFFEF4444),
                                size = 64.dp,
                                onClick = onEndCall
                            )

                            // Accept
                            CallActionButton(
                                icon = Icons.Filled.Call,
                                label = if (isBn) "ধরুন" else "Accept",
                                iconColor = Color.White,
                                containerColor = Color(0xFF10B981),
                                size = 64.dp,
                                onClick = onAccept
                            )
                        }
                    } else {
                        // Connected or Outgoing Actions: Mute, Speaker, End Call
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mute/Unmute
                            CallActionButton(
                                icon = if (callSession.isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                                label = if (callSession.isMuted) (if (isBn) "আনমিউট" else "Unmute") else (if (isBn) "মিউট" else "Mute"),
                                iconColor = if (callSession.isMuted) Color(0xFFEF4444) else Color.White,
                                containerColor = if (callSession.isMuted) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.12f),
                                size = 56.dp,
                                onClick = onToggleMute
                            )

                            // End Call (Hang up)
                            CallActionButton(
                                icon = Icons.Filled.CallEnd,
                                label = if (isBn) "কাটুন" else "End",
                                iconColor = Color.White,
                                containerColor = Color(0xFFEF4444),
                                size = 64.dp,
                                onClick = onEndCall
                            )

                            // Speaker On/Off
                            CallActionButton(
                                icon = if (callSession.isSpeakerOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                                label = if (callSession.isSpeakerOn) (if (isBn) "ইয়ারপিস" else "Earpiece") else (if (isBn) "স্পিকার" else "Speaker"),
                                iconColor = if (callSession.isSpeakerOn) CyanAccent else Color.White,
                                containerColor = if (callSession.isSpeakerOn) CyanAccent.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f),
                                size = 56.dp,
                                onClick = onToggleSpeaker
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallActionButton(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    containerColor: Color,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(containerColor)
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(size * 0.48f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
