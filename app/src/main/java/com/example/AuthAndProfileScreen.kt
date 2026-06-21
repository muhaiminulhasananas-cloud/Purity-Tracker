package com.example

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun ImprovedAuthScreen(
    authViewModel: AuthViewModel,
    userProfileViewModel: UserProfileViewModel,
    purityTrackerViewModel: PurityTrackerViewModel
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val currentUser by authViewModel.currentUser.collectAsState()
    val isUsernameAvailable by userProfileViewModel.isUsernameAvailable.collectAsState()
    var isSignUpMode by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Check Email Link Logic
    LaunchedEffect(activity?.intent) {
        val currentIntent = activity?.intent
        if (currentIntent != null && currentIntent.data != null) {
            authViewModel.checkAndHandleEmailLink(currentIntent, context) { success, msg ->
                Toast.makeText(context, if(success) "$msg ✅" else "$msg ❌", Toast.LENGTH_LONG).show()
                activity.intent = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(CardBlue, DeepBlue)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(1.2.dp, CyanAccent.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                        .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSignUpMode) Icons.Filled.Person else Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CyanAccent
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isSignUpMode) "Register Acccount" else "Welcome Back",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Toggle Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(24.dp))
                    .padding(2.dp)
            ) {
                Button(
                    onClick = { isSignUpMode = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSignUpMode) CyanAccent else Color.Transparent,
                        contentColor = if (isSignUpMode) Color.Black else Color.LightGray
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Register", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { isSignUpMode = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isSignUpMode) CyanAccent else Color.Transparent,
                        contentColor = if (!isSignUpMode) Color.Black else Color.LightGray
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Sign In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            if (isSignUpMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("App Name (Display)") },
                    leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null, tint = CyanAccent) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = Color.Gray,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = CyanAccent) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = Color.Gray,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                ),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = CyanAccent) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = Color.Gray,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                ),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (!isSignUpMode) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = {
                        if (email.isBlank()) {
                            Toast.makeText(context, "Enter email first", Toast.LENGTH_LONG).show()
                            return@TextButton
                        }
                        isLoading = true
                        authViewModel.sendPasswordReset(email) { success, msg ->
                            isLoading = false
                            Toast.makeText(context, if (success) "Reset sent!" else msg, Toast.LENGTH_LONG).show()
                        }
                    }) {
                        Text("Forgot Password?", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                CircularProgressIndicator(color = CyanAccent)
            } else {
                Button(
                    onClick = {
                        if (email.isBlank() || password.length < 6) {
                            Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        if (isSignUpMode) {
                            if (name.isBlank()) {
                                Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                                isLoading = false
                                return@Button
                            }
                            authViewModel.signUpWithEmail(email, name, password, context) { success, msg ->
                                isLoading = false
                                if (!success) Toast.makeText(context, msg ?: "Signup failed", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            authViewModel.signInWithEmail(email, password, context) { success, msg ->
                                isLoading = false
                                if (!success) Toast.makeText(context, msg ?: "Login failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(if (isSignUpMode) "Create Account" else "Login securely", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(0.9f)) {
                Box(modifier = Modifier.height(1.dp).weight(1f).background(Color.White.copy(alpha = 0.15f)))
                Text(" OR ", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.height(1.dp).weight(1f).background(Color.White.copy(alpha = 0.15f)))
            }
            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { authViewModel.signInWithGoogle(context) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Unspecified)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Continue with Google", color = Color(0xFF1E293B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
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

    LaunchedEffect(username) {
        if (username.length >= 3) {
            userProfileViewModel.checkUsernameAvailability(username)
        }
    }

    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Complete Your Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Set a unique username and tell others about yourself.", color = Color.LightGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent, unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
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
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (username.length >= 3) {
                if (isUsernameAvailable == true) {
                    Text("Username available!", color = Color.Green, fontSize = 12.sp)
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
                    focusedBorderColor = CyanAccent, unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
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
fun UserProfileContent(
    userProfile: UserProfile,
    authViewModel: AuthViewModel,
    purityTrackerViewModel: PurityTrackerViewModel,
    socialViewModel: SocialViewModel,
    userProfileViewModel: UserProfileViewModel,
    isPremiumUserState: Boolean,
    onNavigateToStaticPage: (String) -> Unit,
    onOpenFriendsList: () -> Unit = {}
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var editBio by remember { mutableStateOf(userProfile.bio) }
    var editBirthDate by remember { mutableStateOf(userProfile.birthDate) }
    
    val friendReqs by socialViewModel.friendRequests.collectAsState()
    val friends by socialViewModel.friends.collectAsState()

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Profile image
            Box(contentAlignment = Alignment.Center) {
                if (userProfile.profilePicUrl.isNotEmpty()) {
                    BadgeImage(
                        imageUrl = userProfile.profilePicUrl,
                        modifier = Modifier.size(80.dp).clip(CircleShape)
                    )
                } else {
                    Box(modifier = Modifier.size(80.dp).background(Color.Gray, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(50.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(userProfile.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                if (isPremiumUserState) {
                    Spacer(modifier = Modifier.width(6.dp))
                    ProBlueBadge(size = 18.dp) // Assuming ProBlueBadge is accessible here
                }
            }
            Text("@${userProfile.username}", color = CyanAccent, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isEditing) {
                OutlinedTextField(
                    value = editBio, onValueChange = { editBio = it },
                    label = { Text("Bio") }, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editBirthDate, onValueChange = { editBirthDate = it },
                    label = { Text("Birth Date (e.g. 2000-01-01)") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    // Assume userProfileViewModel is somehow available or passed to update?
                    // Actually let's just make it a local call on firestore directly for simplicity or pass vm
                    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(uid)
                        .set(mapOf("bio" to editBio, "birthDate" to editBirthDate), com.google.firebase.firestore.SetOptions.merge())
                    isEditing = false
                }, colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)) {
                    Text("Save", color = Color.Black)
                }
            } else {
                if (userProfile.bio.isNotEmpty()) {
                    Text(userProfile.bio, color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
                }
                if (userProfile.birthDate.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🎂 ${userProfile.birthDate}", color = Color.LightGray, fontSize = 12.sp)
                }
                TextButton(onClick = { isEditing = true }) { Text("Edit Profile", color = CyanAccent) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Friends count
            Row(modifier = Modifier.fillMaxWidth().clickable { onOpenFriendsList() }.padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${friends.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Friends", color = Color.LightGray, fontSize = 12.sp)
                }
            }
            
            // Friend requests incoming
            if (friendReqs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Friend Requests (${friendReqs.size})", color = Color.White, fontWeight = FontWeight.Bold)
                friendReqs.forEach { req ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("User: ${req.fromUserId.take(6)}...", color = Color.LightGray) // Ideally resolve name
                        Row {
                            TextButton(onClick = { socialViewModel.acceptFriendRequest(req.fromUserId) }) { Text("Accept", color = Color.Green) }
                            TextButton(onClick = { socialViewModel.rejectFriendRequest(req.fromUserId) }) { Text("Decline", color = Color.Red) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = { 
                authViewModel.signOut() 
                purityTrackerViewModel.clearAllData()
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)), modifier = Modifier.fillMaxWidth()) {
                Text("Log Out")
            }
            
            if (userProfile.email == "muhaiminulhasananas@gmail.com") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { onNavigateToStaticPage("admin_badges") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.AdminPanelSettings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin: Manage Badges")
                 }
                 Spacer(modifier = Modifier.height(8.dp))
                 Button(onClick = { onNavigateToStaticPage("admin_payments") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)), modifier = Modifier.fillMaxWidth()) {
                     Icon(Icons.Filled.Payment, contentDescription = null, tint = Color.Black)
                     Spacer(modifier = Modifier.width(8.dp))
                     Text("Admin: Review Payments", color = Color.Black)
                 }
            }
        }
    }
}

@Composable
fun FriendsListScreen(
    socialViewModel: SocialViewModel,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    val friends by socialViewModel.friends.collectAsState()
    val friendRequests by socialViewModel.friendRequests.collectAsState()
    var selectedTab by remember { mutableStateOf("friends") } // "friends" or "requests"
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
            Text(if (selectedTab == "friends") "Your Friends" else "Friend Requests", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardBlue)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == "friends") CyanAccent else Color.Transparent)
                    .clickable { selectedTab = "friends" }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Friends (${friends.size})", color = if (selectedTab == "friends") Color.Black else Color.White, fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == "requests") CyanAccent else Color.Transparent)
                    .clickable { selectedTab = "requests" }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Requests", color = if (selectedTab == "requests") Color.Black else Color.White, fontWeight = FontWeight.Medium)
                    if (friendRequests.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.background(Color.Red, CircleShape).padding(horizontal = 6.dp, vertical = 2.dp), contentAlignment = Alignment.Center) {
                            Text(friendRequests.size.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (selectedTab == "friends") {
            if (friends.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You don't have any friends yet.", color = Color.LightGray)
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn {
                    items(friends.size) { index ->
                        val friendId = friends[index]
                        FriendListItem(friendId = friendId, socialViewModel = socialViewModel, onOpenChat = onOpenChat)
                        if (index < friends.size - 1) {
                            Divider(color = Color.White.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        } else {
            if (friendRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No pending friend requests.", color = Color.LightGray)
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn {
                    items(friendRequests.size) { index ->
                        val request = friendRequests[index]
                        FriendRequestItem(request = request, socialViewModel = socialViewModel)
                        if (index < friendRequests.size - 1) {
                            Divider(color = Color.White.copy(alpha = 0.1f))
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
    
    LaunchedEffect(request.fromUserId, request.toUserId) {
        val uid = if (request.fromUserId == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid) request.toUserId else request.fromUserId
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    userProfile = snapshot.toObject(UserProfile::class.java)?.copy(userId = snapshot.id)
                }
            }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (userProfile != null && userProfile!!.profilePicUrl.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = userProfile!!.profilePicUrl,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(40.dp).background(Color.Gray, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.LightGray)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            val name = userProfile?.name?.takeIf { it.isNotBlank() } ?: "User"
            val isPremium = userProfile?.isPremium == true || userProfile?.email == "muhaiminulhasananas@gmail.com"
            Row(verticalAlignment = Alignment.CenterVertically) {
                com.example.PremiumNameText(
                    name = name,
                    isPremium = isPremium,
                    defaultColor = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (isPremium) {
                    Spacer(modifier = Modifier.width(4.dp))
                    com.example.ProBlueBadge(size = 16.dp)
                }
                if (userProfile?.email == "muhaiminulhasananas@gmail.com") {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(16.dp))
                }
            }
        }
        
        val isSentByMe = request.fromUserId == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (isSentByMe) {
            Text("Pending", color = Color.Gray, fontSize = 12.sp)
        } else {
            Row {
                IconButton(onClick = { socialViewModel.acceptFriendRequest(request.fromUserId) }, modifier = Modifier.background(Color(0xFF3B82F6), CircleShape).size(36.dp)) {
                    Icon(Icons.Filled.Check, contentDescription = "Accept", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { socialViewModel.rejectFriendRequest(request.fromUserId) }, modifier = Modifier.background(Color(0xFFEF4444), CircleShape).size(36.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Reject", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun FriendListItem(friendId: String, socialViewModel: SocialViewModel, onOpenChat: (String) -> Unit) {
    var friendProfile by remember { mutableStateOf<UserProfile?>(null) }
    val unreadCounts by socialViewModel.unreadCounts.collectAsState()
    val unreadCount = unreadCounts[friendId] ?: 0

    LaunchedEffect(friendId) {
        val listener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(friendId).addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    friendProfile = snapshot.toObject(UserProfile::class.java)?.copy(userId = snapshot.id)
                }
            }
        kotlinx.coroutines.awaitCancellation()
    }

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onOpenChat(friendId) }.padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (friendProfile != null && friendProfile!!.profilePicUrl.isNotEmpty()) {
                BadgeImage(
                    imageUrl = friendProfile!!.profilePicUrl,
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
            } else {
                Box(modifier = Modifier.size(40.dp).background(Color.Gray, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            
            val name = friendProfile?.name?.takeIf { it.isNotBlank() } ?: "Friend"
            val isPremium = friendProfile?.isPremium == true || friendProfile?.email == "muhaiminulhasananas@gmail.com"
            Row(verticalAlignment = Alignment.CenterVertically) {
                com.example.PremiumNameText(
                    name = name,
                    isPremium = isPremium,
                    defaultColor = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (isPremium) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Filled.Verified, contentDescription = "Pro", tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                }
                if (friendProfile?.email == "muhaiminulhasananas@gmail.com") {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(16.dp))
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .background(Color.Red, CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(unreadCount.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.Filled.ChatBubble, contentDescription = "Chat", tint = CyanAccent)
        }
    }
}

@Composable
fun PrivateChatScreen(
    friendId: String,
    socialViewModel: SocialViewModel,
    onBack: () -> Unit
) {
    val messages by socialViewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val currentUserUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current
    val isMePremium by com.example.PremiumManager.isPremium.collectAsState()

    var friendProfile by remember { mutableStateOf(UserProfile(userId = friendId)) }

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
        onDispose {
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

    LaunchedEffect(friendId) {
        socialViewModel.listenForMessages(friendId)
        socialViewModel.markChatAsRead(friendId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            socialViewModel.markChatAsRead(friendId)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(CardBlue).padding(16.dp).fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
            Spacer(modifier = Modifier.width(8.dp))
            if (friendProfile.profilePicUrl.isNotEmpty()) {
                BadgeImage(
                    imageUrl = friendProfile.profilePicUrl,
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
            } else {
                Box(modifier = Modifier.size(40.dp).background(Color.Gray, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                val isFriendPremium = friendProfile.isPremium || friendProfile.email == "muhaiminulhasananas@gmail.com"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.example.PremiumNameText(
                        name = friendProfile.name.ifBlank { "Friend" },
                        isPremium = isFriendPremium,
                        defaultColor = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isFriendPremium) {
                        Spacer(modifier = Modifier.width(4.dp))
                        com.example.ProBlueBadge(size = 18.dp)
                    }
                    if (friendProfile.email == "muhaiminulhasananas@gmail.com") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(18.dp))
                    }
                }
                Text(friendProfile.badge.ifBlank { "Chat" }, color = CyanAccent, fontSize = 12.sp)
            }
        }
        
        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp), reverseLayout = true) {
            items(messages.size) { index ->
                val msg = messages[messages.size - 1 - index]
                val isMe = msg.senderId == currentUserUid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start) {
                    Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                        val isSenderPremium = if (isMe) {
                            isMePremium || com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email == "muhaiminulhasananas@gmail.com"
                        } else {
                            friendProfile.isPremium || friendProfile.email == "muhaiminulhasananas@gmail.com"
                        }

                        if (!isMe) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                com.example.PremiumNameText(
                                    name = friendProfile.name.ifBlank { "Friend" },
                                    isPremium = isSenderPremium,
                                    defaultColor = CyanAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isSenderPremium) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Filled.Verified, contentDescription = "Pro", tint = Color(0xFF3B82F6), modifier = Modifier.size(12.dp))
                                }
                                if (friendProfile.email == "muhaiminulhasananas@gmail.com") {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(12.dp))
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSenderPremium) {
                                    Icon(Icons.Filled.Verified, contentDescription = "Pro", tint = Color(0xFF3B82F6), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email == "muhaiminulhasananas@gmail.com") {
                                    Icon(Icons.Filled.Verified, contentDescription = "Admin", tint = CyanAccent, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                com.example.PremiumNameText(
                                    name = "You",
                                    isPremium = isSenderPremium,
                                    defaultColor = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))

                        if (msg.imageUrl.isNotEmpty()) {
                            coil.compose.AsyncImage(
                                model = msg.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.width(200.dp).height(200.dp).clip(RoundedCornerShape(12.dp)).padding(bottom = 4.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else if (msg.videoUrl.isNotEmpty()) {
                            Box(modifier = Modifier.width(200.dp).height(200.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black).padding(bottom = 4.dp), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.PlayCircle, contentDescription = "Video", tint = Color.White, modifier = Modifier.size(48.dp))
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
                                    .padding(vertical = 4.dp)
                                    .background(if (isMe) Color(0xFFEF4444) else CardBlue, RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                IconButton(
                                    onClick = { voicePlayerManager.play(msg.id, msg.audioUrl) {} },
                                    modifier = Modifier.size(36.dp).background(if (isMe) Color.White.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = if (isPlaying) "Pause" else "Play", tint = if (isMe) Color.White else CyanAccent, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f))) {
                                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress).background(if (isMe) Color.White else CyanAccent))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Filled.Mic, contentDescription = null, tint = if (isMe) Color.White else CyanAccent, modifier = Modifier.size(16.dp))
                            }
                        } else if (msg.text.isNotEmpty()) {
                            Box(modifier = Modifier
                                .clip(RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isMe) 12.dp else 0.dp,
                                    bottomEnd = if (isMe) 0.dp else 12.dp
                                ))
                                .background(if (isMe) Color(0xFFEF4444) else CardBlue)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            if (isMe) {
                                                msgToEditOrDelete = msg
                                            }
                                        }
                                    )
                                }
                                .padding(12.dp)
                            ) {
                                 Text(msg.text, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF0F172A)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isRecording) {
                Row(
                    modifier = Modifier.weight(1f).background(CardBlue, RoundedCornerShape(24.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).background(Color.Red, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("0:0${recordingDurationSec} / 0:60", color = Color.White)
                    }
                    Text("Delete", color = Color.Red, fontSize = 14.sp, modifier = Modifier.clickable { cancelRecordingLambda() })
                }
            } else {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message...", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = CardBlue,
                        unfocusedContainerColor = CardBlue
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (isRecording) {
                IconButton(
                    onClick = stopAndSendRecordingLambda,
                    modifier = Modifier.background(GreenText, CircleShape)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            } else if (messageText.isBlank()) {
                val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
                IconButton(
                    onClick = {
                        if (hasPermission) startRecordingLambda()
                        else permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier.background(CyanAccent.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = "Mic", tint = CyanAccent)
                }
            } else {
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            socialViewModel.sendMessage(friendId, messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier.background(CyanAccent, CircleShape)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.Black)
                }
            }
        }
    }

    if (msgToEditOrDelete != null && !showEditDialog && !showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { msgToEditOrDelete = null },
            title = { Text("Message Options") },
            text = {
                Column {
                    Text("Edit Message", modifier = Modifier.fillMaxWidth().clickable {
                        editText = msgToEditOrDelete!!.text
                        showEditDialog = true
                    }.padding(16.dp))
                    Text("Delete Message", color = Color.Red, modifier = Modifier.fillMaxWidth().clickable {
                        showDeleteDialog = true
                    }.padding(16.dp))
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { msgToEditOrDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showEditDialog && msgToEditOrDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditDialog = false; msgToEditOrDelete = null },
            title = { Text("Edit Message") },
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
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false; msgToEditOrDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog && msgToEditOrDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false; msgToEditOrDelete = null },
            title = { Text("Delete Message") },
            text = { Text("Are you sure you want to delete this message?") },
            confirmButton = {
                TextButton(onClick = {
                    socialViewModel.deleteMessage(friendId, msgToEditOrDelete!!.id)
                    showDeleteDialog = false
                    msgToEditOrDelete = null
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; msgToEditOrDelete = null }) { Text("Cancel") }
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
    val isFriend = friends.contains(userProfile.userId)
    val hasSentReq = friendReqs.any { it.toUserId == userProfile.userId || it.fromUserId == userProfile.userId }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)).clickable { /* Consume clicks */ }) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
            Text(userProfile.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = CardBlue)) {
                    Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (userProfile.profilePicUrl.isNotEmpty()) {
                            BadgeImage(
                                imageUrl = userProfile.profilePicUrl,
                                modifier = Modifier.size(100.dp).clip(CircleShape)
                            )
                        } else {
                            Box(modifier = Modifier.size(100.dp).background(Color.Gray, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(60.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(userProfile.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("@${userProfile.username}", color = CyanAccent, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (userProfile.bio.isNotEmpty()) {
                            Text(userProfile.bio, color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (currentUserId != userProfile.userId) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                if (isFriend) {
                                    Button(onClick = { /* Already friend */ }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                                        Text("Friends", color = Color.White)
                                    }
                                } else if (hasSentReq) {
                                    Button(onClick = { /* Pending */ }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                                        Text("Request Sent/Pending", color = Color.White)
                                    }
                                } else {
                                    Button(onClick = { 
                                        socialViewModel.sendFriendRequest(userProfile.userId)
                                        android.widget.Toast.makeText(context, "Friend request sent!", android.widget.Toast.LENGTH_SHORT).show()
                                    }, colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)) {
                                        Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add Friend", color = Color.Black)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                IconButton(onClick = { onOpenChat(userProfile) }, modifier = Modifier.background(Color(0xFF4B5563), CircleShape)) {
                                    Icon(Icons.Filled.ChatBubble, contentDescription = "Message", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (userPosts.isEmpty()) {
                    Text("No posts from this user.", color = Color.LightGray, modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center)
                } else {
                    Text("User Posts", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
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
