package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

class PinLockActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // If there's no PIN set, they shouldn't even be here natively, but just in case:
        if (!LockManager.hasPin(this)) {
            finish()
            return
        }

        setContent {
            var pinInput by remember { mutableStateOf("") }
            val darkBg = Color(0xFF1E2124)
            val cyanAccent = Color(0xFF00FFCC)

            Surface(modifier = Modifier.fillMaxSize(), color = darkBg) {
                Column(
                    modifier = Modifier.padding(24.dp), 
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Security Verification", 
                        style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Please enter your PIN.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = { Text("Enter PIN", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cyanAccent,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            val savedPin = LockManager.getPin(this@PinLockActivity)
                            if (pinInput == savedPin) {
                                LockManager.unlockForSeconds(this@PinLockActivity, 30)
                                Toast.makeText(this@PinLockActivity, "Unlocked for 30 seconds", Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                Toast.makeText(this@PinLockActivity, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                                pinInput = "" // Clear input
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = cyanAccent, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                    ) {
                        Text("Unlock Settings", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(onClick = { 
                        val homeIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                            addCategory(android.content.Intent.CATEGORY_HOME)
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(homeIntent)
                        finish() 
                    }) {
                        Text("Go Back", color = Color.LightGray)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        val homeIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_HOME)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        super.onBackPressed()
        finish()
    }
}
