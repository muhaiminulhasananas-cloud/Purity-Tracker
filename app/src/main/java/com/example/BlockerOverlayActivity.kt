package com.example

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Displays the immediate BlockerHero-style block screen when `ProtectionService`
 * detects an adult URL or keyword in Chrome or any browser.
 *
 * Uses `SYSTEM_ALERT_WINDOW` (`TYPE_APPLICATION_OVERLAY`) when granted for instant
 * zero-delay overlay rendering, plus `BlockerOverlayActivity` so the block screen
 * always appears even if overlay permission has not been granted yet.
 */
object BlockerOverlayManager {

    private var overlayView: View? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun showBlockScreen(context: Context, matchedTerm: String) {
        val appCtx = context.applicationContext
        mainHandler.post {
            var shownOverlayWindow = false
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(appCtx)) {
                shownOverlayWindow = showSystemAlertOverlay(appCtx, matchedTerm)
            }
            if (!shownOverlayWindow) {
                launchBlockActivity(appCtx, matchedTerm)
            }
        }
    }

    private fun showSystemAlertOverlay(context: Context, matchedTerm: String): Boolean {
        return try {
            dismissOverlay(context)
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return false

            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            val dp = { value: Int ->
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    value.toFloat(),
                    context.resources.displayMetrics
                ).toInt()
            }

            val rootLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setBackgroundColor(AndroidColor.parseColor("#F2060B16"))
                setPadding(dp(28), dp(36), dp(28), dp(36))
            }

            val cardLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(24).toFloat()
                    setColor(AndroidColor.parseColor("#111827"))
                    setStroke(dp(2), AndroidColor.parseColor("#EF4444"))
                }
                setPadding(dp(24), dp(28), dp(24), dp(28))
            }

            val badgeView = TextView(context).apply {
                text = "🛡️"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 44f)
                gravity = Gravity.CENTER
            }

            val titleView = TextView(context).apply {
                text = "Adult Content Blocked"
                setTextColor(AndroidColor.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(0, dp(12), 0, dp(8))
            }

            val subtitleView = TextView(context).apply {
                text = "Purity Tracker Blocker is active.\nStay strong on your commitment — your Accountability Partner is standing with you."
                setTextColor(AndroidColor.parseColor("#CBD5E1"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(16))
            }

            val triggerView = TextView(context).apply {
                text = "Blocked trigger: $matchedTerm"
                setTextColor(AndroidColor.parseColor("#FCA5A5"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(8), dp(12), dp(20))
            }

            val closeBtn = Button(context).apply {
                text = "Go Back Safely"
                isAllCaps = false
                setTextColor(AndroidColor.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                typeface = Typeface.DEFAULT_BOLD
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(14).toFloat()
                    setColor(AndroidColor.parseColor("#EF4444"))
                }
                setPadding(dp(24), dp(12), dp(24), dp(12))
                setOnClickListener {
                    dismissOverlay(context)
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(homeIntent)
                    } catch (e: Exception) {}
                }
            }

            cardLayout.addView(badgeView)
            cardLayout.addView(titleView)
            cardLayout.addView(subtitleView)
            cardLayout.addView(triggerView)
            cardLayout.addView(closeBtn)

            rootLayout.addView(
                cardLayout,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            wm.addView(rootLayout, params)
            overlayView = rootLayout
            true
        } catch (e: Exception) {
            false
        }
    }

    fun dismissOverlay(context: Context) {
        try {
            val view = overlayView ?: return
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            wm?.removeViewImmediate(view)
        } catch (e: Exception) {
        } finally {
            overlayView = null
        }
    }

    private fun launchBlockActivity(context: Context, matchedTerm: String) {
        try {
            val intent = Intent(context, BlockerOverlayActivity::class.java).apply {
                putExtra("matched_term", matchedTerm)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
            }
            context.startActivity(intent)
        } catch (e: Exception) {}
    }
}

class BlockerOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val matchedTerm = intent?.getStringExtra("matched_term").orEmpty().ifBlank { "adult content" }

        setContent {
            BackHandler {
                goHomeAndFinish()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF060B16), Color(0xFF111827), Color(0xFF1E1B4B))
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.96f)),
                    border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(Color(0xFFEF4444).copy(alpha = 0.16f), CircleShape)
                                .border(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = "Blocked",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Adult Content Blocked",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Purity Tracker Blocker protected your eyes and streak. Remember why you started.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "Blocked: $matchedTerm",
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { goHomeAndFinish() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Go Back Safely",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    private fun goHomeAndFinish() {
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
        } catch (e: Exception) {}
        finish()
    }
}
