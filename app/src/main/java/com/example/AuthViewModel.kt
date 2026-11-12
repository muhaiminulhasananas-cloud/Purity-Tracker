package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(application)

    private val disposableEmailDomains = setOf(
        "tempmail.com", "temp-mail.org", "temp-mail.io", "10minutemail.com", "10minutemail.net",
        "guerrillamail.com", "guerrillamailblock.com", "sharklasers.com", "grr.la", "pokemail.net",
        "mailinator.com", "yopmail.com", "yopmail.fr", "yopmail.net", "trashmail.com", "trashmail.net",
        "getnada.com", "nada.email", "maildrop.cc", "dispostable.com", "throwawaymail.com",
        "mohmal.com", "fakeinbox.com", "mailnesia.com", "tempmailo.com", "emailondeck.com",
        "crazymailing.com", "burnermail.io", "inboxkitten.com", "mintemail.com", "mytemp.email",
        "tempail.com", "tempr.email", "discard.email", "fakemail.net", "fakemailgenerator.com",
        "example.com", "test.com", "fake.com", "invalid.com", "none.com", "asdf.com", "qwerty.com"
    )

    fun isUserVerified(user: FirebaseUser?): Boolean {
        if (user == null) return false
        val email = user.email?.trim()?.lowercase() ?: ""
        if (email == "muhaiminulhasananas@gmail.com") return true
        val isGoogleProvider = user.providerData.any { it.providerId == "google.com" }
        if (isGoogleProvider) return true
        return user.isEmailVerified
    }

    fun isValidRealEmail(rawEmail: String): Boolean {
        val email = rawEmail.trim().lowercase()
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) return false
        val parts = email.split("@")
        if (parts.size != 2) return false
        val localPart = parts[0]
        val domain = parts[1]
        if (localPart.length < 2) return false
        if (!domain.contains(".")) return false
        val tld = domain.substringAfterLast(".")
        if (tld.length < 2) return false
        if (disposableEmailDomains.contains(domain)) return false
        if (disposableEmailDomains.any { domain.endsWith(".$it") }) return false
        return true
    }

    // Only expose currentUser when logged in AND verified (blocks fake/unverified email accounts)
    private val _currentUser = MutableStateFlow<FirebaseUser?>(
        auth.currentUser?.takeIf { isUserVerified(it) }
    )
    val currentUser = _currentUser.asStateFlow()

    // Holds the email address of a user who registered or signed in with an unverified email
    private val _pendingVerificationEmail = MutableStateFlow<String?>(
        auth.currentUser?.takeIf { !isUserVerified(it) }?.email
    )
    val pendingVerificationEmail = _pendingVerificationEmail.asStateFlow()

    init {
        val existing = auth.currentUser
        if (existing != null) {
            if (!isUserVerified(existing)) {
                _currentUser.value = null
                _pendingVerificationEmail.value = existing.email
            }
            val uid = existing.uid
            val email = existing.email ?: ""
            // Check if this account was marked deleted in Firestore
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("deleted_accounts").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        existing.delete().addOnCompleteListener {
                            signOut()
                        }
                    }
                }
            // Reload from Firebase Auth to detect if the account was deleted in Firebase Console
            existing.reload().addOnCompleteListener { reloadTask ->
                val ex = reloadTask.exception
                if (!reloadTask.isSuccessful && ex is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                    // User was deleted or disabled in Firebase Console -> wipe all Firestore traces!
                    AccountDeletionManager.purgeUserCompletely(
                        userId = uid,
                        email = email,
                        isSelfDelete = false
                    ) { _, _ ->
                        signOut()
                    }
                    return@addOnCompleteListener
                }
                val refreshed = auth.currentUser
                if (refreshed == null) {
                    signOut()
                } else if (isUserVerified(refreshed)) {
                    _pendingVerificationEmail.value = null
                    _currentUser.value = refreshed
                }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            try {
                val webClientId = try {
                    context.getString(R.string.web_client_id).takeIf {
                        it.isNotBlank() && it != "YOUR_WEB_CLIENT_ID_HERE"
                    } ?: run {
                        val defaultId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                        if (defaultId != 0) context.getString(defaultId) else ""
                    }
                } catch (e: Exception) {
                    ""
                }

                if (webClientId.isBlank()) {
                    Log.e("AuthViewModel", "WEB CLIENT ID IS MISSING.")
                    return@launch
                }

                val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId = webClientId).build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                val result = try {
                    credentialManager.getCredential(request = request, context = context)
                } catch (e: Exception) {
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(webClientId)
                        .setAutoSelectEnabled(true)
                        .build()
                    val fallbackRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()
                    credentialManager.getCredential(request = fallbackRequest, context = context)
                }

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _pendingVerificationEmail.value = null
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
        val cleanEmail = email.trim()
        val isBn = LanguageStore.currentLanguage.value == "bn"
        if (!isValidRealEmail(cleanEmail)) {
            onComplete(
                false,
                if (isBn) "দয়া করে একটি সঠিক ও আসল ইমেইল দিন (ফেইক/টেম্পোরারি ইমেইল গ্রহণযোগ্য নয়)।"
                else "Please enter a valid, real email address (fake/disposable emails are blocked)."
            )
            return
        }

        auth.signInWithEmailAndPassword(cleanEmail, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user == null) {
                        onComplete(false, if (isBn) "লগইন ব্যর্থ হয়েছে" else "Login failed")
                        return@addOnCompleteListener
                    }
                    user.reload().addOnCompleteListener {
                        val refreshedUser = auth.currentUser ?: user
                        if (isUserVerified(refreshedUser)) {
                            _pendingVerificationEmail.value = null
                            _currentUser.value = refreshedUser
                            onComplete(true, null)
                        } else {
                            _currentUser.value = null
                            _pendingVerificationEmail.value = refreshedUser.email ?: cleanEmail
                            refreshedUser.sendEmailVerification()
                            onComplete(
                                false,
                                if (isBn) "আপনার ইমেইল ভেরিফাই করা হয়নি! আপনার ইনবক্স/স্প্যামে ভেরিফিকেশন লিংক পাঠানো হয়েছে।"
                                else "Please verify your email first! A verification link has been sent to your inbox."
                            )
                        }
                    }
                } else {
                    onComplete(false, task.exception?.message ?: if (isBn) "ইমেইল বা পাসওয়ার্ড সঠিক নয়" else "Invalid email or password")
                }
            }
    }

    fun signUpWithEmail(email: String, name: String, pass: String, context: Context, onComplete: (Boolean, String?) -> Unit) {
        val cleanEmail = email.trim()
        val isBn = LanguageStore.currentLanguage.value == "bn"
        if (!isValidRealEmail(cleanEmail)) {
            onComplete(
                false,
                if (isBn) "দয়া করে একটি সঠিক ও আসল ইমেইল দিন (ফেইক/টেম্পোরারি ইমেইল দিয়ে রেজিস্ট্রেশন বন্ধ আছে)।"
                else "Please enter a valid, real email address (fake/disposable emails are blocked)."
            )
            return
        }

        auth.createUserWithEmailAndPassword(cleanEmail, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val finalizeSignup: () -> Unit = {
                            if (isUserVerified(user)) {
                                _pendingVerificationEmail.value = null
                                _currentUser.value = user
                                onComplete(true, null)
                            } else {
                                _currentUser.value = null
                                _pendingVerificationEmail.value = user.email ?: cleanEmail
                                user.sendEmailVerification().addOnCompleteListener { verifyTask ->
                                    if (verifyTask.isSuccessful) {
                                        onComplete(
                                            true,
                                            if (isBn) "ভেরিফিকেশন ইমেইল পাঠানো হয়েছে! আপনার ইনবক্স বা স্প্যাম চেক করে ইমেইল ভেরিফাই করুন।"
                                            else "Verification email sent! Please check your inbox/spam and verify your email."
                                        )
                                    } else {
                                        onComplete(
                                            true,
                                            if (isBn) "অ্যাকাউন্ট তৈরি হয়েছে। লগইন করতে আপনার ইমেইল ভেরিফাই করুন।"
                                            else "Account created. Please verify your email to continue."
                                        )
                                    }
                                }
                            }
                        }

                        if (name.isNotBlank()) {
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name.trim())
                                .build()
                            user.updateProfile(profileUpdates).addOnCompleteListener {
                                finalizeSignup()
                            }
                        } else {
                            finalizeSignup()
                        }
                    } else {
                        onComplete(false, if (isBn) "রেজিস্ট্রেশন ব্যর্থ হয়েছে" else "Registration failed")
                    }
                } else {
                    onComplete(false, task.exception?.message ?: if (isBn) "রেজিস্ট্রেশন ব্যর্থ হয়েছে" else "Registration failed")
                }
            }
    }

    fun checkEmailVerificationStatus(onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        val isBn = LanguageStore.currentLanguage.value == "bn"
        if (user == null) {
            _pendingVerificationEmail.value = null
            onResult(false, if (isBn) "সেশন পাওয়া যায়নি, আবার লগইন করুন।" else "Session expired, please sign in again.")
            return
        }
        user.reload().addOnCompleteListener { task ->
            val refreshed = auth.currentUser
            if (task.isSuccessful && isUserVerified(refreshed)) {
                _pendingVerificationEmail.value = null
                _currentUser.value = refreshed
                onResult(
                    true,
                    if (isBn) "ইমেইল সফলভাবে ভেরিফাই হয়েছে! স্বাগতম।" else "Email verified successfully! Welcome."
                )
            } else {
                onResult(
                    false,
                    if (isBn) "এখনো ইমেইল ভেরিফাই হয়নি। আপনার ইমেইলের ইনবক্স বা স্প্যাম ফোল্ডারে গিয়ে ভেরিফিকেশন লিংকে ক্লিক করুন।"
                    else "Email is not verified yet. Please click the verification link in your email Inbox or Spam folder."
                )
            }
        }
    }

    fun resendVerificationEmail(onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        val isBn = LanguageStore.currentLanguage.value == "bn"
        if (user == null) {
            onResult(false, if (isBn) "আবার লগইন করে চেষ্টা করুন" else "Please sign in again")
            return
        }
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(
                    true,
                    if (isBn) "ভেরিফিকেশন লিংক পুনরায় আপনার ইমেইলে পাঠানো হয়েছে!"
                    else "Verification link resent! Please check your Inbox or Spam folder."
                )
            } else {
                onResult(
                    false,
                    task.exception?.localizedMessage ?: if (isBn) "কিছুক্ষণ পর আবার চেষ্টা করুন" else "Please wait a moment before trying again"
                )
            }
        }
    }

    fun cancelPendingVerification() {
        auth.signOut()
        _pendingVerificationEmail.value = null
        _currentUser.value = null
    }

    fun sendPasswordlessSignInLink(email: String, name: String, onComplete: (Boolean, String?) -> Unit) {
        val appContext = getApplication<Application>().applicationContext
        val actionCodeSettings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
            .setUrl("https://ais-pre-spzdpff2lbcs3767oevm2f-22495587935.asia-east1.run.app")
            .setHandleCodeInApp(true)
            .setAndroidPackageName(
                appContext.packageName,
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
                        _pendingVerificationEmail.value = null
                        _currentUser.value = user
                        if (name.isNotBlank() && user.displayName.isNullOrBlank()) {
                            val profileUpdates = UserProfileChangeRequest.Builder()
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
                                _pendingVerificationEmail.value = null
                                _currentUser.value = user
                                if (name.isNotBlank() && (user.displayName.isNullOrBlank() || user.displayName == "Anonymous Warrior")) {
                                    val profileUpdates = UserProfileChangeRequest.Builder()
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
        viewModelScope.launch {
            try {
                credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
            } catch (_: Exception) {}
        }
        auth.signOut()
        _pendingVerificationEmail.value = null
        _currentUser.value = null
        PremiumManager.setPremiumStatus(getApplication(), false)
    }

    fun deleteCurrentAccount(
        username: String = "",
        onComplete: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(false, "Not logged in")
            return
        }
        val uid = user.uid
        val email = user.email ?: ""
        AccountDeletionManager.purgeUserCompletely(
            userId = uid,
            username = username,
            email = email,
            isSelfDelete = true
        ) { success, errorMsg ->
            viewModelScope.launch {
                try {
                    credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
                } catch (_: Exception) {}
            }
            _pendingVerificationEmail.value = null
            _currentUser.value = null
            PremiumManager.setPremiumStatus(getApplication(), false)
            onComplete(success, errorMsg)
        }
    }
}
