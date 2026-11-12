package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Global Theme Switcher & Settings
object GlassTheme {
    val isLightMode = mutableStateOf(false)
}

// Signature Pure Black & Luminous Glassmorphism Accent Palette
val ChatGptEmerald = Color(0xFF10B981)
val KimiCyan = Color(0xFF38BDF8)
val KimiTeal = Color(0xFF2DD4BF)
val AiSlateGlow = Color(0xFF38BDF8)
val AiIndigoAccent = Color(0xFF818CF8)
val AiAmberAccent = Color(0xFFF59E0B)
val MacRedPill = Color(0xFFE50914)

// Pure Black & Frosted Glass Studio Tokens
val ObsidianCanvas = Color(0xFF000000)       // Pure black background (#000000)
val ObsidianSurface = Color(0xFF0A0A0A)      // Deep black surface
val ObsidianCard = Color(0x1FFFFFFF)         // White frosted glass top
val ObsidianCardEnd = Color(0x12FFFFFF)      // White frosted glass bottom
val ObsidianBorder = Color(0x38FFFFFF)       // Specular white rim border

// Porcelain Light Studio Tokens
val PorcelainCanvas = Color(0xFFF4F7FB)      // Clean minimalist porcelain
val PorcelainSurface = Color(0xFFFFFFFF)
val PorcelainCard = Color(0xFFFFFFFF)
val PorcelainCardEnd = Color(0xFFF1F5F9)
val PorcelainBorder = Color(0xFFCBD5E1)

// Harmonious Glassmorphism Accent Aliases
val TelegramBlue = Color(0xFF38BDF8)
val TelegramBlueDark = Color(0xFF0284C7)
val TelegramSky = Color(0xFF38BDF8)
val TelegramCyan = Color(0xFF38BDF8)
val TelegramEmerald = ChatGptEmerald
val TelegramAmber = AiAmberAccent
val TelegramCrimson = MacRedPill
val TelegramPurple = Color(0xFFA78BFA)

val TelegramDarkBg = ObsidianCanvas
val TelegramDarkSurface = ObsidianSurface
val TelegramDarkCard = ObsidianCard
val TelegramDarkHighlight = Color(0x38FFFFFF) // rgba(255, 255, 255, 0.22)

val TelegramLightBg = PorcelainCanvas
val TelegramLightSurface = PorcelainSurface
val TelegramLightCard = PorcelainCard
val TelegramLightBorder = PorcelainBorder

// Pre-allocated Brushes for 120Hz Zero-Allocation White Frosted Glass Rendering
private val DarkGlassBgBrush = Brush.verticalGradient(
    0.0f to Color.White.copy(alpha = 0.11f),
    0.5f to Color.White.copy(alpha = 0.08f),
    1.0f to Color.White.copy(alpha = 0.06f)
)
private val LightGlassBgBrush = Brush.verticalGradient(
    0.0f to Color.White.copy(alpha = 0.96f),
    0.5f to Color(0xFFF6F9FD).copy(alpha = 0.94f),
    1.0f to Color.White.copy(alpha = 0.95f)
)
private val DarkGlassBorderBrush = Brush.verticalGradient(
    0.0f to Color.White.copy(alpha = 0.30f),
    0.15f to Color.White.copy(alpha = 0.15f),
    0.85f to Color.White.copy(alpha = 0.15f),
    1.0f to Color.White.copy(alpha = 0.22f)
)
private val LightGlassBorderBrush = Brush.verticalGradient(
    0.0f to Color.White.copy(alpha = 0.99f),
    0.15f to Color(0xFFCBD5E1).copy(alpha = 0.85f),
    0.85f to Color(0xFFCBD5E1).copy(alpha = 0.85f),
    1.0f to Color.White.copy(alpha = 0.95f)
)
private val DarkInnerGlassBgBrush = Brush.verticalGradient(
    0.0f to Color.White.copy(alpha = 0.12f),
    1.0f to Color.White.copy(alpha = 0.07f)
)
private val LightInnerGlassBgBrush = Brush.verticalGradient(
    0.0f to Color.White.copy(alpha = 0.98f),
    1.0f to Color(0xFFF8FAFC).copy(alpha = 0.96f)
)
private val SelectedPillBrush = Brush.verticalGradient(
    listOf(Color(0xFF38BDF8).copy(alpha = 0.24f), Color(0xFF0284C7).copy(alpha = 0.20f))
)
private val UnselectedPillBrush = Brush.verticalGradient(
    listOf(Color.Transparent, Color.Transparent)
)
private val DarkTextShadow = Shadow(
    color = Color.Black.copy(alpha = 0.90f),
    offset = Offset(0f, 1.8f),
    blurRadius = 2.5f
)
private val LightTextShadow = Shadow(
    color = Color.Black.copy(alpha = 0.12f),
    offset = Offset(0f, 1f),
    blurRadius = 1f
)

// Dynamic Text Tokens
val TextPrimary: Color
    @Composable get() = if (GlassTheme.isLightMode.value) Color(0xFF0F172A) else Color(0xFFFFFFFF)

val TextSecondary: Color
    @Composable get() = if (GlassTheme.isLightMode.value) Color(0xFF1E293B) else Color.White.copy(alpha = 0.92f)

val TextMuted: Color
    @Composable get() = if (GlassTheme.isLightMode.value) Color(0xFF475569) else Color.White.copy(alpha = 0.75f)

val TextCaption: Color
    @Composable get() = if (GlassTheme.isLightMode.value) Color(0xFF64748B) else Color.White.copy(alpha = 0.60f)

val TextCyan: Color
    @Composable get() = if (GlassTheme.isLightMode.value) Color(0xFF0284C7) else Color(0xFF38BDF8)

@Composable
fun floatingTextShadow(
    elevation: Dp = 2.dp,
    isLight: Boolean = GlassTheme.isLightMode.value
): Shadow {
    return if (isLight) LightTextShadow else DarkTextShadow
}

/**
 * 120Hz-Optimized Pure Black & White Frosted Glass Card Modifier
 * Zero-allocation static brushes for buttery-smooth scrolling and page transitions.
 */
fun Modifier.glassCard(
    cornerSize: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    glassAlpha: Float = 1f,
    hasSheen: Boolean = true
): Modifier {
    val isLight = GlassTheme.isLightMode.value
    val shape = RoundedCornerShape(cornerSize)
    val bgBrush = if (isLight) LightGlassBgBrush else DarkGlassBgBrush
    val borderBrush = if (isLight) LightGlassBorderBrush else DarkGlassBorderBrush

    return this
        .clip(shape)
        .background(brush = bgBrush, shape = shape)
        .border(borderWidth, brush = borderBrush, shape = shape)
        .then(
            if (hasSheen) {
                Modifier.drawWithContent {
                    val topSheen = Brush.verticalGradient(
                        colors = listOf(
                            if (isLight) Color.White.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.28f
                    )
                    drawRect(brush = topSheen)
                    drawContent()
                }
            } else {
                Modifier
            }
        )
}

/**
 * 120Hz-Optimized Inner Floating Glass Card Modifier
 */
fun Modifier.innerFloatingGlassCard(
    cornerSize: Dp = 14.dp,
    elevation: Dp = 2.dp,
    glassAlpha: Float = 1f,
    borderGlow: Color = Color.White
): Modifier {
    val isLight = GlassTheme.isLightMode.value
    val shape = RoundedCornerShape(cornerSize)
    val bgBrush = if (isLight) LightInnerGlassBgBrush else DarkInnerGlassBgBrush
    val borderColor = if (isLight) borderGlow.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.20f)

    return this
        .clip(shape)
        .background(brush = bgBrush, shape = shape)
        .border(1.dp, borderColor, shape = shape)
}

/**
 * Floating Pill / Capsule Glass Modifier (Deep White Glass when selected)
 */
fun Modifier.floatingPillGlass(
    isSelected: Boolean = false,
    cornerSize: Dp = 14.dp,
    activeColor: Color = Color.White.copy(alpha = 0.22f)
): Modifier {
    val shape = RoundedCornerShape(cornerSize)
    val bgBrush = if (isSelected) SelectedPillBrush else UnselectedPillBrush
    return this
        .clip(shape)
        .background(brush = bgBrush, shape = shape)
        .then(
            if (isSelected) Modifier.border(1.dp, Color.White.copy(alpha = 0.55f), shape)
            else Modifier
        )
}

@Composable
fun GlassDivider(
    modifier: Modifier = Modifier
) {
    val isLight = GlassTheme.isLightMode.value
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                if (isLight) Color(0xFFCBD5E1).copy(alpha = 0.50f) else Color.White.copy(alpha = 0.10f)
            )
    )
}

@Composable
fun GlassTrafficLights(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(9.dp).background(Color(0xFFFF5F56), CircleShape))
        Box(modifier = Modifier.size(9.dp).background(Color(0xFFFFBD2E), CircleShape))
        Box(modifier = Modifier.size(9.dp).background(Color(0xFF27C93F), CircleShape))
    }
}

@Composable
fun GlassSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    val isLight = GlassTheme.isLightMode.value
    Text(
        text = title.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.6.sp,
        color = if (isLight) Color(0xFF64748B) else Color.White.copy(alpha = 0.65f),
        modifier = modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun GlassNotificationBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count <= 0) return
    Box(
        modifier = modifier
            .background(MacRedPill, CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
            .padding(horizontal = 5.dp, vertical = 1.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = Color.White,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GlassCapsuleTabBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val isLight = GlassTheme.isLightMode.value
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isLight) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.07f),
                RoundedCornerShape(18.dp)
            )
            .border(
                1.dp,
                if (isLight) Color(0xFFCBD5E1) else Color.White.copy(alpha = 0.16f),
                RoundedCornerShape(18.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun RowScope.GlassCapsuleTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: @Composable (() -> Unit)? = null,
    badgeCount: Int = 0,
    activeColor: Color = Color(0xFF38BDF8)
) {
    val isLight = GlassTheme.isLightMode.value
    val titleTextColor = if (selected) {
        Color(0xFF061018)
    } else {
        if (isLight) Color(0xFF475569) else Color.White.copy(alpha = 0.72f)
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) activeColor else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
            .then(
                if (selected) {
                    Modifier.border(
                        1.dp,
                        Color(0xFF7DD3FC).copy(alpha = 0.65f),
                        RoundedCornerShape(14.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = titleTextColor,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontSize = 11.5.sp,
                letterSpacing = (-0.2).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(3.dp))
                GlassNotificationBadge(count = badgeCount)
            }
        }
    }
}

@Composable
fun GlassBackground(
    content: @Composable () -> Unit
) {
    val isLight = GlassTheme.isLightMode.value
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isLight) PorcelainCanvas else Color.Black)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (!isLight) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.045f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.50f, h * 0.08f),
                        radius = w * 0.90f
                    )
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF00B4D8).copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(w * 0.85f, h * 0.10f),
                        radius = w * 0.80f
                    )
                )
            }
        }

        content()
    }
}
