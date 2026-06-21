package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(application)
    
    // State to reflect user login
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            try {
                // Get the string resource for Web Client ID
                val webClientId = context.getString(R.string.web_client_id)
                if (webClientId == "YOUR_WEB_CLIENT_ID_HERE" || webClientId.isBlank()) {
                    Log.e("AuthViewModel", "WEB CLIENT ID IS MISSING. Please update strings.xml or google-services.json")
                    return@launch
                }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _currentUser.value = auth.currentUser
                            Log.d("AuthViewModel", "Signed in as: ${auth.currentUser?.email}")
                        } else {
                            Log.e("AuthViewModel", "Sign-in failed", task.exception)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Credential manager failed", e)
            }
        }
    }

    fun signInWithEmail(email: String, pass: String, context: Context, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _currentUser.value = auth.currentUser
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message ?: "Login failed")
                }
            }
    }

    fun signUpWithEmail(email: String, name: String, pass: String, context: Context, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && name.isNotBlank()) {
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(name.trim())
                            .build()
                        user.updateProfile(profileUpdates).addOnCompleteListener {
                            _currentUser.value = auth.currentUser
                            onComplete(true, null)
                        }
                    } else {
                        _currentUser.value = auth.currentUser
                        onComplete(true, null)
                    }
                } else {
                    onComplete(false, task.exception?.message ?: "Registration failed")
                }
            }
    }

    fun sendPasswordlessSignInLink(email: String, name: String, onComplete: (Boolean, String?) -> Unit) {
        val actionCodeSettings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
            .setUrl("https://ais-pre-spzdpff2lbcs3767oevm2f-22495587935.asia-east1.run.app")
            .setHandleCodeInApp(true)
            .setAndroidPackageName(
                "com.example",
                true,
                "21"
            )
            .build()

        auth.sendSignInLinkToEmail(email.trim(), actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val context = getApplication<Application>().applicationContext
                    context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putString("pending_email", email.trim())
                        .putString("pending_name", name.trim())
                        .apply()
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message ?: "Failed to send link")
                }
            }
    }

    fun signInWithEmailLinkManual(email: String, emailLink: String, name: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailLink(email.trim(), emailLink)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        _currentUser.value = user
                        if (name.isNotBlank() && user.displayName.isNullOrBlank()) {
                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(name.trim())
                                .build()
                            user.updateProfile(profileUpdates).addOnCompleteListener {
                                _currentUser.value = auth.currentUser
                                onComplete(true, null)
                            }
                        } else {
                            onComplete(true, null)
                        }
                    } else {
                        onComplete(false, "User session empty")
                    }
                } else {
                    onComplete(false, task.exception?.message ?: "Sign-in failed")
                }
            }
    }

    fun checkAndHandleEmailLink(intent: android.content.Intent, context: Context, onResult: (Boolean, String?) -> Unit) {
        val emailLink = intent.dataString ?: return
        if (auth.isSignInWithEmailLink(emailLink)) {
            val prefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
            val email = prefs.getString("pending_email", "") ?: ""
            val name = prefs.getString("pending_name", "") ?: ""
            
            if (email.isNotBlank()) {
                auth.signInWithEmailLink(email, emailLink)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                _currentUser.value = user
                                if (name.isNotBlank() && (user.displayName.isNullOrBlank() || user.displayName == "Anonymous Warrior")) {
                                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                        .setDisplayName(name.trim())
                                        .build()
                                    user.updateProfile(profileUpdates).addOnCompleteListener {
                                        _currentUser.value = auth.currentUser
                                        onResult(true, "Authentication successful! Welcome, $name")
                                    }
                                } else {
                                    onResult(true, "Authentication successful!")
                                }
                            } else {
                                onResult(false, "User session empty")
                            }
                        } else {
                            onResult(false, task.exception?.message ?: "Link authentication failed")
                        }
                    }
            } else {
                onResult(false, "Email confirmation needed")
            }
        }
    }
    
    fun sendPasswordReset(email: String, onComplete: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message ?: "Failed to reset password")
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        PremiumManager.setPremiumStatus(getApplication(), false)
    }
}
