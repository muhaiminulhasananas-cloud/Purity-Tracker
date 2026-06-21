package com.example.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.blur

// Glassmorphism design tokens matching high-end iOS aesthetics
val GlassBgColorStart = Color(0x331E293B) // Frosted Slate Translucent Dark
val GlassBgColorEnd = Color(0x730F172A)   // Safe contrast backfill
val GlassBorderColorStart = Color(0x59FFFFFF) // Light Reflection Edge
val GlassBorderColorEnd = Color(0x0DFFFFFF)   // Low Intensity reflection edge

fun Modifier.glassCard(
    cornerSize: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    glassAlpha: Float = 1f
): Modifier = this.then(
    Modifier
        .graphicsLayer {
            clip = true
            shape = RoundedCornerShape(cornerSize)
        }
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1E2D4A).copy(alpha = 0.85f * glassAlpha),
                    Color(0xFF0F172A).copy(alpha = 0.95f * glassAlpha)
                )
            )
        )
        .border(
            borderWidth,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.18f),
                    Color.White.copy(alpha = 0.04f),
                    Color.Black.copy(alpha = 0.15f)
                )
            ),
            shape = RoundedCornerShape(cornerSize)
        )
)

@Composable
fun GlassBackground(
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")
    
    // Shifting coordinates to make the aurora effect animate beautifully
    val scaleFactor1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spot1"
    )
    val scaleFactor2 by infiniteTransition.animateFloat(
        initialValue = 1.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spot2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19)) // Deepest space background
    ) {
        Canvas(modifier = Modifier.fillMaxSize().blur(radius = 120.dp)) {
            val width = size.width
            val height = size.height
            
            // Neon Cyan Aura Top-Left
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF06B6D4).copy(alpha = 0.18f),
                        Color(0xFF06B6D4).copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    radius = width * 0.7f * scaleFactor1
                ),
                center = Offset(width * 0.2f, height * 0.2f)
            )
            
            // Royal Purple Aura Bottom-Right
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6366F1).copy(alpha = 0.22f),
                        Color(0xFF4F46E5).copy(alpha = 0.07f),
                        Color.Transparent
                    ),
                    radius = width * 0.8f * scaleFactor2
                ),
                center = Offset(width * 0.8f, height * 0.7f)
            )
            
            // Secondary glowing accents to enrich depth (Emerald/Green/Blue hues)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF10B981).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    radius = width * 0.5f
                ),
                center = Offset(width * 0.5f, height * 0.45f)
            )
        }
        content()
    }
}
