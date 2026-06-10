package com.example

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PremiumPaywallScreen(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val currentLang = LanguageStore.currentLanguage.value
    val isBn = currentLang == "bn"
    
    // Helpers for translation
    fun pt(en: String, bn: String): String = if (isBn) bn else en

    var selectedPlan by remember { mutableStateOf("monthly") } // "monthly", "yearly", "family"
    var showPaymentSheet by remember { mutableStateOf(false) }
    var selectedMethod by remember { mutableStateOf("bkash") } // "bkash", "nagad", "rocket"
    
    // manual payment details
    var walletNumber by remember { mutableStateOf("") }
    var transactionId by remember { mutableStateOf("") }
    var sentAmount by remember { mutableStateOf("") }
    var paymentStep by remember { mutableStateOf(1) } // 1: Input details, 2: Success Confirmation
    var isVerifyingPayment by remember { mutableStateOf(false) }
    
    // Auto sync price amount when plan changes
    LaunchedEffect(selectedPlan) {
        sentAmount = when (selectedPlan) {
            "monthly" -> "149"
            "yearly" -> "799"
            else -> "1999"
        }
    }
    
    val planPrice = when (selectedPlan) {
        "monthly" -> "৳১৪৯"
        "yearly" -> "৳৭৯৯"
        else -> "৳১,৯৯৯"
    }
    
    val planPriceEn = when (selectedPlan) {
        "monthly" -> "149 BDT"
        "yearly" -> "799 BDT"
        else -> "1,999 BDT"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlue)
    ) {
        // Gradient glow background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E2A40).copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pt("PRO", "খেতাব প্রো"),
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Glowing crown & logo
            val infiniteTransition = rememberInfiniteTransition()
            val floatAnim by infiniteTransition.animateFloat(
                initialValue = -8f,
                targetValue = 8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Box(
                modifier = Modifier
                    .offset(y = floatAnim.dp)
                    .size(90.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = pt("Upgrade to Warrior Pro 👑", "যোদ্ধা প্রো-তে আপগ্রেড করুন 👑"),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = pt("Unlock ultimate recovery and exclusive community benefits", "পবিত্র চরিত্র গঠন ও অসাধারণ প্রিমিয়াম সুযোগসমূহ আনলক করুন"),
                color = Color.LightGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Features Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBlue),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = pt("EXCLUSIVE PRO FEATURES", "প্রিমিয়াম ফিচারের তালিকা:"),
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    val features = listOf(
                        pt("Unlimited Counselor AI Counseling", "আনলিমিটেড এআই রুকইয়াহ্ ও আসক্তি নিরাময় কাউন্সেলিং (No Daily Limit)") to "psychology",
                        pt("Premium Adhans (Medina, Mecca, Al-Aqsa)", "মদীনা, মক্কা ও আল-আকসার প্রিমিয়াম রিংটোন ও অফলাইন আজান এলার্ট") to "notifications_active",
                        pt("Guardian SMS Relapse Incident Alert", "ভুল বা রিসেট হলে অটোমেটিক অভিভাবকের ফোনে ইনস্ট্যান্ট সিকিউর অ্যালার্ট") to "security",
                        pt("Golden VIP Crown Badge & Glowing Aura on Chat", "ভিআইপি গোল্ডেন প্রো ব্যাজ ও মেসেজে আকর্ষণীয় নেওন গ্লোয়িং বর্ডার") to "stars",
                        pt("Detailed trigger, mood logs & protection charts", "আসক্তির ট্রিগার অ্যানালাইসিস ও বিস্তারিত প্রটেকশন চার্ট") to "analytics"
                    )

                    features.forEach { (text, iconStr) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = when (iconStr) {
                                    "psychology" -> Icons.Default.Psychology
                                    "notifications_active" -> Icons.Default.NotificationsActive
                                    "security" -> Icons.Default.Shield
                                    "stars" -> Icons.Default.Stars
                                    else -> Icons.Default.Leaderboard
                                },
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = text,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Plan Selection Section
            Text(
                text = pt("Choose your spiritual recovery plan:", "অনুকূল প্যাকেজটি বেছে নিন:"),
                color = Color.LightGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing Boxes
            val plans = listOf(
                Triple("monthly", pt("Monthly Pro", "১ মাস মেয়াদী"), pt("৳১৪৯/mo", "৳১৪৯ / মাস")),
                Triple("yearly", pt("Yearly Pro\n(Save 55%)", "১ বছর মেয়াদী\n(৫৫% ছাড়)"), pt("৳৭৯৯/yr", "৳৭৯৯ / বছর")),
                Triple("family", pt("Family Pro\n(Pariwar)", "ফ্যামিলি প্যাক\n(আজীবন মেয়াদী)"), pt("৳১,৯৯৯", "৳১,৯৯৯ / আজীবন মেয়াদী"))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                plans.forEach { (id, title, priceText) ->
                    val isSelected = selectedPlan == id
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPlan = id },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFFFD700).copy(alpha = 0.08f) else CardBlue
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color(0xFFFFD700) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                minLines = 2
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = priceText,
                                color = if (isSelected) Color(0xFFFFD700) else GreenText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Upgrade Button
            Button(
                onClick = { showPaymentSheet = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = pt("Join Warrior Pro for $planPriceEn", "যোদ্ধা প্রো-তে যোগ দিন (মূল্য: $planPrice)"),
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = pt("🔒 100% Risk Free • Instant Access • Encrypted Payment", "🔒 শতভাগ নিরাপদ গেটওয়ে • ওয়ান-ক্লিক ইনস্ট্যান্ট অ্যাক্টিভেশন"),
                color = Color.Gray,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }

        // SIMULATED SECURE PAYMENT GATEWAY (As custom bottom sheet dialog style overlay!)
        if (showPaymentSheet) {
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { 
                        if (!isVerifyingPayment) showPaymentSheet = false 
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                // Prevent inner clicks from closing
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .clickable(enabled = false, onClick = {})
                        .background(Color(0xFF151F30)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151F30)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Drag handle
                        Box(
                            modifier = Modifier
                                .size(40.dp, 4.dp)
                                .background(Color.Gray.copy(alpha = 0.4f), CircleShape)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        if (paymentStep == 1) {
                            Text(
                                text = pt("Manual Secure Payment 🔒", "ম্যানুয়াল পেমেন্ট গেটওয়ে 🔒"),
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pt("Total Payable Amount: $planPriceEn", "মোট পরিশোধযোগ্য মূল্য: $planPrice"),
                                color = GreenText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Method tabs (bKash, Nagad, Rocket, Upay)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val methods = listOf(
                                    Pair("bkash", pt("bKash", "বিকাশ")),
                                    Pair("nagad", pt("Nagad", "নগদ")),
                                    Pair("rocket", pt("Rocket", "রকেট")),
                                    Pair("upay", pt("Upay", "উপায়"))
                                )
                                
                                methods.forEach { (id, name) ->
                                    val isMethodSelected = selectedMethod == id
                                    val baseColor = when (id) {
                                        "bkash" -> Color(0xFFE2125D)
                                        "nagad" -> Color(0xFFF15A22)
                                        "rocket" -> Color(0xFF8C3494)
                                        else -> Color(0xFF00ADEE) // Upay brand color
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isMethodSelected) baseColor else CardBlue)
                                            .border(1.dp, if (isMethodSelected) baseColor else Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                                            .clickable { selectedMethod = id }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = name,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Display Personal Number and Copy button
                            val pNumber = when (selectedMethod) {
                                "bkash", "nagad" -> "01862391580" // bkash & nagad
                                else -> "01797277453" // rocket & upay
                            }
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = pt("Send Money (Personal Number):", "সেন্ড মানি করুন (পার্সোনাল নাম্বার):"),
                                        color = Color.LightGray,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$pNumber (${selectedMethod.uppercase()})",
                                            color = Color(0xFFFFD700),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            letterSpacing = 1.sp
                                        )
                                        
                                        Button(
                                            onClick = {
                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(pNumber))
                                                android.widget.Toast.makeText(context, pt("Copied to Clipboard!", "নাম্বারটি কপি করা হয়েছে!"), android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(pt("Copy", "কপি করুন"), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = pt("Instruction: Please send the exact amount to the personal number above, copy the transaction ID (TxnID) and enter details below.", "নির্দেশনাঃ উপরে উল্লেখিত নাম্বারে সঠিক পরিমাণ টাকা সেন্ড মানি করুন। তারপর ট্রানজেকশন আইডি কপি করে নিচের ফর্মে বসিয়ে পেমেন্ট রিকোয়েস্ট পাঠান।"),
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Form inputs
                            OutlinedTextField(
                                value = walletNumber,
                                onValueChange = { walletNumber = it },
                                label = { Text(pt("Your Wallet Number (from where you sent)", "যেই নাম্বার থেকে টাকা পাঠিয়েছেন"), color = Color.Gray, fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD700)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = sentAmount,
                                    onValueChange = { sentAmount = it },
                                    label = { Text(pt("Sent Amount (BDT)", "টাকার পরিমাণ (টাকা)"), color = Color.Gray, fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFFFD700)
                                    )
                                )
                                
                                OutlinedTextField(
                                    value = transactionId,
                                    onValueChange = { transactionId = it },
                                    label = { Text(pt("Transaction ID (TxnID)", "ট্রানজেকশন আইডি (TxnID)"), color = Color.Gray, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1.3f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFFFD700)
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            if (isVerifyingPayment) {
                                CircularProgressIndicator(color = Color(0xFFFFD700))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = pt("Submitting payment request to database...", "পেমেন্ট রিকোয়েস্ট ডাটাবেজে পাঠানো হচ্ছে..."),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            } else {
                                Button(
                                    onClick = {
                                        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                                        if (currentUser == null) {
                                            android.widget.Toast.makeText(context, pt("Please sign in or register first!", "অনুগ্রহ করে আগে প্রোফাইল পেজে গিয়ে সাইন ইন বা রেজিস্টার করুন!"), android.widget.Toast.LENGTH_LONG).show()
                                            return@Button
                                        }
                                        if (walletNumber.length < 10 || sentAmount.isBlank() || transactionId.isBlank()) {
                                            android.widget.Toast.makeText(context, pt("Please fill up all field correctly!", "অনুগ্রহ করে সবগুলো তথ্য সঠিকভাবে দিন!"), android.widget.Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        
                                        isVerifyingPayment = true
                                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                        val reqId = db.collection("payment_requests").document().id
                                        
                                        val requestData = mapOf(
                                            "id" to reqId,
                                            "userId" to currentUser.uid,
                                            "userEmail" to (currentUser.email ?: ""),
                                            "userName" to (currentUser.displayName ?: "Warrior User"),
                                            "planId" to selectedPlan,
                                            "senderNumber" to walletNumber,
                                            "txnId" to transactionId,
                                            "amount" to "$sentAmount BDT",
                                            "method" to selectedMethod,
                                            "status" to "pending",
                                            "timestamp" to System.currentTimeMillis()
                                        )
                                        
                                        db.collection("payment_requests").document(reqId)
                                            .set(requestData)
                                            .addOnSuccessListener {
                                                isVerifyingPayment = false
                                                paymentStep = 2
                                            }
                                            .addOnFailureListener { e ->
                                                isVerifyingPayment = false
                                                android.widget.Toast.makeText(context, "ব্যর্থ হয়েছে: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(pt("Submit Payment Verification Request", "পেমেন্ট রিকোয়েস্ট পাঠান"), color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Step 2: SUCCESS SUBMITTED CONFIRMATION!
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = GreenText,
                                modifier = Modifier.size(72.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Text(
                                text = pt("Payment Request Submitted! 🎉", "পেমেন্ট রিকোয়েস্ট পাঠানো হয়েছে! 🎉"),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = pt(
                                    "Your transaction is listed. Admin will verify details and unlock your chosen Pro package shortly. Thank you!",
                                    "আপনার সাবমিট করা তথ্যটি সিস্টেমে নথিভুক্ত করা হয়েছে। এডমিন আপনার ট্রানজেকশন আইডি ও নাম্বার চেক করে অল্প সময়ের ভেতরেই আপনার অ্যাকাউন্টটির জন্য নির্বাচিত প্রো প্যাক আনলক করে দেবেন। ধন্যবাদ!"
                                ),
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp),
                                lineHeight = 16.sp
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = {
                                    showPaymentSheet = false
                                    paymentStep = 1
                                    walletNumber = ""
                                    transactionId = ""
                                    onClose()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(pt("Close", "বন্ধ করুন"), color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Data class representation of premium requests
data class PaymentRequest(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val planId: String = "",
    val senderNumber: String = "",
    val txnId: String = "",
    val amount: String = "",
    val method: String = "",
    val status: String = "", // "pending", "approved", "rejected"
    val timestamp: Long = 0L
)

// Admin Approvals panel for the main account holder
@Composable
fun AdminPaymentsScreen(onBack: () -> Unit) {
    val db = remember { com.google.firebase.firestore.FirebaseFirestore.getInstance() }
    val context = LocalContext.current
    var requests by remember { mutableStateOf<List<PaymentRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentFilter by remember { mutableStateOf("pending") } // "pending", "all"
    
    DisposableEffect(Unit) {
        val listener = db.collection("payment_requests")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                isLoading = false
                if (e == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val userId = doc.getString("userId") ?: ""
                        val userEmail = doc.getString("userEmail") ?: ""
                        val userName = doc.getString("userName") ?: ""
                        val planId = doc.getString("planId") ?: ""
                        val senderNumber = doc.getString("senderNumber") ?: ""
                        val txnId = doc.getString("txnId") ?: ""
                        val amount = doc.getString("amount") ?: ""
                        val method = doc.getString("method") ?: ""
                        val status = doc.getString("status") ?: ""
                        val timestamp = doc.getLong("timestamp") ?: 0L
                        PaymentRequest(id, userId, userEmail, userName, planId, senderNumber, txnId, amount, method, status, timestamp)
                    }
                    requests = list
                }
            }
        onDispose {
            listener.remove()
        }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("পেমেন্ট পেন্ডিং রিকোয়েস্ট", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF151F30))
            )
        },
        containerColor = DeepBlue
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Filter states tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { currentFilter = "pending" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentFilter == "pending") Color(0xFFFFD700) else CardBlue,
                        contentColor = if (currentFilter == "pending") Color.Black else Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("পেন্ডিং রিকোয়েস্ট (${requests.count { it.status == "pending" }})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                
                Button(
                    onClick = { currentFilter = "all" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentFilter == "all") Color(0xFFFFD700) else CardBlue,
                        contentColor = if (currentFilter == "all") Color.Black else Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("সকল হিস্টোরি (${requests.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val filteredList = if (currentFilter == "pending") {
                requests.filter { it.status == "pending" }
            } else {
                requests
            }
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFFD700))
                }
            } else if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("কোনো পেমেন্ট রিকোয়েস্ট পাওয়া যায়নি।", color = Color.Gray, textAlign = TextAlign.Center)
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList.size) { index ->
                        val request = filteredList[index]
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBlue),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = request.userName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    
                                    Badge(
                                        containerColor = when (request.status) {
                                            "pending" -> Color(0xFFFFD700).copy(alpha = 0.15f)
                                            "approved" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                            else -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                        },
                                        contentColor = when (request.status) {
                                            "pending" -> Color(0xFFFFD700)
                                            "approved" -> Color(0xFF10B981)
                                            else -> Color(0xFFEF4444)
                                        },
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        Text(
                                            text = request.status.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                
                                Text(text = "ইমেইল: ${request.userEmail}", color = Color.LightGray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = Color.White.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("প্যাকেজ: ${request.planId.uppercase()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("টাকা: ${request.amount}", color = GreenText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("মাধ্যম: ${request.method.uppercase()}", color = Color.LightGray, fontSize = 12.sp)
                                        Text("পাঠানো নাম্বার: ${request.senderNumber}", color = Color.LightGray, fontSize = 12.sp)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "ট্রানজেকশন আইডি: ${request.txnId}",
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                                
                                if (request.status == "pending") {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val dbRef = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                dbRef.collection("payment_requests").document(request.id)
                                                    .update("status", "rejected")
                                                    .addOnSuccessListener {
                                                        android.widget.Toast.makeText(context, "অনুরোধটি বাতিল করা হয়েছে", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Reject (বাতিল)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        
                                        Button(
                                            onClick = {
                                                val dbRef = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                val batch = dbRef.batch()
                                                
                                                val reqRef = dbRef.collection("payment_requests").document(request.id)
                                                batch.update(reqRef, "status", "approved")
                                                
                                                val userRef = dbRef.collection("users").document(request.userId)
                                                val updates = mapOf(
                                                    "isPremium" to true,
                                                    "premiumPlan" to request.planId
                                                )
                                                batch.set(userRef, updates, com.google.firebase.firestore.SetOptions.merge())
                                                
                                                batch.commit()
                                                    .addOnSuccessListener {
                                                        android.widget.Toast.makeText(context, "প্যাকেজ আনলক সফল হয়েছে! 👑", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Approve (এপ্রুভ)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
