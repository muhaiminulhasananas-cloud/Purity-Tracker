package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SplashScreenOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "splash_anim")

        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )

        val shimmerRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerRotation"
        )

        val isBn = LanguageStore.currentLanguage.value == "bn"
        val splashCyan = Color(0xFF00B4D8)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            Color(0xFF0F172A),
                            Color(0xFF050B14)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Logo with glowing pulsing aura ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(140.dp)
                ) {
                    // Outer glow circle
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        splashCyan.copy(alpha = glowAlpha * 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Rotating ambient ring
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .graphicsLayer { rotationZ = shimmerRotation }
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        splashCyan.copy(alpha = 0.8f),
                                        Color(0xFF38BDF8).copy(alpha = 0.2f),
                                        splashCyan.copy(alpha = 0.8f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // App icon launcher badge
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .scale(pulseScale)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color(0xFF0B132B))
                            .border(1.5.dp, splashCyan.copy(alpha = 0.6f), RoundedCornerShape(26.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "Purity Tracker Logo",
                            modifier = Modifier.size(76.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // App Title
                Text(
                    text = if (isBn) "পিউরিটি ট্র্যাকার" else "Purity Tracker",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle
                Text(
                    text = if (isBn) "আত্মশুদ্ধি ও সুস্থ জীবনের সঙ্গী" else "Your Path to Purity & Strength",
                    color = splashCyan.copy(alpha = 0.9f),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Modern 3-dot pulsing loader
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dotScale1 by infiniteTransition.animateFloat(
                        initialValue = 0.6f, targetValue = 1.3f,
                        animationSpec = infiniteRepeatable(tween(600, delayMillis = 0), RepeatMode.Reverse),
                        label = "dot1"
                    )
                    val dotScale2 by infiniteTransition.animateFloat(
                        initialValue = 0.6f, targetValue = 1.3f,
                        animationSpec = infiniteRepeatable(tween(600, delayMillis = 180), RepeatMode.Reverse),
                        label = "dot2"
                    )
                    val dotScale3 by infiniteTransition.animateFloat(
                        initialValue = 0.6f, targetValue = 1.3f,
                        animationSpec = infiniteRepeatable(tween(600, delayMillis = 360), RepeatMode.Reverse),
                        label = "dot3"
                    )

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(dotScale1)
                            .clip(CircleShape)
                            .background(splashCyan)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(dotScale2)
                            .clip(CircleShape)
                            .background(splashCyan)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(dotScale3)
                            .clip(CircleShape)
                            .background(splashCyan)
                    )
                }
            }
        }
    }
}
