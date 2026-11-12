package com.example

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.widget.VideoView
import android.widget.MediaController
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    var emailInput by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isSentSuccess by remember { mutableStateOf(false) }
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val goldAccent = Color(0xFFE5C168)

    androidx.activity.compose.BackHandler {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(
                1.dp,
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        goldAccent.copy(alpha = 0.42f),
                        Color.White.copy(alpha = 0.16f),
                        goldAccent.copy(alpha = 0.22f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1F1D1A).copy(alpha = 0.96f),
                                Color(0xFF15161B).copy(alpha = 0.98f),
                                Color(0xFF0E1015)
                            )
                        )
                    )
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.10f), CircleShape)
                            .border(1.dp, goldAccent.copy(alpha = 0.35f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = goldAccent,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isBn) "পাসওয়ার্ড রিকভারি" else "Forgot Password",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(goldAccent.copy(alpha = 0.15f), CircleShape)
                        .border(1.4.dp, goldAccent.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSentSuccess) Icons.Filled.MarkEmailRead else Icons.Filled.LockReset,
                        contentDescription = null,
                        tint = goldAccent,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isSentSuccess) {
                        if (isBn) "রিকভারি লিঙ্ক পাঠানো হয়েছে! ✓" else "Recovery Link Sent! ✓"
                    } else {
                        if (isBn) "পাসওয়ার্ড রিসেট করুন" else "Reset Your Password"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 21.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isSentSuccess) {
                        if (isBn)
                            "$emailInput ঠিকানায় পাসওয়ার্ড রিসেট করার লিঙ্ক পাঠানো হয়েছে। আপনার ইনবক্স বা স্প্যাম চেক করুন।"
                        else
                            "A password reset link has been sent to $emailInput. Please check your Inbox or Spam folder."
                    } else {
                        if (isBn)
                            "আপনার অ্যাকাউন্টের নিবন্ধিত ইমেইল ঠিকানাটি নিচে লিখুন। আমরা পাসওয়ার্ড রিসেট লিংক পাঠাব।"
                        else
                            "Enter your registered email address and we will send you a secure reset link."
                    },
                    color = Color(0xFFB0B8C8),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (!isSentSuccess) {
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text(if (isBn) "আপনার ইমেইল ঠিকানা" else "Email Address") },
                        placeholder = { Text("you@example.com", color = Color.White.copy(alpha = 0.42f)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = goldAccent, modifier = Modifier.size(19.dp))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = goldAccent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = goldAccent,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.70f),
                            focusedContainerColor = Color(0xFF0B0C10).copy(alpha = 0.85f),
                            unfocusedContainerColor = Color(0xFF0B0C10).copy(alpha = 0.85f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFE8C872), Color(0xFFD6AF53))
                                )
                            )
                            .clickable {
                                if (isSending) return@clickable
                                val cleanMail = emailInput.trim()
                                if (cleanMail.isBlank() || !cleanMail.contains("@")) {
                                    Toast.makeText(context, if (isBn) "দয়া করে সঠিক ইমেইল ঠিকানা দিন" else "Please enter a valid email", Toast.LENGTH_SHORT).show()
                                    return@clickable
                                }
                                isSending = true
                                authViewModel.sendPasswordReset(cleanMail) { success, msg ->
                                    isSending = false
                                    if (success) {
                                        isSentSuccess = true
                                    } else {
                                        Toast.makeText(context, msg ?: if (isBn) "ইমেইল পাঠাতে সমস্যা হয়েছে" else "Failed to send reset link", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                color = Color(0xFF0B0D13),
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.2.dp
                            )
                        } else {
                            Text(
                                text = if (isBn) "পাসওয়ার্ড রিসেট লিংক পাঠান  →" else "Send Reset Link  →",
                                color = Color(0xFF0B0D13),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFE8C872), Color(0xFFD6AF53))
                                )
                            )
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBn) "লগইন স্ক্রিনে ফিরে যান  →" else "Return to Login  →",
                            color = Color(0xFF0B0D13),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = onBack) {
                    Text(
                        text = if (isBn) "বাতিল করুন" else "Cancel",
                        color = goldAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun IslamicArchHeroIllustration(
    archHeight: androidx.compose.ui.unit.Dp = 166.dp,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(archHeight)
    ) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f

        // Warm golden ambient glow behind the arch (matching reference screenshot)
        drawCircle(
            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE5C168).copy(alpha = 0.22f),
                    Color(0xFFD4AF37).copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = androidx.compose.ui.geometry.Offset(cx, h * 0.54f),
                radius = w * 0.46f
            ),
            center = androidx.compose.ui.geometry.Offset(cx, h * 0.54f),
            radius = w * 0.46f
        )

        fun createArchPath(halfWidth: Float, topPeakY: Float, shoulderY: Float, bottomY: Float): androidx.compose.ui.graphics.Path {
            return androidx.compose.ui.graphics.Path().apply {
                moveTo(cx - halfWidth, bottomY)
                lineTo(cx - halfWidth, shoulderY)
                cubicTo(
                    cx - halfWidth, shoulderY - (shoulderY - topPeakY) * 0.58f,
                    cx - halfWidth * 0.36f, topPeakY + (shoulderY - topPeakY) * 0.22f,
                    cx, topPeakY
                )
                cubicTo(
                    cx + halfWidth * 0.36f, topPeakY + (shoulderY - topPeakY) * 0.22f,
                    cx + halfWidth, shoulderY - (shoulderY - topPeakY) * 0.58f,
                    cx + halfWidth, shoulderY
                )
                lineTo(cx + halfWidth, bottomY)
            }
        }

        // Outer Arch (Warm Gold fading down)
        val outerPath = createArchPath(
            halfWidth = w * 0.35f,
            topPeakY = h * 0.07f,
            shoulderY = h * 0.54f,
            bottomY = h * 1.05f
        )
        drawPath(
            path = outerPath,
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE5C168).copy(alpha = 0.62f),
                    Color(0xFFE5C168).copy(alpha = 0.22f),
                    Color.Transparent
                )
            ),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.3.dp.toPx())
        )

        // Middle Arch
        val midPath = createArchPath(
            halfWidth = w * 0.27f,
            topPeakY = h * 0.19f,
            shoulderY = h * 0.59f,
            bottomY = h * 1.05f
        )
        drawPath(
            path = midPath,
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE5C168).copy(alpha = 0.45f),
                    Color(0xFFE5C168).copy(alpha = 0.16f),
                    Color.Transparent
                )
            ),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
        )

        // Inner Arch with warm gold-tinted fill and bright champagne-gold rim
        val innerPath = createArchPath(
            halfWidth = w * 0.19f,
            topPeakY = h * 0.31f,
            shoulderY = h * 0.65f,
            bottomY = h * 1.05f
        )
        drawPath(
            path = innerPath,
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE5C168).copy(alpha = 0.14f),
                    Color(0xFF1A160B).copy(alpha = 0.25f),
                    Color.Transparent
                )
            )
        )
        drawPath(
            path = innerPath,
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFEFA6),
                    Color(0xFFE5C168).copy(alpha = 0.55f),
                    Color.Transparent
                )
            ),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6.dp.toPx())
        )

        // Glowing golden orb at top peak of outer arch
        drawCircle(
            color = Color(0xFFE5C168).copy(alpha = 0.35f),
            radius = 5.5.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(cx, h * 0.07f)
        )
        drawCircle(
            color = Color(0xFFE8C872),
            radius = 2.8.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(cx, h * 0.07f)
        )

        // Four-pointed golden stars & starlight dots around the arch
        fun drawFourPointStar(sx: Float, sy: Float, r: Float, starColor: Color) {
            val starPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(sx, sy - r)
                quadraticTo(sx, sy, sx + r, sy)
                quadraticTo(sx, sy, sx, sy + r)
                quadraticTo(sx, sy, sx - r, sy)
                quadraticTo(sx, sy, sx, sy - r)
                close()
            }
            drawPath(starPath, color = starColor)
        }

        drawFourPointStar(cx - w * 0.35f, h * 0.34f, 4.8.dp.toPx(), Color(0xFFE5C168).copy(alpha = 0.90f))
        drawFourPointStar(cx + w * 0.36f, h * 0.38f, 4.2.dp.toPx(), Color(0xFFE5C168).copy(alpha = 0.85f))
        drawCircle(Color(0xFFE5C168).copy(alpha = 0.55f), radius = 1.6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(cx - w * 0.21f, h * 0.13f))
        drawCircle(Color(0xFFE5C168).copy(alpha = 0.55f), radius = 1.6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(cx + w * 0.22f, h * 0.15f))

        // Center Luminous Warm Golden Crescent Moon inside the inner arch (positioned clearly above card overlap)
        val moonCenterX = cx
        val moonCenterY = h * 0.49f
        val moonRadius = 18.5.dp.toPx()
        val crescentPath = androidx.compose.ui.graphics.Path().apply {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    left = moonCenterX - moonRadius,
                    top = moonCenterY - moonRadius,
                    right = moonCenterX + moonRadius,
                    bottom = moonCenterY + moonRadius
                )
            )
        }
        val cutoutPath = androidx.compose.ui.graphics.Path().apply {
            val cutCenterX = moonCenterX + moonRadius * 0.42f
            val cutCenterY = moonCenterY - moonRadius * 0.24f
            val cutRadius = moonRadius * 0.84f
            addOval(
                androidx.compose.ui.geometry.Rect(
                    left = cutCenterX - cutRadius,
                    top = cutCenterY - cutRadius,
                    right = cutCenterX + cutRadius,
                    bottom = cutCenterY + cutRadius
                )
            )
        }
        val finalMoonPath = androidx.compose.ui.graphics.Path().apply {
            op(crescentPath, cutoutPath, androidx.compose.ui.graphics.PathOperation.Difference)
        }
        drawPath(
            path = finalMoonPath,
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(Color(0xFFF3D785), Color(0xFFD4AD4E), Color(0xFFB88E30)),
                start = androidx.compose.ui.geometry.Offset(moonCenterX - moonRadius, moonCenterY - moonRadius),
                end = androidx.compose.ui.geometry.Offset(moonCenterX + moonRadius, moonCenterY + moonRadius)
            )
        )
    }
}

@Composable
private fun StackedAuthInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    rowHeight: androidx.compose.ui.unit.Dp = 46.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFFE2E8F0),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(86.dp)
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color(0xFF757E90),
                    fontSize = 14.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Normal
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = visualTransformation,
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE5C168)),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(4.dp))
            trailingIcon()
        }
    }
}

@Composable
fun ImprovedAuthScreen(
    authViewModel: AuthViewModel,
    userProfileViewModel: UserProfileViewModel,
    purityTrackerViewModel: PurityTrackerViewModel
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val pendingVerificationEmail by authViewModel.pendingVerificationEmail.collectAsState()
    var isForgotPasswordScreen by remember { mutableStateOf(false) }

    if (isForgotPasswordScreen) {
        ForgotPasswordScreen(
            onBack = { isForgotPasswordScreen = false },
            authViewModel = authViewModel
        )
        return
    }

    var isSignUpMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val isBn = LanguageStore.currentLanguage.value == "bn"
    val goldAccent = Color(0xFFE5C168)

    // Check Email Link Logic
    LaunchedEffect(activity?.intent) {
        val currentIntent = activity?.intent
        if (currentIntent != null && currentIntent.data != null) {
            authViewModel.checkAndHandleEmailLink(currentIntent, context) { success, msg ->
                Toast.makeText(context, if (success) "$msg ✅" else "$msg ❌", Toast.LENGTH_LONG).show()
                activity.intent = null
            }
        }
    }

    // Responsive Single-Screen No-Scroll Layout that guarantees 100% visibility of bottom links & developer credit
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 2.dp, bottom = 6.dp)
    ) {
        val availH = maxHeight
        val isCompactScreen = availH < 590.dp

        val archHeight = if (isSignUpMode) {
            if (isCompactScreen) 108.dp else 124.dp
        } else {
            if (isCompactScreen) 124.dp else 142.dp
        }
        val cardTopOffset = if (isSignUpMode) {
            if (isCompactScreen) 64.dp else 78.dp
        } else {
            if (isCompactScreen) 78.dp else 94.dp
        }
        val cardVerticalPad = if (isSignUpMode) {
            if (isCompactScreen) 13.dp else 16.dp
        } else {
            if (isCompactScreen) 15.dp else 18.dp
        }
        val inputRowHeight = if (isSignUpMode) {
            if (isCompactScreen) 41.dp else 44.dp
        } else {
            if (isCompactScreen) 44.dp else 47.dp
        }
        val primaryBtnHeight = if (isSignUpMode) {
            if (isCompactScreen) 44.dp else 47.dp
        } else {
            if (isCompactScreen) 46.dp else 49.dp
        }
        val googleBtnHeight = if (isSignUpMode) {
            if (isCompactScreen) 42.dp else 45.dp
        } else {
            if (isCompactScreen) 44.dp else 47.dp
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Top Centered Italic "Account" Header (Matching Reference Screenshot)
            Text(
                text = if (isBn) "অ্যাকাউন্ট" else "Account",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                letterSpacing = 0.3.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Warm Golden Islamic Arch + Overlapping Dark Frosted Glass Card (Zero Scrolling)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                IslamicArchHeroIllustration(archHeight = archHeight)

                // Main Warm Dark Frosted Glass Card matching Screenshot_2026-11-10-07-26-11-34.jpg
                Card(
                    modifier = Modifier
                        .padding(top = cardTopOffset)
                        .fillMaxWidth()
                        .widthIn(max = 420.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(
                        1.dp,
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                goldAccent.copy(alpha = 0.38f),
                                Color.White.copy(alpha = 0.16f),
                                Color.White.copy(alpha = 0.14f)
                            )
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF211E1A).copy(alpha = 0.96f),
                                        Color(0xFF16161B).copy(alpha = 0.98f),
                                        Color(0xFF0E0F14)
                                    )
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = cardVerticalPad)
                    ) {
                        if (pendingVerificationEmail != null) {
                            // WELL-PROPORTIONED NO-SCROLL FIREBASE EMAIL VERIFICATION CARD
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(goldAccent.copy(alpha = 0.15f), CircleShape)
                                        .border(1.4.dp, goldAccent.copy(alpha = 0.55f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MarkEmailUnread,
                                        contentDescription = "Verify Email",
                                        tint = goldAccent,
                                        modifier = Modifier.size(25.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = if (isBn) "ইমেইল ভেরিফাই করুন" else "Verify your email",
                                    color = Color.White,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (isBn) {
                                        "ভুয়া ইমেইল প্রতিরোধে আমরা ভেরিফিকেশন লিংক পাঠিয়েছি:\n$pendingVerificationEmail\nInbox বা Spam থেকে লিংকটিতে ক্লিক করে নিচের বাটনে চাপ দিন।"
                                    } else {
                                        "We sent a verification link to:\n$pendingVerificationEmail\nClick the link in your Inbox or Spam folder, then tap below."
                                    },
                                    color = Color(0xFFB0B8C8),
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Golden Sand Primary Verification Button
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                colors = listOf(Color(0xFFE8C872), Color(0xFFD6AF53))
                                            )
                                        )
                                        .clickable {
                                            if (isLoading) return@clickable
                                            isLoading = true
                                            authViewModel.checkEmailVerificationStatus { _, msg ->
                                                isLoading = false
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF0B0D13),
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.2.dp
                                        )
                                    } else {
                                        Text(
                                            text = if (isBn) "ভেরিফাই সম্পন্ন করেছি ✓" else "I've Verified · Continue  →",
                                            color = Color(0xFF0B0D13),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    onClick = {
                                        authViewModel.resendVerificationEmail { _, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color(0xFF1E2028).copy(alpha = 0.90f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (isBn) "পুনরায় ভেরিফিকেশন ইমেইল পাঠান" else "Resend Verification Email",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = if (isBn) "অন্য ইমেইল ব্যবহার করুন / ফিরে যান" else "Use another email · Back to Login",
                                    color = goldAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { authViewModel.cancelPendingVerification() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            // FULL-PROPORTION NO-SCROLL LOGIN / SIGN-UP CARD MATCHING REFERENCE SCREENSHOT
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "السلام عليكم",
                                    color = goldAccent,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = if (isSignUpMode) {
                                        if (isBn) "নতুন অ্যাকাউন্ট" else "Create account"
                                    } else {
                                        if (isBn) "স্বাগতম" else "Welcome back"
                                    },
                                    color = Color.White,
                                    fontSize = if (isSignUpMode) 23.sp else 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = if (isSignUpMode) {
                                        if (isBn) "নামাজ, স্ট্রিক ও বন্ধুদের সিঙ্ক করতে রেজিস্ট্রেশন করুন।"
                                        else "Register with your email to sync prayers, streaks and friends."
                                    } else {
                                        if (isBn) "আপনার নামাজ, স্ট্রিক ও বন্ধুদের সিঙ্ক করতে সাইন ইন করুন।"
                                        else "Sign in to sync your prayers, streaks and friends."
                                    },
                                    color = Color(0xFFA0A8B8),
                                    fontSize = 12.5.sp,
                                    lineHeight = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(if (isSignUpMode) 10.dp else 12.dp))

                                // Unified Dark Frosted Glass Stacked Input Box (Matching Reference Screenshot)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF0A0B0F).copy(alpha = 0.88f))
                                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                                ) {
                                    if (isSignUpMode) {
                                        StackedAuthInputRow(
                                            label = if (isBn) "নাম" else "Name",
                                            value = name,
                                            onValueChange = { name = it },
                                            placeholder = if (isBn) "আপনার পূর্ণ নাম" else "Your full name",
                                            rowHeight = inputRowHeight
                                        )
                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = Color.White.copy(alpha = 0.09f)
                                        )
                                    }

                                    StackedAuthInputRow(
                                        label = if (isBn) "ইমেইল" else "Email",
                                        value = email,
                                        onValueChange = { email = it },
                                        placeholder = "you@example.com",
                                        rowHeight = inputRowHeight,
                                        keyboardType = KeyboardType.Email
                                    )

                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = Color.White.copy(alpha = 0.09f)
                                    )

                                    StackedAuthInputRow(
                                        label = if (isBn) "পাসওয়ার্ড" else "Password",
                                        value = password,
                                        onValueChange = { password = it },
                                        placeholder = if (isBn) "পাসওয়ার্ড দিন" else "Enter password",
                                        rowHeight = inputRowHeight,
                                        keyboardType = KeyboardType.Password,
                                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { isPasswordVisible = !isPasswordVisible },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                                    contentDescription = "Toggle password visibility",
                                                    tint = Color(0xFFB8C0D0),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    )
                                }

                                if (!isSignUpMode) {
                                    Spacer(modifier = Modifier.height(7.dp))
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = if (isBn) "পাসওয়ার্ড ভুলে গেছেন?" else "Forgot Password?",
                                            color = goldAccent,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { isForgotPasswordScreen = true }
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(9.dp))
                                } else {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // Primary Action Pill Button — Warm Golden Sand Gradient (Matching Reference Screenshot)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(primaryBtnHeight)
                                        .clip(RoundedCornerShape(25.dp))
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                colors = listOf(Color(0xFFE8C872), Color(0xFFD6AF53))
                                            )
                                        )
                                        .clickable {
                                            if (isLoading) return@clickable
                                            val cleanMail = email.trim()
                                            if (!authViewModel.isValidRealEmail(cleanMail)) {
                                                Toast.makeText(
                                                    context,
                                                    if (isBn) "দয়া করে একটি সঠিক ও আসল ইমেইল দিন (ফেইক ইমেইল গ্রহণযোগ্য নয়)।"
                                                    else "Please enter a valid, real email address (fake/disposable emails are blocked).",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                return@clickable
                                            }
                                            if (password.length < 6) {
                                                Toast.makeText(
                                                    context,
                                                    if (isBn) "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে"
                                                    else "Password must be at least 6 characters",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                return@clickable
                                            }
                                            isLoading = true
                                            if (isSignUpMode) {
                                                if (name.isBlank()) {
                                                    Toast.makeText(
                                                        context,
                                                        if (isBn) "দয়া করে আপনার নাম লিখুন" else "Please enter your name",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    isLoading = false
                                                    return@clickable
                                                }
                                                authViewModel.signUpWithEmail(cleanMail, name, password, context) { _, msg ->
                                                    isLoading = false
                                                    if (msg != null) {
                                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            } else {
                                                authViewModel.signInWithEmail(cleanMail, password, context) { success, msg ->
                                                    isLoading = false
                                                    if (!success && msg != null) {
                                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF0B0D13),
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.2.dp
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = if (isSignUpMode) {
                                                    if (isBn) "অ্যাকাউন্ট তৈরি করুন" else "Create Account"
                                                } else {
                                                    if (isBn) "নিরাপদে লগইন করুন" else "Login Securely"
                                                },
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF0B0D13)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = Color(0xFF0B0D13),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(if (isSignUpMode) 8.dp else 10.dp))

                                // OR Divider
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(1.dp)
                                            .weight(1f)
                                            .background(Color.White.copy(alpha = 0.12f))
                                    )
                                    Text(
                                        text = "   OR   ",
                                        color = Color(0xFF8C94A6),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .height(1.dp)
                                            .weight(1f)
                                            .background(Color.White.copy(alpha = 0.12f))
                                    )
                                }

                                Spacer(modifier = Modifier.height(if (isSignUpMode) 8.dp else 10.dp))

                                // Sleek Dark Frosted Glass "Login with Google" Pill Button (Matching Reference Screenshot)
                                Surface(
                                    onClick = { authViewModel.signInWithGoogle(context) },
                                    shape = RoundedCornerShape(25.dp),
                                    color = Color(0xFF1F2129).copy(alpha = 0.92f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(googleBtnHeight)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_google),
                                            contentDescription = "Google Logo",
                                            modifier = Modifier.size(19.dp),
                                            tint = Color.Unspecified
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = if (isBn) "Google দিয়ে লগইন করুন" else "Login with Google",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(if (isSignUpMode) 8.dp else 10.dp))

                                // New here? Create account toggle (Warm Golden Link matching Reference Screenshot)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isSignUpMode) {
                                            if (isBn) "আগে থেকেই অ্যাকাউন্ট আছে?  " else "Already have an account?  "
                                        } else {
                                            if (isBn) "এখানে নতুন?  " else "New here?  "
                                        },
                                        color = Color(0xFFB8C0D0),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (isSignUpMode) {
                                            if (isBn) "সাইন ইন করুন" else "Sign in"
                                        } else {
                                            if (isBn) "অ্যাকাউন্ট খুলুন" else "Create account"
                                        },
                                        color = goldAccent,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { isSignUpMode = !isSignUpMode }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(if (isSignUpMode) 4.dp else 6.dp))

                                Text(
                                    text = "Developed by Anas",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSetupScreen(
    userProfileViewModel: UserProfileViewModel,
    onSetupComplete: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val isUsernameAvailable by userProfileViewModel.isUsernameAvailable.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descTextColor = if (isLightMode) Color(0xFF475569) else Color.LightGray
    val cardBg = if (isLightMode) Color.White.copy(alpha = 0.9f) else CardBlue

    LaunchedEffect(username) {
        if (username.length >= 3) {
            userProfileViewModel.checkUsernameAvailability(username)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .glassCard(cornerSize = 24.dp, glassAlpha = 0.95f, hasSheen = true),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Complete Your Profile", color = titleTextColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Set a unique username and tell others about yourself.", color = descTextColor, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent, 
                    unfocusedBorderColor = if (isLightMode) Color(0xFFCBD5E1) else Color.Gray,
                    focusedTextColor = titleTextColor, 
                    unfocusedTextColor = titleTextColor,
                    focusedLabelColor = CyanAccent,
                    unfocusedLabelColor = descTextColor
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it.lowercase().replace("\\s".toRegex(), "") },
                label = { Text("Unique Username") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isUsernameAvailable == true) Color.Green else if (isUsernameAvailable == false) Color.Red else CyanAccent,
                    unfocusedBorderColor = if (isLightMode) Color(0xFFCBD5E1) else Color.Gray,
                    focusedTextColor = titleTextColor, 
                    unfocusedTextColor = titleTextColor,
                    focusedLabelColor = CyanAccent,
                    unfocusedLabelColor = descTextColor
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (username.length >= 3) {
                if (isUsernameAvailable == true) {
                    Text("Username available!", color = Color(0xFF10B981), fontSize = 12.sp)
                } else if (isUsernameAvailable == false) {
                    Text("Username is taken.", color = Color.Red, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio (Optional)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent, 
                    unfocusedBorderColor = if (isLightMode) Color(0xFFCBD5E1) else Color.Gray,
                    focusedTextColor = titleTextColor, 
                    unfocusedTextColor = titleTextColor,
                    focusedLabelColor = CyanAccent,
                    unfocusedLabelColor = descTextColor
                ),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (username.length < 3 || isUsernameAvailable != true || name.isBlank()) {
                        Toast.makeText(context, "Please complete required fields properly", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    userProfileViewModel.setUsernameAndProfile(username, bio, name) { success, msg ->
                        isLoading = false
                        if (success) {
                            Toast.makeText(context, "Profile setup complete!", Toast.LENGTH_SHORT).show()
                            onSetupComplete()
                        } else {
                            Toast.makeText(context, msg ?: "Error", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Profile", color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descTextColor = if (isLightMode) Color(0xFF64748B) else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CyanAccent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = descTextColor, fontSize = 11.sp)
            Text(value, color = titleTextColor, fontSize = 14.sp)
        }
    }
}

@Composable
fun UserProfileContent(
    userProfile: UserProfile,
    authViewModel: AuthViewModel,
    purityTrackerViewModel: PurityTrackerViewModel,
    socialViewModel: SocialViewModel,
    userProfileViewModel: UserProfileViewModel,
    isPremiumUserState: Boolean,
    onNavigateToStaticPage: (String) -> Unit,
    onOpenFriendsList: () -> Unit = {},
    onOpenGuardianShield: () -> Unit = {}
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var editBio by remember(userProfile.bio) { mutableStateOf(userProfile.bio) }
    var editBirthDate by remember(userProfile.birthDate) { mutableStateOf(userProfile.birthDate) }
    var editRelationship by remember(userProfile.relationship) { mutableStateOf(userProfile.relationship) }
    var editLocation by remember(userProfile.location) { mutableStateOf(userProfile.location) }
    var editWork by remember(userProfile.work) { mutableStateOf(userProfile.work) }
    var editEducation by remember(userProfile.education) { mutableStateOf(userProfile.education) }
    var editWebsite by remember(userProfile.website) { mutableStateOf(userProfile.website) }

    var showEditUsernameDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showAdminToolsDialog by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var inputNewUsername by remember { mutableStateOf("") }
    var inputNewName by remember { mutableStateOf("") }
    var usernameErrorText by remember { mutableStateOf<String?>(null) }
    var isCheckingUsername by remember { mutableStateOf(false) }

    val friendReqs by socialViewModel.friendRequests.collectAsState()
    val friends by socialViewModel.friends.collectAsState()
    val streakDays by purityTrackerViewModel.durationDays.collectAsState()
    val unlockedBadgesCount = remember(streakDays) {
        maxOf(1, listOf(0, 3, 7, 14, 21, 30, 60, 90, 120, 365).count { streakDays >= it })
    }

    var isUploadingProfilePic by remember { mutableStateOf(false) }

    val profilePicLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            isUploadingProfilePic = true
            Toast.makeText(context, "Uploading profile picture...", Toast.LENGTH_SHORT).show()
            userProfileViewModel.uploadProfilePicture(context, uri) { success, _ ->
                isUploadingProfilePic = false
                if (success) {
                    Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Upload failed, please try again", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descTextColor = if (isLightMode) Color(0xFF475569) else Color(0xFF94A3B8)
    val isBn = LanguageStore.currentLanguage.value == "bn"

    // 1. Compact Profile Card matching Account@1x.png
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerSize = 24.dp, glassAlpha = 0.95f, hasSheen = true),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Centered Circular Avatar with Golden Border (84.dp)
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF232834), CircleShape)
                    .border(2.dp, Color(0xFFF59E0B), CircleShape)
                    .clickable { profilePicLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (isUploadingProfilePic) {
                    CircularProgressIndicator(
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp
                    )
                } else if (userProfile.profilePicUrl.isNotEmpty()) {
                    BadgeImage(
                        imageUrl = userProfile.profilePicUrl,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    val initial = userProfile.name.trim().firstOrNull()?.uppercase() ?: "A"
                    Text(
                        text = initial,
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name + Verified Cyan Check
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable {
                    inputNewName = userProfile.name
                    showEditNameDialog = true
                }
            ) {
                Text(
                    text = userProfile.name.ifBlank { "Warrior" },
                    color = titleTextColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Verified",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // @username · Role Subtitle
            val handleText = "@${userProfile.username.ifBlank { userProfile.userId.take(8) }}"
            val roleText = userProfile.work.ifBlank {
                if (userProfile.email == "muhaiminulhasananas@gmail.com") "Developer" else "Warrior"
            }
            Text(
                text = "$handleText · $roleText",
                color = descTextColor,
                fontSize = 12.5.sp,
                modifier = Modifier.clickable {
                    inputNewUsername = userProfile.username
                    usernameErrorText = null
                    showEditUsernameDialog = true
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3-Column Stats Row: Friends | Badges | Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenFriendsList() }
                ) {
                    Text(
                        text = "${friends.size}",
                        color = titleTextColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isBn) "বন্ধু" else "Friends",
                        color = descTextColor,
                        fontSize = 12.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$unlockedBadgesCount",
                        color = Color(0xFFFBBF24),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isBn) "ব্যাজ" else "Badges",
                        color = descTextColor,
                        fontSize = 12.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${streakDays}d",
                        color = titleTextColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isBn) "স্ট্রিক" else "Streak",
                        color = descTextColor,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2-Button Action Row: [ + Add story ] [ ✎ Edit profile ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "Story feature coming soon ✨", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = Color(0xFF061018),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "স্টোরি যোগ" else "Add story",
                        color = Color(0xFF061018),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { isEditing = !isEditing },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A202C)),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEditing) (if (isBn) "বন্ধ করুন" else "Close") else (if (isBn) "এডিট প্রোফাইল" else "Edit profile"),
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = editBio,
                    onValueChange = { editBio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = titleTextColor,
                        unfocusedTextColor = titleTextColor,
                        focusedLabelColor = Color(0xFF38BDF8),
                        unfocusedLabelColor = descTextColor
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = editWork,
                    onValueChange = { editWork = it },
                    label = { Text("Role / Occupation (e.g. Developer)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = titleTextColor,
                        unfocusedTextColor = titleTextColor,
                        focusedLabelColor = Color(0xFF38BDF8),
                        unfocusedLabelColor = descTextColor
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = editLocation,
                    onValueChange = { editLocation = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = titleTextColor,
                        unfocusedTextColor = titleTextColor,
                        focusedLabelColor = Color(0xFF38BDF8),
                        unfocusedLabelColor = descTextColor
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users").document(uid)
                                .set(
                                    mapOf(
                                        "bio" to editBio,
                                        "birthDate" to editBirthDate,
                                        "relationship" to editRelationship,
                                        "location" to editLocation,
                                        "work" to editWork,
                                        "education" to editEducation,
                                        "website" to editWebsite
                                    ),
                                    com.google.firebase.firestore.SetOptions.merge()
                                )
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                    ) {
                        Text("Save", color = Color(0xFF061018), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            inputNewUsername = userProfile.username
                            usernameErrorText = null
                            showEditUsernameDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Text("Change @handle", color = Color.White)
                    }
                }
            }

            if (friendReqs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F293A))
                        .clickable { onOpenFriendsList() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${friendReqs.size} pending friend request(s)",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("View ›", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Compact 4-Row Menu Glass Card matching Account@1x.png
    var showLogoutDialog by remember { mutableStateOf(false) }
    val guardianShieldActive = remember {
        context.getSharedPreferences("GuardianPrefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("isShieldActive", false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerSize = 20.dp, glassAlpha = 0.95f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row 1: Guardian Shield
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenGuardianShield() }
                    .padding(horizontal = 18.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = if (isBn) "গার্ডিয়ান শিল্ড" else "Guardian Shield",
                        color = titleTextColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = if (guardianShieldActive) (if (isBn) "সক্রিয় ›" else "Active ›") else (if (isBn) "সেট আপ ›" else "Set up ›"),
                    color = descTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Divider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)

            // Row 2: Language
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageMenu = true }
                        .padding(horizontal = 18.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = if (isBn) "ভাষা" else "Language",
                            color = titleTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    val currentLangName = when (LanguageStore.currentLanguage.value) {
                        "bn" -> "বাংলা ›"
                        "hi" -> "हिंदी ›"
                        "es" -> "Español ›"
                        "ar" -> "العربية ›"
                        "ur" -> "اردو ›"
                        else -> "English ›"
                    }
                    Text(
                        text = currentLangName,
                        color = descTextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                DropdownMenu(
                    expanded = showLanguageMenu,
                    onDismissRequest = { showLanguageMenu = false }
                ) {
                    val languages = listOf(
                        "en" to "English",
                        "bn" to "বাংলা",
                        "hi" to "हिंदी",
                        "es" to "Español",
                        "ar" to "العربية",
                        "ur" to "اردو"
                    )
                    languages.forEach { (tag, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                changeAppLocale(context, tag)
                                showLanguageMenu = false
                            }
                        )
                    }
                }
            }

            val currentAuthEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
            val isAdminUser = AccountDeletionManager.isAdminEmail(currentAuthEmail) ||
                AccountDeletionManager.isAdminEmail(userProfile.email)

            if (isAdminUser) {
                Divider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)

                // Row 3: Admin tools (ONLY visible to muhaiminulhasananas@gmail.com)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdminToolsDialog = true }
                        .padding(horizontal = 18.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = if (isBn) "অ্যাডমিন টুলস" else "Admin tools",
                            color = titleTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(50))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Admin",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("›", color = descTextColor, fontSize = 15.sp)
                    }
                }
            }

            Divider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)

            // Row 4: Log out
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutDialog = true }
                    .padding(horizontal = 18.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = if (isBn) "লগ আউট" else "Log out",
                    color = Color(0xFFF87171),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Divider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)

            // Row 5: Delete account (Wipes Firebase Auth, Leaderboard, Username & all user data)
            var showDeleteAccountDialog by remember { mutableStateOf(false) }
            var isDeletingMyAccount by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isDeletingMyAccount) { showDeleteAccountDialog = true }
                    .padding(horizontal = 18.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = if (isBn) "অ্যাকাউন্ট ডিলিট করুন" else "Delete account",
                        color = Color(0xFFEF4444),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text("›", color = Color(0xFFEF4444).copy(alpha = 0.8f), fontSize = 15.sp)
            }

            if (showDeleteAccountDialog) {
                AlertDialog(
                    onDismissRequest = { if (!isDeletingMyAccount) showDeleteAccountDialog = false },
                    containerColor = Color(0xFF0A0A0D),
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteForever,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "অ্যাকাউন্ট স্থায়ীভাবে ডিলিট করুন" else "Delete Account Permanently",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 17.sp
                            )
                        }
                    },
                    text = {
                        Text(
                            text = if (isBn)
                                "আপনি কি নিশ্চিতভাবে আপনার অ্যাকাউন্ট ডিলিট করতে চান? এটি করলে লিডারবোর্ড, ফায়ারবেস, আপনার @${userProfile.username} ইউজারনেম এবং সকল ডাটা চিরতরে মুছে যাবে।"
                            else
                                "Are you sure you want to permanently delete your account? Your leaderboard rank, Firebase account, @${userProfile.username} username, and all data will be erased immediately.",
                            color = Color.White.copy(alpha = 0.82f),
                            fontSize = 13.5.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isDeletingMyAccount = true
                                authViewModel.deleteCurrentAccount(userProfile.username) { _, _ ->
                                    isDeletingMyAccount = false
                                    showDeleteAccountDialog = false
                                    purityTrackerViewModel.clearAllData()
                                    userProfileViewModel.clearProfile()
                                    Toast.makeText(
                                        context,
                                        if (isBn) "অ্যাকাউন্ট ও সকল ডাটা সফলভাবে ডিলিট হয়েছে।" else "Account & all data deleted permanently.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            enabled = !isDeletingMyAccount,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            if (isDeletingMyAccount) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isBn) "স্থায়ীভাবে ডিলিট করুন" else "Delete Forever",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteAccountDialog = false },
                            enabled = !isDeletingMyAccount
                        ) {
                            Text(if (isBn) "বাতিল" else "Cancel", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                )
            }
        }
    }

    var showAdminUsersDialog by remember { mutableStateOf(false) }

    if (showAdminToolsDialog) {
        AlertDialog(
            onDismissRequest = { showAdminToolsDialog = false },
            containerColor = Color(0xFF0A0A0D),
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Admin Tools", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            showAdminToolsDialog = false
                            onNavigateToStaticPage("admin_badges")
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                    ) {
                        Text("Manage Badges", color = Color(0xFF061018), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            showAdminToolsDialog = false
                            onNavigateToStaticPage("admin_payments")
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24))
                    ) {
                        Text("Review Payments", color = Color(0xFF061018), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            showAdminToolsDialog = false
                            showAdminUsersDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text(
                            text = if (isBn) "ইউজার ও গোস্ট অ্যাকাউন্ট ক্লিনআপ" else "Manage Users & Ghost Cleanup",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAdminToolsDialog = false }) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    if (showAdminUsersDialog) {
        var allUsersList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
        var isLoadingUsers by remember { mutableStateOf(true) }
        var isPurgingGhosts by remember { mutableStateOf(false) }

        fun reloadUsers() {
            isLoadingUsers = true
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnSuccessListener { snap ->
                    allUsersList = snap.documents.mapNotNull { doc ->
                        if (doc.getBoolean("isDeleted") == true) null
                        else doc.toObject(UserProfile::class.java)?.copy(userId = doc.id)
                    }
                    isLoadingUsers = false
                }
                .addOnFailureListener {
                    isLoadingUsers = false
                }
        }

        LaunchedEffect(Unit) {
            reloadUsers()
        }

        AlertDialog(
            onDismissRequest = { showAdminUsersDialog = false },
            containerColor = Color(0xFF0A0A0D),
            shape = RoundedCornerShape(20.dp),
            title = {
                Column {
                    Text(
                        text = if (isBn) "সকল ইউজার ও ডাটাবেজ ক্লিনআপ (${allUsersList.size})" else "All Users & Cleanup (${allUsersList.size})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBn) "যেকোনো ডিলিট করা বা অতিরিক্ত অ্যাকাউন্ট মুছে ফেললে লিডারবোর্ড ও সার্চ থেকে সাথে সাথে মুছে যাবে।" else "Deleting any account removes it from Leaderboard, Search & Usernames.",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.5.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                    Button(
                        onClick = {
                            isPurgingGhosts = true
                            AccountDeletionManager.cleanupOrphanedAndGhostAccounts { count ->
                                isPurgingGhosts = false
                                reloadUsers()
                                Toast.makeText(
                                    context,
                                    if (isBn) "ক্লিনআপ সম্পন্ন হয়েছে ($count টি গোস্ট রেকর্ড মুছে ফেলা হয়েছে)" else "Cleanup finished ($count ghost records purged)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = !isPurgingGhosts,
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.14f))
                    ) {
                        if (isPurgingGhosts) {
                            CircularProgressIndicator(modifier = Modifier.size(15.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isBn) "অটো গোস্ট ও অরফান ইউজারনেম ক্লিন করুন" else "Auto-Purge Ghost & Orphaned Usernames",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isLoadingUsers) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        }
                    } else if (allUsersList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No users found", color = Color.White.copy(alpha = 0.6f))
                        }
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allUsersList.size) { idx ->
                                val itemUser = allUsersList[idx]
                                val isSelfAdmin = AccountDeletionManager.isAdminEmail(itemUser.email)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = itemUser.name.ifBlank { "Unnamed Warrior" },
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "@${itemUser.username.ifBlank { "no_username" }} • ${itemUser.email.ifBlank { itemUser.userId.take(8) }}",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (!isSelfAdmin) {
                                        IconButton(
                                            onClick = {
                                                val targetId = itemUser.userId
                                                allUsersList = allUsersList.filterNot { it.userId == targetId }
                                                AccountDeletionManager.purgeUserCompletely(
                                                    userId = targetId,
                                                    username = itemUser.username,
                                                    email = itemUser.email,
                                                    isSelfDelete = false
                                                ) { _, _ ->
                                                    Toast.makeText(
                                                        context,
                                                        if (isBn) "@${itemUser.username} সম্পূর্ণ ডিলিট করা হয়েছে" else "Purged @${itemUser.username} completely",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.DeleteForever,
                                                contentDescription = "Delete User",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAdminUsersDialog = false }) {
                    Text(if (isBn) "বন্ধ করুন" else "Close", color = Color.White)
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = if (isLightMode) Color.White else Color(0xFF0A0A0D),
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = if (isBn) "লগ আউট নিশ্চিত করুন" else "Confirm Log Out",
                    fontWeight = FontWeight.Bold,
                    color = if (isLightMode) Color.Black else Color.White
                )
            },
            text = {
                Text(
                    text = if (isBn) "আপনি কি নিশ্চিতভাবে এই ডিভাইস থেকে লগ আউট করতে চান?" else "Are you sure you want to log out from this device?",
                    color = if (isLightMode) Color.DarkGray else Color.LightGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.signOut()
                        purityTrackerViewModel.clearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(if (isBn) "লগ আউট" else "Log Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Dialog to change Username
    if (showEditUsernameDialog) {
        val isBn = LanguageStore.currentLanguage.value == "bn"
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { 
                if (!isCheckingUsername) showEditUsernameDialog = false 
            },
            containerColor = if (isLightMode) Color.White else Color(0xFF1E293B),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AlternateEmail, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "ইউজারনেম পরিবর্তন করুন" else "Change Username",
                        color = titleTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (isBn) "অনন্য ইউজারনেম দিন (ন্যূনতম ৩ অক্ষর, ছোট হাতের অক্ষর ও সংখ্যা):" else "Enter a unique username (min 3 chars, letters/numbers):",
                        color = descTextColor,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputNewUsername,
                        onValueChange = { inputNewUsername = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' } },
                        label = { Text("Username") },
                        prefix = { Text("@", color = CyanAccent, fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = if (isLightMode) Color(0xFFCBD5E1) else Color.Gray,
                            focusedTextColor = titleTextColor,
                            unfocusedTextColor = titleTextColor
                        )
                    )
                    if (usernameErrorText != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = usernameErrorText!!,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = inputNewUsername.lowercase().trim()
                        if (clean.length < 3) {
                            usernameErrorText = if (isBn) "কমপক্ষে ৩ অক্ষরের হতে হবে" else "Must be at least 3 characters"
                            return@Button
                        }
                        isCheckingUsername = true
                        usernameErrorText = null
                        userProfileViewModel.updateUsername(clean) { success, err ->
                            isCheckingUsername = false
                            if (success) {
                                showEditUsernameDialog = false
                                Toast.makeText(context, if (isBn) "ইউজারনেম সফলভাবে পরিবর্তন হয়েছে! ✅" else "Username updated successfully! ✅", Toast.LENGTH_SHORT).show()
                            } else {
                                usernameErrorText = if (err?.contains("taken", ignoreCase = true) == true) {
                                    if (isBn) "এই ইউজারনেমটি অন্য কেউ নিয়ে নিয়েছে!" else "This username is already taken!"
                                } else {
                                    err ?: "Failed to update"
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    enabled = !isCheckingUsername
                ) {
                    if (isCheckingUsername) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(if (isBn) "সেভ করুন" else "Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditUsernameDialog = false },
                    enabled = !isCheckingUsername
                ) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = descTextColor)
                }
            }
        )
    }

    // Dialog to change Display Name
    if (showEditNameDialog) {
        val isBn = LanguageStore.currentLanguage.value == "bn"
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = if (isLightMode) Color.White else Color(0xFF1E293B),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Badge, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "নাম পরিবর্তন করুন" else "Change Display Name",
                        color = titleTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (isBn) "আপনার নাম লিখুন যা অন্যরা দেখবে:" else "Enter your full display name:",
                        color = descTextColor,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputNewName,
                        onValueChange = { inputNewName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = if (isLightMode) Color(0xFFCBD5E1) else Color.Gray,
                            focusedTextColor = titleTextColor,
                            unfocusedTextColor = titleTextColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = inputNewName.trim()
                        if (clean.isEmpty()) return@Button
                        userProfileViewModel.updateDisplayName(clean) { success, err ->
                            if (success) {
                                showEditNameDialog = false
                                Toast.makeText(context, if (isBn) "নাম সফলভাবে আপডেট হয়েছে! ✅" else "Name updated successfully! ✅", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, err ?: "Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text(if (isBn) "সেভ করুন" else "Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = descTextColor)
                }
            }
        )
    }
}

@Composable
fun FriendsListScreen(
    socialViewModel: SocialViewModel,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onViewProfile: ((UserProfile) -> Unit)? = null
) {
    val friends by socialViewModel.sortedFriends.collectAsState()
    val friendRequests by socialViewModel.friendRequests.collectAsState()
    var selectedTab by remember { mutableStateOf("friends") } // "friends", "requests", or "search"
    
    // Search States
    var searchText by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(searchText) {
        if (searchText.isBlank()) {
            searchResults = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        val lowercaseQuery = searchText.lowercase().trim()
        val listener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .whereGreaterThanOrEqualTo("username", lowercaseQuery)
            .whereLessThanOrEqualTo("username", lowercaseQuery + "\uf8ff")
            .limit(20)
            .addSnapshotListener { snapshot, e ->
                isSearching = false
                if (e != null || snapshot == null) {
                    searchResults = emptyList()
                    return@addSnapshotListener
                }
                val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                val users = snapshot.documents.mapNotNull { doc ->
                    if (doc.getBoolean("isDeleted") == true) return@mapNotNull null
                    val profile = doc.toObject(UserProfile::class.java)?.copy(userId = doc.id) ?: return@mapNotNull null
                    if (profile.username.isBlank()) return@mapNotNull null
                    if (profile.email.isNotBlank() && !AccountDeletionManager.isValidRealEmail(profile.email)) return@mapNotNull null
                    profile
                }.filter { it.userId != currentUid } // Exclude current user from search
                searchResults = users
            }
        
        try {
            kotlinx.coroutines.awaitCancellation()
        } finally {
            listener.remove()
        }
    }

    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descTextColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val tabContainerBg = if (isLightMode) Color(0xFFE2E8F0) else CardBlue
    val dividerColor = if (isLightMode) Color(0xFFCBD5E1) else Color.White.copy(alpha = 0.1f)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = if (LanguageStore.currentLanguage.value == "bn") "বন্ধু তালিকা" else "Friends",
            color = titleTextColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Pill Tab Bar matching Friends@1x.png
        GlassCapsuleTabBar(
            modifier = Modifier.fillMaxWidth()
        ) {
            GlassCapsuleTabItem(
                selected = selectedTab == "friends",
                onClick = { selectedTab = "friends" },
                label = "Friends ${friends.size}"
            )

            GlassCapsuleTabItem(
                selected = selectedTab == "requests",
                onClick = { selectedTab = "requests" },
                label = "Requests",
                badgeCount = friendRequests.size
            )

            GlassCapsuleTabItem(
                selected = selectedTab == "search",
                onClick = { selectedTab = "search" },
                label = "Search"
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == "friends") {
            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(friends.size) { index ->
                    val friendId = friends[index]
                    FriendListItem(
                        friendId = friendId,
                        socialViewModel = socialViewModel,
                        onOpenChat = onOpenChat,
                        onViewProfile = onViewProfile
                    )
                }
                item {
                    // "+ Find new friends" full-width glass button matching Friends@1x.png
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .glassCard(cornerSize = 18.dp, glassAlpha = 0.9f)
                            .clickable { selectedTab = "search" },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (LanguageStore.currentLanguage.value == "bn") "নতুন বন্ধু খুঁজুন" else "Find new friends",
                                color = Color(0xFF38BDF8),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else if (selectedTab == "requests") {
            if (friendRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No pending friend requests.", color = descTextColor)
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(friendRequests.size) { index ->
                        val request = friendRequests[index]
                        FriendRequestItem(request = request, socialViewModel = socialViewModel)
                    }
                }
            }
        } else {
            // Search Tab UI
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it.lowercase().replace("\\s".toRegex(), "") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by username...", color = descTextColor, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = descTextColor) },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear", tint = descTextColor)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = if (isLightMode) Color(0xFFCBD5E1) else Color.Gray,
                        focusedBorderColor = CyanAccent,
                        unfocusedTextColor = titleTextColor,
                        focusedTextColor = titleTextColor
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isSearching) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CyanAccent)
                    }
                } else if (searchText.isBlank()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = if (isLightMode) Color(0xFF94A3B8) else Color.Gray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Type a username to start searching", color = if (isLightMode) Color(0xFF94A3B8) else Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else if (searchResults.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No matching users found.", color = descTextColor)
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn {
                        items(searchResults.size) { index ->
                            val user = searchResults[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onViewProfile?.invoke(user) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val searchPresence = UserPresenceManager.formatPresenceStatus(
                                    isOnline = user.isOnline,
                                    lastSeen = user.lastSeen,
                                    isBn = LanguageStore.currentLanguage.value == "bn"
                                )
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    if (user.profilePicUrl.isNotEmpty()) {
                                        BadgeImage(
                                            imageUrl = user.profilePicUrl,
                                            modifier = Modifier.size(48.dp).clip(CircleShape)
                                        )
                                    } else {
                                        Box(modifier = Modifier.size(48.dp).background(if (isLightMode) Color(0xFFE2E8F0) else Color.Gray, CircleShape), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.Person, contentDescription = null, tint = if (isLightMode) Color(0xFF64748B) else Color.White, modifier = Modifier.size(28.dp))
                                        }
                                    }
                                    if (searchPresence.isOnline) {
                                        Box(
                                            modifier = Modifier
                                                .size(13.dp)
                                                .background(if (isLightMode) Color.White else Color(0xFF0F172A), CircleShape)
                                                .padding(2.dp)
                                                .background(Color(0xFF31A24C), CircleShape)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.name, color = titleTextColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("@${user.username}", color = CyanAccent, fontSize = 13.sp)
                                    if (user.bio.isNotEmpty()) {
                                        Text(user.bio, color = descTextColor, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }

                                val isAdminViewer = AccountDeletionManager.isAdminEmail(
                                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
                                )
                                if (isAdminViewer && !AccountDeletionManager.isAdminEmail(user.email)) {
                                    IconButton(
                                        onClick = {
                                            val targetId = user.userId
                                            searchResults = searchResults.filterNot { it.userId == targetId }
                                            AccountDeletionManager.purgeUserCompletely(
                                                userId = targetId,
                                                username = user.username,
                                                email = user.email,
                                                isSelfDelete = false
                                            )
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteForever,
                                            contentDescription = "Delete Ghost User",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Icon(Icons.Filled.ChevronRight, contentDescription = "View Profile", tint = descTextColor)
                            }
                            
                            if (index < searchResults.size - 1) {
                                Divider(color = dividerColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendRequestItem(request: FriendRequest, socialViewModel: SocialViewModel) {
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    val premiumUserEmails by socialViewModel.premiumUserEmails.collectAsState()
    val premiumUsers by socialViewModel.premiumUsers.collectAsState()
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descTextColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    
    LaunchedEffect(request.fromUserId, request.toUserId) {
        val uid = if (request.fromUserId == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid) request.toUserId else request.fromUserId
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    userProfile = snapshot.toObject(UserProfile::class.java)?.copy(userId = snapshot.id)
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .innerFloatingGlassCard(cornerSize = 16.dp, elevation = 2.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (userProfile != null && userProfile!!.profilePicUrl.isNotEmpty()) {
                    coil.compose.AsyncImage(
                        model = userProfile!!.profilePicUrl,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp).clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.size(42.dp).background(if (isLightMode) Color(0xFFE2E8F0) else Color(0xFF334155), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = if (isLightMode) Color(0xFF64748B) else Color.LightGray)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                val name = userProfile?.name?.takeIf { it.isNotBlank() } ?: "User"
                val isPremium = userProfile?.isPremium == true || 
                                userProfile?.email == "muhaiminulhasananas@gmail.com" ||
                                premiumUsers.contains(userProfile?.userId ?: "") ||
                                premiumUserEmails.contains(userProfile?.email ?: "")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.example.PremiumNameText(
                        name = name,
                        isPremium = isPremium,
                        defaultColor = titleTextColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isPremium) {
                        Spacer(modifier = Modifier.width(4.dp))
                        com.example.ProBlueBadge(size = 15.dp)
                    }
                    if (userProfile?.email == "muhaiminulhasananas@gmail.com") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(15.dp))
                    }
                }
            }
            
            val isSentByMe = request.fromUserId == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (isSentByMe) {
                Text("Pending", color = descTextColor, fontSize = 12.sp)
            } else {
                Row {
                    IconButton(onClick = { socialViewModel.acceptFriendRequest(request.fromUserId) }, modifier = Modifier.background(Color(0xFF3B82F6), CircleShape).size(34.dp)) {
                        Icon(Icons.Filled.Check, contentDescription = "Accept", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { socialViewModel.rejectFriendRequest(request.fromUserId) }, modifier = Modifier.background(Color(0xFFEF4444), CircleShape).size(34.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Reject", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FriendListItem(
    friendId: String,
    socialViewModel: SocialViewModel,
    onOpenChat: (String) -> Unit,
    onViewProfile: ((UserProfile) -> Unit)? = null
) {
    var friendProfile by remember { mutableStateOf<UserProfile?>(null) }
    val unreadCounts by socialViewModel.unreadCounts.collectAsState()
    val unreadCount = unreadCounts[friendId] ?: 0
    val premiumUserEmails by socialViewModel.premiumUserEmails.collectAsState()
    val premiumUsers by socialViewModel.premiumUsers.collectAsState()
    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descTextColor = if (isLightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    LaunchedEffect(friendId) {
        val listener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(friendId).addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    friendProfile = snapshot.toObject(UserProfile::class.java)?.copy(userId = snapshot.id)
                }
            }
        kotlinx.coroutines.awaitCancellation()
    }

    val isBn = LanguageStore.currentLanguage.value == "bn"
    val presence = UserPresenceManager.formatPresenceStatus(
        isOnline = friendProfile?.isOnline ?: false,
        lastSeen = friendProfile?.lastSeen ?: 0L,
        isBn = isBn
    )

    val name = friendProfile?.name?.takeIf { it.isNotBlank() } ?: "Friend"
    val initialLetter = name.trim().firstOrNull()?.uppercase() ?: "F"
    val isPremium = friendProfile?.isPremium == true ||
                    friendProfile?.email == "muhaiminulhasananas@gmail.com" ||
                    premiumUsers.contains(friendId) ||
                    premiumUserEmails.contains(friendProfile?.email ?: "")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerSize = 18.dp, glassAlpha = 0.95f)
            .clickable { onOpenChat(friendId) }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    if (friendProfile != null && friendProfile!!.profilePicUrl.isNotEmpty()) {
                        BadgeImage(
                            imageUrl = friendProfile!!.profilePicUrl,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable(enabled = onViewProfile != null) { onViewProfile?.invoke(friendProfile!!) },
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF262C3A), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                                .clickable(enabled = onViewProfile != null && friendProfile != null) { onViewProfile?.invoke(friendProfile!!) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initialLetter,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bottom-right online green or offline gray dot matching Friends@1x.png
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .background(Color(0xFF10141C), CircleShape)
                            .padding(2.dp)
                            .background(
                                if (presence.isOnline) Color(0xFF22C55E) else Color(0xFF64748B),
                                CircleShape
                            )
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        com.example.PremiumNameText(
                            name = name,
                            isPremium = isPremium,
                            defaultColor = titleTextColor,
                            fontSize = 15.5.sp,
                            fontWeight = if (unreadCount > 0) FontWeight.ExtraBold else FontWeight.Bold
                        )
                        if (isPremium) {
                            Spacer(modifier = Modifier.width(4.dp))
                            com.example.ProBlueBadge(size = 14.dp)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (presence.isOnline) (if (isBn) "সক্রিয়" else "Active now") else (if (isBn) "অফলাইন" else "Offline"),
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .sizeIn(minWidth = 20.dp, minHeight = 20.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                            .padding(horizontal = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(unreadCount.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Sky-cyan rounded square chat button matching Friends@1x.png
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF38BDF8))
                        .clickable { onOpenChat(friendId) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Chat",
                        tint = Color(0xFF061018),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GlassChatIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    tint: Color,
    containerColor: Color,
    borderColor: Color = Color.Transparent,
    size: androidx.compose.ui.unit.Dp = 38.dp,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor, CircleShape)
            .then(
                if (borderColor != Color.Transparent) Modifier.border(1.dp, borderColor, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun ChatMediaThumbnail(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = "Media",
    contentScale: androidx.compose.ui.layout.ContentScale = androidx.compose.ui.layout.ContentScale.Crop
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    if (imageUrl.startsWith("data:image")) {
        var bitmap by remember(imageUrl) { mutableStateOf(FastBitmapCache.get(imageUrl)) }
        LaunchedEffect(imageUrl) {
            if (bitmap == null) {
                bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    FastBitmapCache.decodeAndCache(imageUrl)
                }
            }
        }
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        } else {
            Box(modifier = modifier.background(Color(0xFF151924)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Image, contentDescription = null, tint = Color.White.copy(alpha = 0.35f))
            }
        }
    } else {
        val imageRequest = remember(imageUrl) {
            coil.request.ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .size(coil.size.Size.ORIGINAL) // Load original pixel density for crisp HD rendering
                .build()
        }
        coil.compose.AsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
fun PrivateChatScreen(
    friendId: String,
    socialViewModel: SocialViewModel,
    onBack: () -> Unit,
    onViewProfile: ((UserProfile) -> Unit)? = null
) {
    val messages by socialViewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val currentUserUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current
    val isMePremium by com.example.PremiumManager.isPremium.collectAsState()
    val premiumUserEmails by socialViewModel.premiumUserEmails.collectAsState()
    val premiumUsers by socialViewModel.premiumUsers.collectAsState()

    var friendProfile by remember { mutableStateOf(UserProfile(userId = friendId)) }
    var myProfile by remember { mutableStateOf(UserProfile(userId = currentUserUid)) }

    LaunchedEffect(currentUserUid) {
        if (currentUserUid.isNotEmpty()) {
            val listener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(currentUserUid).addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        myProfile = snapshot.toObject(UserProfile::class.java)?.copy(userId = snapshot.id) ?: myProfile
                    }
                }
            kotlinx.coroutines.awaitCancellation()
        }
    }

    LaunchedEffect(friendId) {
        val listener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(friendId).addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    friendProfile = snapshot.toObject(UserProfile::class.java)?.copy(userId = snapshot.id) ?: friendProfile
                }
            }
        kotlinx.coroutines.awaitCancellation()
    }

    val voicePlayerManager = remember { VoicePlayerManager(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableStateOf(0) }
    var mediaRecorder by remember { mutableStateOf<android.media.MediaRecorder?>(null) }
    val recordFilePath = remember { java.io.File(context.cacheDir, "temp_private_voice.3gp").absolutePath }
    
    var msgToEditOrDelete by remember { mutableStateOf<SocialMessage?>(null) }
    var editText by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDurationSec = 0
            while (isRecording) {
                kotlinx.coroutines.delay(1000)
                recordingDurationSec += 1
            }
        }
    }

    DisposableEffect(Unit) {
        ChatNavigationState.isInsideChat.value = true
        onDispose {
            ChatNavigationState.isInsideChat.value = false
            voicePlayerManager.stop()
            try { mediaRecorder?.apply { stop(); release() } } catch (e: Exception) {}
        }
    }

    val startRecordingLambda = {
        try {
            val file = java.io.File(recordFilePath)
            if (file.exists()) file.delete()
            val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                android.media.MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                android.media.MediaRecorder()
            }.apply {
                setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                setOutputFormat(android.media.MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordFilePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "রেকর্ডিং শুরু করা যায়নি!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val stopAndSendRecordingLambda = {
        try { mediaRecorder?.apply { stop(); release() } } catch (e: Exception) {} finally { mediaRecorder = null; isRecording = false }
        val base64 = fileToBase64(recordFilePath)
        if (base64.isNotEmpty() && recordingDurationSec > 0) {
            socialViewModel.sendMessage(friendId, "ভয়েস মেসেজ (স্পিকার টিপুন) 🎙️", audioUrl = base64)
        }
    }

    val cancelRecordingLambda = {
        try { mediaRecorder?.apply { stop(); release() } } catch (e: Exception) {} finally { mediaRecorder = null; isRecording = false }
        try { java.io.File(recordFilePath).delete() } catch (e: Exception) {}
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) startRecordingLambda()
            else android.widget.Toast.makeText(context, "ভয়েস রেকর্ডের জন্য অনুগ্রহ করে অনুমতি প্রদান করুন", android.widget.Toast.LENGTH_SHORT).show()
        }
    )

    val isBn = LanguageStore.currentLanguage.value == "bn"
    val isUploadingMedia by socialViewModel.isUploadingMedia.collectAsState()
    val uploadProgressText by socialViewModel.uploadProgressText.collectAsState()

    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var fullScreenVideoUrl by remember { mutableStateOf<String?>(null) }
    var showMediaOptionsDialog by remember { mutableStateOf(false) }

    val chatImagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            socialViewModel.sendHdImage(context, friendId, uri, caption = messageText.trim())
            messageText = ""
        }
    }

    val chatVideoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            socialViewModel.sendHdVideo(context, friendId, uri, caption = messageText.trim())
            messageText = ""
        }
    }

    val chatCameraPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            socialViewModel.sendLiveCameraPhoto(context, friendId, bitmap, caption = messageText.trim())
            messageText = ""
        }
    }

    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            chatCameraPicker.launch(null)
        } else {
            Toast.makeText(context, if (isBn) "ক্যামেরার অনুমতি প্রদান করুন" else "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val audioPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            VoiceCallManager.startCall(
                context = context,
                callerId = currentUserUid,
                callerName = myProfile.name.ifBlank { "User" },
                callerPhoto = myProfile.profilePicUrl,
                receiverId = friendId,
                receiverName = friendProfile.name.ifBlank { "Friend" },
                receiverPhoto = friendProfile.profilePicUrl,
                isVideo = false
            )
        } else {
            Toast.makeText(context, if (isBn) "কল করার জন্য মাইক্রোফোন পারমিশন প্রয়োজন" else "Microphone permission required for call", Toast.LENGTH_SHORT).show()
        }
    }

    var reactingMessageId by remember { mutableStateOf<String?>(null) }
    var showQuickEmojis by remember { mutableStateOf(false) }

    LaunchedEffect(friendId) {
        socialViewModel.listenForMessages(friendId)
        socialViewModel.markChatAsRead(friendId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            socialViewModel.markChatAsRead(friendId)
        }
    }

    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descTextColor = if (isLightMode) Color(0xFF475569) else Color(0xFF94A3B8)
    val canvasBg = if (isLightMode) com.example.ui.theme.PorcelainCanvas else com.example.ui.theme.ObsidianCanvas
    val glassHeaderBg = if (isLightMode) Color.White.copy(alpha = 0.92f) else Color(0xFF13141D).copy(alpha = 0.90f)
    val glassHeaderBorder = if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.10f)
    val glassBottomBarBg = if (isLightMode) Color.White.copy(alpha = 0.94f) else Color(0xFF11121A).copy(alpha = 0.92f)
    val glassInputBg = if (isLightMode) Color(0xFFF1F5F9) else Color(0xFF1B1C26)
    val glassInputBorder = if (isLightMode) Color(0xFFCBD5E1) else Color.White.copy(alpha = 0.12f)
    val myBubbleGradient = androidx.compose.ui.graphics.Brush.horizontalGradient(
        listOf(Color(0xFF00B4D8), Color(0xFF0284C7))
    )
    val friendBubbleBg = if (isLightMode) Color.White else Color(0xFF1D1E28)
    val friendBubbleBorder = if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.12f)
    val friendTextColor = if (isLightMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    Column(modifier = Modifier.fillMaxSize().background(canvasBg)) {
        // Glass Navigation Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(glassHeaderBg)
                .border(BorderStroke(1.dp, androidx.compose.ui.graphics.Brush.verticalGradient(listOf(glassHeaderBorder, Color.Transparent))))
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            GlassChatIconButton(
                icon = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
                tint = titleTextColor,
                containerColor = if (isLightMode) Color.Black.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.08f),
                borderColor = if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.12f),
                size = 38.dp,
                iconSize = 20.dp
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Active Online Dot (Facebook style) & status text
            val chatPresence = UserPresenceManager.formatPresenceStatus(
                isOnline = friendProfile.isOnline,
                lastSeen = friendProfile.lastSeen,
                isBn = isBn
            )

            // Avatar with Active Online Dot
            Box(
                modifier = Modifier.clickable(enabled = onViewProfile != null) {
                    onViewProfile?.invoke(friendProfile)
                }
            ) {
                if (friendProfile.profilePicUrl.isNotEmpty()) {
                    BadgeImage(
                        imageUrl = friendProfile.profilePicUrl,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, CyanAccent.copy(alpha = 0.6f), CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isLightMode) Color(0xFFE2E8F0) else Color(0xFF1E293B), CircleShape)
                            .border(1.5.dp, CyanAccent.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                    }
                }

                if (chatPresence.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd)
                            .background(if (isLightMode) Color.White else Color(0xFF0C0D12), CircleShape)
                            .padding(1.5.dp)
                            .background(Color(0xFF31A24C), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // User Info & Status
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onViewProfile != null) {
                        onViewProfile?.invoke(friendProfile)
                    }
            ) {
                val isFriendPremium = friendProfile.isPremium || 
                                      friendProfile.email == "muhaiminulhasananas@gmail.com" ||
                                      premiumUsers.contains(friendId) ||
                                      premiumUserEmails.contains(friendProfile.email)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.example.PremiumNameText(
                        name = friendProfile.name.ifBlank { "Friend" },
                        isPremium = isFriendPremium,
                        defaultColor = titleTextColor,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isFriendPremium) {
                        Spacer(modifier = Modifier.width(4.dp))
                        com.example.ProBlueBadge(size = 13.dp)
                    }
                    if (friendProfile.email == "muhaiminulhasananas@gmail.com") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(13.dp))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chatPresence.isOnline) {
                        Box(modifier = Modifier.size(5.5.dp).background(Color(0xFF31A24C), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = chatPresence.statusText,
                            color = Color(0xFF31A24C),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = chatPresence.statusText,
                            color = descTextColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Top Bar Glass Action Buttons (In a tight spaced row so they never overlap)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassChatIconButton(
                    icon = Icons.Filled.Call,
                    contentDescription = "Voice Call",
                    onClick = {
                        val hasAudioPerm = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (hasAudioPerm) {
                            VoiceCallManager.startCall(
                                context = context,
                                callerId = currentUserUid,
                                callerName = myProfile.name.ifBlank { "User" },
                                callerPhoto = myProfile.profilePicUrl,
                                receiverId = friendId,
                                receiverName = friendProfile.name.ifBlank { "Friend" },
                                receiverPhoto = friendProfile.profilePicUrl,
                                isVideo = false
                            )
                        } else {
                            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    tint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                    containerColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.08f) else CyanAccent.copy(alpha = 0.12f),
                    borderColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.25f),
                    size = 35.dp,
                    iconSize = 18.dp
                )

                GlassChatIconButton(
                    icon = Icons.Filled.Videocam,
                    contentDescription = "Video Call",
                    onClick = {
                        val hasAudioPerm = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (hasAudioPerm) {
                            VoiceCallManager.startCall(
                                context = context,
                                callerId = currentUserUid,
                                callerName = myProfile.name.ifBlank { "User" },
                                callerPhoto = myProfile.profilePicUrl,
                                receiverId = friendId,
                                receiverName = friendProfile.name.ifBlank { "Friend" },
                                receiverPhoto = friendProfile.profilePicUrl,
                                isVideo = true
                            )
                        } else {
                            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    tint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                    containerColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.08f) else CyanAccent.copy(alpha = 0.12f),
                    borderColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.25f),
                    size = 35.dp,
                    iconSize = 18.dp
                )

                GlassChatIconButton(
                    icon = Icons.Filled.Info,
                    contentDescription = "Profile Details",
                    onClick = { onViewProfile?.invoke(friendProfile) },
                    tint = descTextColor,
                    containerColor = if (isLightMode) Color.Black.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.06f),
                    borderColor = if (isLightMode) Color(0xFFCBD5E1).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f),
                    size = 35.dp,
                    iconSize = 18.dp
                )
            }
        }
        
        // Chat Message List
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            reverseLayout = true
        ) {
            // Welcome Glass Banner
            if (messages.size <= 2) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .glassCard(cornerSize = 18.dp, glassAlpha = 0.94f),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (friendProfile.profilePicUrl.isNotEmpty()) {
                                BadgeImage(
                                    imageUrl = friendProfile.profilePicUrl,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, CyanAccent.copy(alpha = 0.6f), CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(if (isLightMode) Color(0xFFE2E8F0) else Color(0xFF1E293B))
                                        .border(2.dp, CyanAccent.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Person, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(36.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(friendProfile.name.ifBlank { "Friend" }, color = titleTextColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (isBn) "চ্যাটে সংযুক্ত হয়েছেন" else "Connected in Chat",
                                color = descTextColor,
                                fontSize = 12.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(CyanAccent.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                    .border(1.dp, CyanAccent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (isBn) "ইসলামিক ভ্রাতৃত্ব ও নিরাপদ আলাপন" else "Islamic Brotherhood & Pure Friendship",
                                    color = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            items(messages.size) { index ->
                val msg = messages[messages.size - 1 - index]
                val isMe = msg.senderId == currentUserUid
                val isReactingThis = reactingMessageId == msg.id

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    // Floating Reaction Bar on tap/long press
                    if (isReactingThis) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isLightMode) Color.White.copy(alpha = 0.96f) else Color(0xFF1A1B26).copy(alpha = 0.95f),
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, if (isLightMode) Color(0xFFCBD5E1) else Color.White.copy(alpha = 0.18f)),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("❤️", "👍", "😂", "😮", "😢", "🤲").forEach { emoji ->
                                    Text(
                                        text = emoji,
                                        fontSize = 20.sp,
                                        modifier = Modifier
                                            .clickable {
                                                socialViewModel.reactToMessage(friendId, msg.id, emoji)
                                                reactingMessageId = null
                                            }
                                            .padding(3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Friend Avatar on left of incoming bubble
                        if (!isMe) {
                            if (friendProfile.profilePicUrl.isNotEmpty()) {
                                BadgeImage(
                                    imageUrl = friendProfile.profilePicUrl,
                                    modifier = Modifier.size(28.dp).clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier.size(28.dp).background(if (isLightMode) Color(0xFFE2E8F0) else Color(0xFF334155), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Person, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Message content column
                        Column(
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            if (msg.imageUrl.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .widthIn(max = 240.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .then(
                                            if (isMe) Modifier.background(myBubbleGradient).border(BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f)), RoundedCornerShape(16.dp))
                                            else Modifier.background(friendBubbleBg).border(BorderStroke(0.8.dp, friendBubbleBorder), RoundedCornerShape(16.dp))
                                        )
                                        .clickable { fullScreenImageUrl = msg.imageUrl }
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                                        ChatMediaThumbnail(
                                            imageUrl = msg.imageUrl,
                                            contentDescription = "HD Photo",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.Black.copy(alpha = 0.6f),
                                            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.Hd, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("HD", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    if (msg.text.isNotEmpty()) {
                                        Text(
                                            text = msg.text,
                                            color = if (isMe) Color.White else friendTextColor,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            } else if (msg.videoUrl.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .widthIn(max = 240.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .then(
                                            if (isMe) Modifier.background(myBubbleGradient).border(BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f)), RoundedCornerShape(16.dp))
                                            else Modifier.background(friendBubbleBg).border(BorderStroke(0.8.dp, friendBubbleBorder), RoundedCornerShape(16.dp))
                                        )
                                        .clickable { fullScreenVideoUrl = msg.videoUrl }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(170.dp)
                                            .background(Color(0xFF0A0C14)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                .border(1.5.dp, Color(0xFF00E5FF), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play Video", tint = Color.White, modifier = Modifier.size(32.dp))
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.Black.copy(alpha = 0.65f),
                                            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("HD Video", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                    if (msg.text.isNotEmpty()) {
                                        Text(
                                            text = msg.text,
                                            color = if (isMe) Color.White else friendTextColor,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            if (msg.audioUrl.isNotEmpty()) {
                                val isCurrent = voicePlayerManager.currentlyPlayingMessageId.value == msg.id
                                val isPlaying = isCurrent && voicePlayerManager.isPlaying.value
                                val progress = if (isCurrent) voicePlayerManager.currentPlaybackPosition.value else 0f
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .widthIn(min = 180.dp, max = 260.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .then(
                                            if (isMe) Modifier.background(myBubbleGradient).border(BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f)), RoundedCornerShape(18.dp))
                                            else Modifier.background(friendBubbleBg).border(BorderStroke(0.8.dp, friendBubbleBorder), RoundedCornerShape(18.dp))
                                        )
                                        .padding(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { voicePlayerManager.play(msg.id, msg.audioUrl) {} },
                                        modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = if (isPlaying) "Pause" else "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f))) {
                                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress).background(Color.White))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Filled.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            } else if (msg.text.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 18.dp,
                                                topEnd = 18.dp,
                                                bottomStart = if (isMe) 18.dp else 4.dp,
                                                bottomEnd = if (isMe) 4.dp else 18.dp
                                            )
                                        )
                                        .then(
                                            if (isMe) Modifier.background(myBubbleGradient).border(BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f)), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                                            else Modifier.background(friendBubbleBg).border(BorderStroke(0.8.dp, friendBubbleBorder), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                                        )
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = {
                                                    reactingMessageId = if (isReactingThis) null else msg.id
                                                },
                                                onLongPress = {
                                                    if (isMe) {
                                                        msgToEditOrDelete = msg
                                                    } else {
                                                        reactingMessageId = msg.id
                                                    }
                                                }
                                            )
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = if (isMe) Color.White else friendTextColor,
                                        fontSize = 14.5.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }

                            // Reaction Badge if present
                            if (msg.reaction.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isLightMode) Color.White else Color(0xFF1E202C),
                                    border = BorderStroke(1.dp, if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.15f)),
                                    modifier = Modifier
                                        .offset(y = (-6).dp)
                                        .padding(horizontal = 6.dp)
                                        .clickable { reactingMessageId = msg.id }
                                ) {
                                    Text(
                                        text = msg.reaction,
                                        fontSize = 12.5.sp,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Emojis strip (collapsible)
        if (showQuickEmojis) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isLightMode) Color(0xFFF1F5F9) else Color(0xFF161722))
                    .border(BorderStroke(0.5.dp, if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.08f)))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("😊", "🤲", "🕌", "🤍", "👍", "🌙", "✨", "❤️").forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 22.sp,
                        modifier = Modifier
                            .clickable {
                                messageText += emoji
                            }
                            .padding(4.dp)
                    )
                }
            }
        }

        // Uploading indicator banner
        if (isUploadingMedia) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isLightMode) Color(0xFFE0F2FE) else Color(0xFF0C243C),
                border = BorderStroke(1.dp, if (isLightMode) Color(0xFF38BDF8) else Color(0xFF0284C7))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = if (isLightMode) Color(0xFF0284C7) else CyanAccent
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = uploadProgressText ?: (if (isBn) "মিডিয়া পাঠানো হচ্ছে..." else "Uploading media..."),
                        color = if (isLightMode) Color(0xFF0369A1) else Color(0xFF7DD3FC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Glass Bottom Bar (Sleek pill / flattened bottom dock)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            color = glassBottomBarBg,
            border = BorderStroke(1.dp, androidx.compose.ui.graphics.Brush.verticalGradient(listOf(if (isLightMode) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.12f), Color.Transparent)))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRecording) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(glassInputBg, RoundedCornerShape(24.dp))
                            .border(1.dp, glassInputBorder, RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color.Red, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("0:0${recordingDurationSec} / 0:60", color = titleTextColor, fontSize = 13.sp)
                        }
                        Text(
                            text = if (isBn) "বাতিল" else "Delete",
                            color = Color(0xFFEF4444),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { cancelRecordingLambda() }
                        )
                    }
                } else {
                    // Left action buttons: Image/Video & Mic in a tight, cleanly spaced row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassChatIconButton(
                            icon = Icons.Filled.AddPhotoAlternate,
                            contentDescription = "Send Photo/Video",
                            onClick = { showMediaOptionsDialog = true },
                            tint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                            containerColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.08f) else CyanAccent.copy(alpha = 0.12f),
                            borderColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.25f),
                            size = 38.dp,
                            iconSize = 19.dp
                        )

                        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        GlassChatIconButton(
                            icon = Icons.Filled.Mic,
                            contentDescription = "Voice note",
                            onClick = {
                                if (hasPermission) startRecordingLambda()
                                else permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            },
                            tint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                            containerColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.08f) else CyanAccent.copy(alpha = 0.12f),
                            borderColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.25f),
                            size = 38.dp,
                            iconSize = 19.dp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Glass Pill Input Field with inside emoji button
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(if (isBn) "মেসেজ লিখুন..." else "Message...", color = descTextColor, fontSize = 13.5.sp) },
                        trailingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .clickable { showQuickEmojis = !showQuickEmojis },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Mood,
                                    contentDescription = "Emojis",
                                    tint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                            unfocusedBorderColor = glassInputBorder,
                            focusedTextColor = titleTextColor,
                            unfocusedTextColor = titleTextColor,
                            focusedContainerColor = glassInputBg,
                            unfocusedContainerColor = glassInputBg
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Action button: Send or Like (👍)
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF00B4D8), Color(0xFF0284C7))), CircleShape)
                            .clickable(onClick = stopAndSendRecordingLambda),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send Audio", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                } else if (messageText.isBlank()) {
                    GlassChatIconButton(
                        icon = Icons.Filled.ThumbUp,
                        contentDescription = "Like",
                        onClick = {
                            socialViewModel.sendMessage(friendId, "👍")
                        },
                        tint = if (isLightMode) Color(0xFF0284C7) else CyanAccent,
                        containerColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.08f) else CyanAccent.copy(alpha = 0.12f),
                        borderColor = if (isLightMode) Color(0xFF0284C7).copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.25f),
                        size = 38.dp,
                        iconSize = 19.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF00B4D8), Color(0xFF0284C7))), CircleShape)
                            .clickable {
                                if (messageText.isNotBlank()) {
                                    val toSend = messageText.trim()
                                    messageText = ""
                                    socialViewModel.sendMessage(friendId, toSend)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    // Media Options Dialog
    if (showMediaOptionsDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showMediaOptionsDialog = false },
            containerColor = if (isLightMode) Color.White else Color(0xFF1E202C),
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = if (isBn) "মিডিয়া শেয়ার করুন" else "Share Media",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // HD Photo
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isLightMode) Color(0xFFF1F5F9) else Color(0xFF282A3A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMediaOptionsDialog = false
                                chatImagePicker.launch("image/*")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF0284C7).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Image, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isBn) "HD ছবি পাঠান" else "Send HD Photo",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = titleTextColor
                                )
                                Text(
                                    text = if (isBn) "গ্যালারি থেকে হাই কোয়ালিটি ছবি" else "High quality photo from gallery",
                                    fontSize = 11.5.sp,
                                    color = descTextColor
                                )
                            }
                        }
                    }

                    // Live Camera Photo
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isLightMode) Color(0xFFF1F5F9) else Color(0xFF282A3A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMediaOptionsDialog = false
                                val hasCam = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (hasCam) {
                                    chatCameraPicker.launch(null)
                                } else {
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isBn) "সরাসরি ক্যামেরা ছবি" else "Live Camera",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = titleTextColor
                                )
                                Text(
                                    text = if (isBn) "এখনই ছবি তুলে পাঠান" else "Take photo right now",
                                    fontSize = 11.5.sp,
                                    color = descTextColor
                                )
                            }
                        }
                    }

                    // HD Video
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isLightMode) Color(0xFFF1F5F9) else Color(0xFF282A3A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMediaOptionsDialog = false
                                chatVideoPicker.launch("video/*")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Videocam, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isBn) "HD ভিডিও পাঠান (সর্বোচ্চ ২ মিনিট)" else "Send HD Video (Max 2 min)",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = titleTextColor
                                )
                                Text(
                                    text = if (isBn) "গ্যালারি থেকে হাই কোয়ালিটি ভিডিও ক্লিপ" else "High quality video clip from gallery",
                                    fontSize = 11.5.sp,
                                    color = descTextColor
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMediaOptionsDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = descTextColor)
                }
            }
        )
    }

    // Fullscreen Image Viewer Dialog
    if (fullScreenImageUrl != null) {
        Dialog(
            onDismissRequest = { fullScreenImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.96f))
            ) {
                ChatMediaThumbnail(
                    imageUrl = fullScreenImageUrl!!,
                    contentDescription = "Full Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )

                IconButton(
                    onClick = { fullScreenImageUrl = null },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }

    // Fullscreen Video Player Dialog
    if (fullScreenVideoUrl != null) {
        Dialog(
            onDismissRequest = { fullScreenVideoUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            val controller = MediaController(ctx)
                            controller.setAnchorView(this)
                            setMediaController(controller)
                            setVideoURI(android.net.Uri.parse(fullScreenVideoUrl))
                            setOnPreparedListener { mp ->
                                mp.isLooping = false
                                start()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                )

                IconButton(
                    onClick = { fullScreenVideoUrl = null },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }

    if (msgToEditOrDelete != null && !showEditDialog && !showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { msgToEditOrDelete = null },
            title = { Text(if (isBn) "মেসেজ অপশন" else "Message Options") },
            text = {
                Column {
                    Text(
                        text = if (isBn) "মেসেজ এডিট করুন" else "Edit Message",
                        color = titleTextColor,
                        modifier = Modifier.fillMaxWidth().clickable {
                            editText = msgToEditOrDelete!!.text
                            showEditDialog = true
                        }.padding(16.dp)
                    )
                    Text(
                        text = if (isBn) "মেসেজ ডিলিট করুন" else "Delete Message",
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth().clickable {
                            showDeleteDialog = true
                        }.padding(16.dp)
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { msgToEditOrDelete = null }) { Text(if (isBn) "বাতিল" else "Cancel") }
            }
        )
    }

    if (showEditDialog && msgToEditOrDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditDialog = false; msgToEditOrDelete = null },
            title = { Text(if (isBn) "মেসেজ এডিট" else "Edit Message") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    socialViewModel.updateMessage(friendId, msgToEditOrDelete!!.id, editText)
                    showEditDialog = false
                    msgToEditOrDelete = null
                }) { Text(if (isBn) "সংরক্ষণ" else "Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false; msgToEditOrDelete = null }) { Text(if (isBn) "বাতিল" else "Cancel") }
            }
        )
    }

    if (showDeleteDialog && msgToEditOrDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false; msgToEditOrDelete = null },
            title = { Text(if (isBn) "মেসেজ ডিলিট" else "Delete Message") },
            text = { Text(if (isBn) "আপনি কি নিশ্চিত যে এই মেসেজটি ডিলিট করতে চান?" else "Are you sure you want to delete this message?") },
            confirmButton = {
                TextButton(onClick = {
                    socialViewModel.deleteMessage(friendId, msgToEditOrDelete!!.id)
                    showDeleteDialog = false
                    msgToEditOrDelete = null
                }) { Text(if (isBn) "ডিলিট" else "Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; msgToEditOrDelete = null }) { Text(if (isBn) "বাতিল" else "Cancel") }
            }
        )
    }
}

@Composable
fun UserDetailScreen(
    userProfile: UserProfile,
    currentUserId: String,
    socialViewModel: SocialViewModel,
    communityViewModel: CommunityViewModel,
    onBack: () -> Unit,
    onOpenChat: (UserProfile) -> Unit
) {
    val posts by communityViewModel.posts.collectAsState()
    val userPosts = posts.filter { it.userId == userProfile.userId }
    val friendReqs by socialViewModel.friendRequests.collectAsState()
    val friends by socialViewModel.friends.collectAsState()

    var relationshipStatus by remember { mutableStateOf("none") } // "none", "sent", "received", "accepted"
    var liveUserProfile by remember { mutableStateOf(userProfile) }
    val context = LocalContext.current

    LaunchedEffect(userProfile.userId) {
        if (userProfile.userId.isEmpty()) return@LaunchedEffect
        val listener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(userProfile.userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    liveUserProfile = snapshot.toObject(UserProfile::class.java)?.copy(userId = snapshot.id) ?: liveUserProfile
                }
            }
        kotlinx.coroutines.awaitCancellation()
    }

    LaunchedEffect(userProfile.userId, currentUserId) {
        if (currentUserId.isEmpty() || userProfile.userId.isEmpty()) return@LaunchedEffect
        
        // Listen to friend list of current user to see if they are already friends
        val userDocListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val friendsList = snapshot.get("friends") as? List<String> ?: emptyList()
                    if (friendsList.contains(userProfile.userId)) {
                        relationshipStatus = "accepted"
                    } else {
                        // Check friend requests
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("friend_requests").document("${currentUserId}_${userProfile.userId}")
                            .addSnapshotListener { req1, _ ->
                                if (req1 != null && req1.exists() && req1.getString("status") == "pending") {
                                    relationshipStatus = "sent"
                                } else {
                                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                        .collection("friend_requests").document("${userProfile.userId}_${currentUserId}")
                                        .addSnapshotListener { req2, _ ->
                                            if (req2 != null && req2.exists() && req2.getString("status") == "pending") {
                                                relationshipStatus = "received"
                                            } else if (relationshipStatus != "accepted" && relationshipStatus != "sent") {
                                                relationshipStatus = "none"
                                            }
                                        }
                                }
                            }
                    }
                }
            }
        kotlinx.coroutines.awaitCancellation()
    }

    val isLightMode = com.example.ui.theme.GlassTheme.isLightMode.value
    val titleTextColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val descTextColor = if (isLightMode) Color(0xFF475569) else Color.LightGray
    val subCardBg = if (isLightMode) Color(0xFFF1F5F9) else Color.Black.copy(alpha = 0.2f)
    val cardBg = if (isLightMode) Color.White.copy(alpha = 0.9f) else CardBlue
    val screenBg = if (isLightMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0B1120)).clickable { /* Consume clicks */ }) {
        // Facebook Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0xFF1E293B).copy(alpha = 0.95f))
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(userProfile.name.ifBlank { "Profile" }, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .glassCard(cornerSize = 24.dp, glassAlpha = 0.95f, hasSheen = true),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Facebook Cover Photo Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        ) {
                            if (userProfile.coverPicUrl.isNotEmpty()) {
                                coil.compose.AsyncImage(
                                    model = userProfile.coverPicUrl,
                                    contentDescription = "Cover Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF0F172A),
                                                    Color(0xFF1E293B),
                                                    Color(0xFF0284C7)
                                                )
                                            )
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Mosque,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.08f),
                                        modifier = Modifier
                                            .size(130.dp)
                                            .align(Alignment.CenterEnd)
                                            .offset(x = 20.dp)
                                    )
                                }
                            }
                        }

                        // Facebook Avatar overlapping cover photo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier.offset(y = (-45).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF0F172A),
                                    shadowElevation = 8.dp,
                                    border = BorderStroke(3.5.dp, Color(0xFF0F172A)),
                                    modifier = Modifier.size(92.dp)
                                ) {
                                    if (userProfile.profilePicUrl.isNotEmpty()) {
                                        BadgeImage(
                                            imageUrl = userProfile.profilePicUrl,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0xFF242526), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.Person,
                                                contentDescription = null,
                                                tint = Color.LightGray,
                                                modifier = Modifier.size(52.dp)
                                            )
                                        }
                                    }
                                }

                                val isBn = LanguageStore.currentLanguage.value == "bn"
                                val profilePresence = UserPresenceManager.formatPresenceStatus(
                                    isOnline = liveUserProfile.isOnline,
                                    lastSeen = liveUserProfile.lastSeen,
                                    isBn = isBn
                                )

                                // Green Active Indicator (Facebook style)
                                if (profilePresence.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(end = 4.dp, bottom = 4.dp)
                                            .size(16.dp)
                                            .background(Color(0xFF0F172A), CircleShape)
                                            .padding(2.5.dp)
                                            .background(Color(0xFF31A24C), CircleShape)
                                    )
                                }
                            }
                        }

                        // Profile Info & Name
                        val isBn = LanguageStore.currentLanguage.value == "bn"
                        val profilePresence = UserPresenceManager.formatPresenceStatus(
                            isOnline = liveUserProfile.isOnline,
                            lastSeen = liveUserProfile.lastSeen,
                            isBn = isBn
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-30).dp)
                                .padding(horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(liveUserProfile.name.ifBlank { "User" }, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                if (liveUserProfile.isPremium || liveUserProfile.email == "muhaiminulhasananas@gmail.com") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    ProBlueBadge(size = 18.dp)
                                }
                                if (liveUserProfile.email == "muhaiminulhasananas@gmail.com") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Facebook style online / last active badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (profilePresence.isOnline) Color(0xFF31A24C).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f))
                                    .border(0.8.dp, if (profilePresence.isOnline) Color(0xFF31A24C).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                if (profilePresence.isOnline) {
                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF31A24C), CircleShape))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(profilePresence.statusText, color = Color(0xFF31A24C), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                } else {
                                    Text(profilePresence.statusText, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Normal)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                                    .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("User ID", liveUserProfile.username.ifBlank { liveUserProfile.userId })
                                        clipboard?.setPrimaryClip(clip)
                                        Toast.makeText(context, "ইউজার আইডি কপি হয়েছে! 📋", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("@${liveUserProfile.username.ifBlank { liveUserProfile.userId.take(8) }}", color = CyanAccent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy ID", tint = CyanAccent, modifier = Modifier.size(13.dp))
                            }
                            
                            if (liveUserProfile.bio.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    liveUserProfile.bio,
                                    color = Color.LightGray,
                                    fontSize = 13.5.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Facebook Profile Action Buttons (Message + Add Friend/Unfriend)
                            if (currentUserId != userProfile.userId) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Primary Message Button (Facebook Messenger Blue)
                                    Button(
                                        onClick = { onOpenChat(userProfile) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0084FF)),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Filled.ChatBubble, contentDescription = "Message", tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Message", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    // Friend Action Button
                                    when (relationshipStatus) {
                                        "accepted" -> {
                                            Button(
                                                onClick = {
                                                    socialViewModel.unfriend(userProfile.userId)
                                                    android.widget.Toast.makeText(context, "Unfriended ${userProfile.name}", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(38.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3B3C)),
                                                contentPadding = PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Friends", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                        "sent" -> {
                                            Button(
                                                onClick = {
                                                    socialViewModel.cancelFriendRequest(userProfile.userId)
                                                    android.widget.Toast.makeText(context, "Friend request cancelled", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(38.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3B3C)),
                                                contentPadding = PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Cancel", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                        "received" -> {
                                            Button(
                                                onClick = {
                                                    socialViewModel.acceptFriendRequest(userProfile.userId)
                                                    android.widget.Toast.makeText(context, "Friend request accepted!", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(38.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                contentPadding = PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Accept", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                        else -> {
                                            Button(
                                                onClick = {
                                                    socialViewModel.sendFriendRequest(userProfile.userId)
                                                    android.widget.Toast.makeText(context, "Friend request sent!", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(38.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                                contentPadding = PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Add Friend", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }

                                    // More button
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF3A3B3C),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.MoreHoriz, contentDescription = "More", tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Facebook Details Section Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .offset(y = (-15).dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(subCardBg)
                                .padding(16.dp)
                        ) {
                            Text("Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(bottom = 8.dp))
                            
                            if (userProfile.birthDate.isNotEmpty()) {
                                ProfileInfoRow(icon = Icons.Filled.Cake, label = "Birth Date", value = userProfile.birthDate)
                            }
                            if (userProfile.relationship.isNotEmpty()) {
                                ProfileInfoRow(icon = Icons.Filled.Favorite, label = "Relationship Status", value = userProfile.relationship)
                            }
                            if (userProfile.location.isNotEmpty()) {
                                ProfileInfoRow(icon = Icons.Filled.Place, label = "Lives in", value = userProfile.location)
                            }
                            if (userProfile.work.isNotEmpty()) {
                                ProfileInfoRow(icon = Icons.Filled.Work, label = "Works as", value = userProfile.work)
                            }
                            if (userProfile.education.isNotEmpty()) {
                                ProfileInfoRow(icon = Icons.Filled.School, label = "Studied at", value = userProfile.education)
                            }
                            if (userProfile.website.isNotEmpty()) {
                                ProfileInfoRow(icon = Icons.Filled.Link, label = "Website / Social Link", value = userProfile.website)
                            }
                            if (userProfile.badge.isNotEmpty()) {
                                ProfileInfoRow(icon = Icons.Filled.Verified, label = "Purity Title", value = userProfile.badge)
                            }
                            
                            if (userProfile.birthDate.isEmpty() && userProfile.relationship.isEmpty() && 
                                userProfile.location.isEmpty() && userProfile.work.isEmpty() && 
                                userProfile.education.isEmpty() && userProfile.website.isEmpty()) {
                                Text(
                                    "No extra details added yet.",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (userPosts.isEmpty()) {
                    Text("No posts from this user.", color = descTextColor, modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center)
                } else {
                    Text("User Posts", color = titleTextColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            if (userPosts.isNotEmpty()) {
                items(userPosts.size) { index ->
                    val post = userPosts[index]
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        CommunityPost(
                            post = post,
                            currentUserId = currentUserId,
                            communityViewModel = communityViewModel
                        )
                    }
                }
            }
        }
    }
}
