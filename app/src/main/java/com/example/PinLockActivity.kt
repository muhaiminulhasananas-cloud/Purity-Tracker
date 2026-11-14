package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class PinLockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LockManager.markPinScreenActive(true)

        onBackPressedDispatcher.addCallback(
            this,
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Never auto-back out of the PIN screen; keep it open until the user enters the PIN or taps Cancel.
                }
            }
        )

        setContent {
            var pinInput by remember { mutableStateOf("") }
            var isError by remember { mutableStateOf(false) }
            var customErrorMessage by remember { mutableStateOf<String?>(null) }
            var isSendingPartnerReset by remember { mutableStateOf(false) }
            var statusMessage by remember { mutableStateOf<String?>(null) }

            val isBn = LanguageStore.currentLanguage.value == "bn"
            val darkBg = Color(0xFF0F172A)
            val cardBg = Color(0xFF1E293B)
            val cyanAccent = Color(0xFF00FFCC)

            LaunchedEffect(Unit) {
                BlockerManager.ensurePinResetApprovalListener(this@PinLockActivity) { status ->
                    if (status == "approved") {
                        BlockerManager.consumePartnerPinResetNotice(this@PinLockActivity)
                        statusMessage = "Your PIN was reset by your partner"
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxSize(), color = darkBg) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(cyanAccent.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = cyanAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (isBn) "নিরাপত্তা পিন যাচাইকরণ" else "Security Verification",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isBn)
                                    "সেটিংস বা প্রটেকশন আনলক করতে আপনার পার্টনারের সেট করা পিন কোডটি প্রবেশ করান।"
                                else
                                    "Enter the security PIN set by your accountability partner to unlock settings for 60 seconds.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { input ->
                                    if (input.length <= 6 && input.all { it.isDigit() }) {
                                        pinInput = input
                                        if (isError) isError = false
                                        customErrorMessage = null
                                    }
                                },
                                label = {
                                    Text(
                                        text = if (isBn) "পিন কোড লিখুন" else "Enter PIN",
                                        color = if (isError) Color(0xFFEF4444) else Color(0xFF94A3B8)
                                    )
                                },
                                isError = isError,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isError) Color(0xFFEF4444) else cyanAccent,
                                    unfocusedBorderColor = if (isError) Color(0xFFEF4444) else Color(0xFF475569),
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = cyanAccent
                                ),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (isError) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = customErrorMessage ?: if (isBn) "ভুল পিন কোড! সঠিক পিন লিখুন।" else "Incorrect PIN! Please try again.",
                                    color = Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (!statusMessage.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = statusMessage.orEmpty(),
                                    color = Color(0xFF34D399),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (LockManager.verifyPin(this@PinLockActivity, pinInput)) {
                                        LockManager.unlockForSeconds(this@PinLockActivity, 60)
                                        LockManager.markPinScreenActive(false)
                                        Toast.makeText(
                                            this@PinLockActivity,
                                            if (isBn) "সঠিক পিন! ১ মিনিটের জন্য আনলক করা হয়েছে" else "Correct PIN! Unlocked for 1 minute",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        finish()
                                    } else {
                                        isError = true
                                        customErrorMessage = if (isBn) "ভুল পিন কোড! সঠিক পিন লিখুন।" else "Incorrect PIN! Please try again."
                                        pinInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = cyanAccent,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = if (isBn) "আনলক করুন" else "Unlock Settings",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                enabled = !isSendingPartnerReset,
                                onClick = {
                                    if (!BlockerManager.hasValidPartnerEmail(this@PinLockActivity)) {
                                        Toast.makeText(
                                            this@PinLockActivity,
                                            "Save an accountability partner email on the Blocker screen first.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        isSendingPartnerReset = true
                                        BlockerManager.sendPinResetRequestToPartner(this@PinLockActivity) { ok, msg ->
                                            isSendingPartnerReset = false
                                            if (ok) {
                                                statusMessage = msg
                                            } else {
                                                Toast.makeText(this@PinLockActivity, msg, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    text = if (isSendingPartnerReset) "Sending..." else "Forgot PIN?",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            TextButton(
                                onClick = {
                                    LockManager.markPinScreenActive(false)
                                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                        addCategory(Intent.CATEGORY_HOME)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    startActivity(homeIntent)
                                    finish()
                                }
                            ) {
                                Text(
                                    text = if (isBn) "বাতিল করুন (হোম স্ক্রিন)" else "Cancel (Go Home)",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LockManager.markPinScreenActive(true)
    }

    override fun onResume() {
        super.onResume()
        LockManager.markPinScreenActive(true)
    }

    override fun onDestroy() {
        if (isFinishing) {
            LockManager.markPinScreenActive(false)
        }
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Ignore system/accessibility back events so the PIN screen stays open until PIN entry or explicit Cancel.
    }
}
