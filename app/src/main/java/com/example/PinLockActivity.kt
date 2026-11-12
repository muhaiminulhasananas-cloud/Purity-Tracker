package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class PinLockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var isPinConfigured by remember { mutableStateOf(LockManager.hasPin(this@PinLockActivity)) }
            var pinInput by remember { mutableStateOf("") }
            var confirmPinInput by remember { mutableStateOf("") }
            var isPasswordVisible by remember { mutableStateOf(false) }
            var isError by remember { mutableStateOf(false) }
            var customErrorMessage by remember { mutableStateOf<String?>(null) }

            // Guardian Reset Flow States
            var showResetDialog by remember { mutableStateOf(false) }
            var guardianKeyInput by remember { mutableStateOf("") }
            var newPinInput by remember { mutableStateOf("") }
            var resetConfirmPinInput by remember { mutableStateOf("") }
            var resetErrorMessage by remember { mutableStateOf<String?>(null) }
            var newlyGeneratedKey by remember { mutableStateOf<String?>(null) }
            var showSuccessKeyDialog by remember { mutableStateOf(false) }

            val isBn = LanguageStore.currentLanguage.value == "bn"
            val darkBg = Color(0xFF0F172A)
            val cardBg = Color(0xFF1E293B)
            val cyanAccent = Color(0xFF00FFCC)

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
                                text = if (isPinConfigured) {
                                    if (isBn) "নিরাপত্তা পিন যাচাইকরণ" else "Security Verification"
                                } else {
                                    if (isBn) "নিরাপত্তা পিন সেট করুন" else "Set Security PIN"
                                },
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isPinConfigured) {
                                    if (isBn) 
                                        "সেটিংস বা প্রটেকশন আনলক করতে আপনার স্থায়ী পিন কোডটি প্রবেশ করান।" 
                                    else 
                                        "Enter your permanent security PIN to unlock settings or protection."
                                } else {
                                    if (isBn)
                                        "সেটিংস ও অ্যাপ আনইনস্টল সুরক্ষিত করতে ৪-সংখ্যার একটি স্থায়ী পিন সেট করুন।"
                                    else
                                        "Set a 4-digit security PIN to protect settings and recovery streak."
                                },
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
                                    if (input.length <= 8) {
                                        pinInput = input
                                        if (isError) isError = false
                                        customErrorMessage = null
                                    }
                                },
                                label = {
                                    Text(
                                        text = if (isPinConfigured) {
                                            if (isBn) "পিন কোড লিখুন" else "Enter PIN"
                                        } else {
                                            if (isBn) "নতুন ৪-সংখ্যার পিন" else "New 4-digit PIN"
                                        },
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
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = "Toggle PIN visibility",
                                            tint = Color(0xFF94A3B8)
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (!isPinConfigured) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = confirmPinInput,
                                    onValueChange = { input ->
                                        if (input.length <= 8) {
                                            confirmPinInput = input
                                            if (isError) isError = false
                                            customErrorMessage = null
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = if (isBn) "পিন কোড নিশ্চিত করুন" else "Confirm PIN",
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
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (isError) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = customErrorMessage ?: if (isBn) "ভুল পিন কোড! সঠিক পিন লিখুন।" else "Incorrect PIN! Please try again.",
                                    color = Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (!isPinConfigured) {
                                        val cleanPin = LockManager.normalizeDigits(pinInput)
                                        val cleanConfirm = LockManager.normalizeDigits(confirmPinInput)
                                        if (cleanPin.length < 4) {
                                            isError = true
                                            customErrorMessage = if (isBn) "কমপক্ষে ৪ সংখ্যার পিন লিখুন!" else "PIN must be at least 4 digits!"
                                        } else if (cleanPin != cleanConfirm) {
                                            isError = true
                                            customErrorMessage = if (isBn) "দুটো পিন কোড মেলেনি!" else "PINs do not match!"
                                        } else {
                                            val (ok, key) = LockManager.setPin(this@PinLockActivity, cleanPin)
                                            if (ok) {
                                                LockManager.unlockForSeconds(this@PinLockActivity, 60)
                                                Toast.makeText(
                                                    this@PinLockActivity,
                                                    if (isBn) "পিন সেট হয়েছে ও ১ মিনিটের জন্য আনলক করা হয়েছে" else "PIN set & unlocked for 1 minute",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                finish()
                                            }
                                        }
                                    } else {
                                        if (LockManager.verifyPin(this@PinLockActivity, pinInput)) {
                                            LockManager.unlockForSeconds(this@PinLockActivity, 60)
                                            Toast.makeText(
                                                this@PinLockActivity,
                                                if (isBn) "সঠিক পিন! ১ মিনিটের জন্য আনলক করা হয়েছে" else "Correct PIN! Unlocked for 1 minute",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            finish()
                                        } else {
                                            isError = true
                                            customErrorMessage = if (isBn) "ভুল পিন কোড!" else "Incorrect PIN"
                                            Toast.makeText(
                                                this@PinLockActivity,
                                                if (isBn) "ভুল পিন কোড!" else "Incorrect PIN",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            pinInput = ""
                                        }
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
                                    text = if (!isPinConfigured) {
                                        if (isBn) "পিন সেট ও আনলক করুন" else "Set PIN & Unlock"
                                    } else {
                                        if (isBn) "আনলক করুন" else "Unlock Settings"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (isPinConfigured) {
                                // Guardian Key Reset Button
                                TextButton(
                                    onClick = {
                                        guardianKeyInput = ""
                                        newPinInput = ""
                                        resetConfirmPinInput = ""
                                        resetErrorMessage = null
                                        showResetDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Key,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBn) "পিন ভুলে গেছেন? অভিভাবকের কি (Key) দিন" else "Forgot PIN? Use Guardian Key",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            TextButton(
                                onClick = {
                                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                        addCategory(Intent.CATEGORY_HOME)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    startActivity(homeIntent)
                                    finish()
                                }
                            ) {
                                Text(
                                    text = if (isBn) "ফিরে যান (হোম স্ক্রিন)" else "Go Back (Home)",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Guardian Reset Dialog
                if (showResetDialog) {
                    val remainingLockout = remember { LockManager.getResetLockoutRemainingMinutes(this@PinLockActivity) }
                    
                    AlertDialog(
                        onDismissRequest = { showResetDialog = false },
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color(0xFF1E293B),
                        tonalElevation = 8.dp,
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.15f), CircleShape)
                                    .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = R.mipmap.ic_launcher),
                                    contentDescription = "Purity Tracker Logo",
                                    modifier = Modifier.size(42.dp).clip(CircleShape)
                                )
                            }
                        },
                        title = {
                            Text(
                                text = if (isBn) "অভিভাবক দিয়ে পিন রিসেট" else "Guardian PIN Reset",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isBn)
                                            "🛡️ আপনার অভিভাবক বা বিশ্বস্ত বন্ধুর কাছে থাকা ৮-অক্ষরের গার্ডিয়ান কি প্রবেশ করান। আপনি নিজে একা এই কি ছাড়া পিন পরিবর্তন করতে পারবেন না।"
                                        else
                                            "🛡️ Enter the 8-character Guardian Key kept with your trusted partner. You cannot reset your PIN without their code.",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = guardianKeyInput,
                                    onValueChange = { 
                                        guardianKeyInput = it
                                        resetErrorMessage = null
                                    },
                                    label = { Text(if (isBn) "গার্ডিয়ান কি (যেমন: G-4982-XP)" else "Guardian Key (e.g. G-4982-XP)", color = Color(0xFF94A3B8)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A),
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF475569)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = newPinInput,
                                    onValueChange = { 
                                        if (it.length <= 8) newPinInput = it
                                        resetErrorMessage = null
                                    },
                                    label = { Text(if (isBn) "নতুন ৪-সংখ্যার পিন" else "New 4-digit PIN", color = Color(0xFF94A3B8)) },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                                    ),
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A),
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF475569)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = confirmPinInput,
                                    onValueChange = { 
                                        if (it.length <= 8) confirmPinInput = it
                                        resetErrorMessage = null
                                    },
                                    label = { Text(if (isBn) "নতুন পিন নিশ্চিত করুন" else "Confirm New PIN", color = Color(0xFF94A3B8)) },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                                    ),
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A),
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF475569)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (resetErrorMessage != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = resetErrorMessage ?: "",
                                        color = Color(0xFFEF4444),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else if (remainingLockout > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isBn) "⚠️ অতিরিক্ত ভুল চেষ্টার কারণে $remainingLockout মিনিটের জন্য রিসেট বন্ধ রাখা হয়েছে।" else "⚠️ Locked out for $remainingLockout minutes due to too many failed attempts.",
                                        color = Color(0xFFEF4444),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                var emailSentNotice by remember { mutableStateOf(false) }
                                if (emailSentNotice) {
                                    Text(
                                        text = if (isBn) "✅ অভিভাবক ইমেইলে রিকভারি নির্দেশিকা পাঠানো হয়েছে।" else "✅ Recovery instructions sent to guardian email.",
                                        color = Color(0xFF10B981),
                                        fontSize = 11.5.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    TextButton(
                                        onClick = {
                                            val sharedPref = this@PinLockActivity.getSharedPreferences("GuardianPrefs", android.content.Context.MODE_PRIVATE)
                                            val gMail = sharedPref.getString("guardianEmail", "") ?: ""
                                            val currentAuthMail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: ""
                                            val targetMail = if (gMail.isNotBlank()) gMail else currentAuthMail
                                            if (targetMail.isNotBlank()) {
                                                com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(targetMail)
                                                emailSentNotice = true
                                                Toast.makeText(this@PinLockActivity, if (isBn) "$targetMail ঠিকানায় রিকভারি পাঠানো হয়েছে" else "Recovery sent to $targetMail", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(this@PinLockActivity, if (isBn) "কোনো অভিভাবক ইমেইল সংরক্ষিত নেই, কি (Key) দিন" else "No guardian email saved, enter Key", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Filled.Email, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isBn) "ইমেইলে অভিভাবক রিকভারি কোড পাঠান" else "Send Recovery Code to Email",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val cleanNewPin = LockManager.normalizeDigits(newPinInput)
                                    val cleanConfirm = LockManager.normalizeDigits(confirmPinInput)

                                    if (cleanNewPin.length < 4) {
                                        resetErrorMessage = if (isBn) "পিন কমপক্ষে ৪ সংখ্যার হতে হবে।" else "PIN must be at least 4 digits."
                                        return@Button
                                    }
                                    if (cleanNewPin != cleanConfirm) {
                                        resetErrorMessage = if (isBn) "নতুন পিন দুটি মেলেনি।" else "New PINs do not match."
                                        return@Button
                                    }

                                    val result = LockManager.resetPinWithGuardianKey(
                                        this@PinLockActivity,
                                        guardianKeyInput,
                                        cleanNewPin
                                    )

                                    when (result) {
                                        is ResetResult.Success -> {
                                            newlyGeneratedKey = result.newGuardianKey
                                            showResetDialog = false
                                            showSuccessKeyDialog = true
                                        }
                                        is ResetResult.InvalidKey -> {
                                            resetErrorMessage = if (isBn) 
                                                "ভুল গার্ডিয়ান কি! আর মাত্র ${result.attemptsRemaining} বার চেষ্টা করা যাবে।" 
                                            else 
                                                "Incorrect Guardian Key! ${result.attemptsRemaining} attempts left."
                                        }
                                        is ResetResult.LockedOut -> {
                                            resetErrorMessage = if (isBn) 
                                                "অতিরিক্ত ভুল চেষ্টার কারণে ${result.remainingMinutes} মিনিটের জন্য রিসেট বন্ধ!" 
                                            else 
                                                "Locked out for ${result.remainingMinutes} minutes due to failed attempts!"
                                        }
                                        is ResetResult.InvalidNewPin -> {
                                            resetErrorMessage = if (isBn) "পিন কমপক্ষে ৪ সংখ্যার হতে হবে।" else "PIN must be at least 4 digits."
                                        }
                                    }
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF38BDF8),
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(if (isBn) "পিন রিসেট করুন" else "Reset PIN", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetDialog = false }) {
                                Text(if (isBn) "বাতিল" else "Cancel", color = Color(0xFF94A3B8))
                            }
                        }
                    )
                }

                // Success & New Guardian Key Dialog
                if (showSuccessKeyDialog && newlyGeneratedKey != null) {
                    AlertDialog(
                        onDismissRequest = {
                            showSuccessKeyDialog = false
                            finish()
                        },
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color(0xFF1E293B),
                        tonalElevation = 8.dp,
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Key,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        },
                        title = {
                            Text(
                                text = if (isBn) "নতুন গার্ডিয়ান কি সংরক্ষিত হয়েছে! 🎉" else "New Guardian Key Generated! 🎉",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isBn)
                                        "আপনার পিন সফলভাবে রিসেট হয়েছে এবং আনলক করা হয়েছে। নিরাপত্তার স্বার্থে আপনার জন্য একটি নতুন গার্ডিয়ান কি তৈরি হয়েছে:"
                                    else
                                        "Your PIN has been reset and unlocked. A brand new Guardian Key has been created for your protection:",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0F172A),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = newlyGeneratedKey ?: "",
                                            color = Color(0xFF00FFCC),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp,
                                            letterSpacing = 2.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = if (isBn)
                                        "⚠️ অত্যন্ত গুরুত্বপূর্ণ: এই নতুন কি-টি শুধুমাত্র একবারই প্রদর্শিত হচ্ছে এবং অ্যাপে এটি আর কখনোই দেখা যাবে না! এখনই অভিভাবককে শেয়ার করে রাখুন।"
                                    else
                                        "⚠️ Critical: This new key is shown ONLY ONCE and will never be displayed in the app again! Share it with your guardian immediately.",
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.5.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    newlyGeneratedKey?.let {
                                        LockManager.shareGuardianKey(this@PinLockActivity, it)
                                    }
                                    showSuccessKeyDialog = false
                                    finish()
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBn) "অভিভাবককে কি পাঠিয়ে দিন" else "Share to Guardian", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showSuccessKeyDialog = false
                                    finish()
                                }
                            ) {
                                Text(if (isBn) "বুঝেছি ও প্রবেশ করুন" else "Proceed", color = Color(0xFF94A3B8))
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        super.onBackPressed()
        finish()
    }
}
