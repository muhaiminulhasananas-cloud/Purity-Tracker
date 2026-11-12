package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val bio: String = "",
    val profilePicUrl: String = "",
    val coverPicUrl: String = "",
    val birthDate: String = "",
    val isPremium: Boolean = false,
    val badge: String = "",
    val joinDate: Long = System.currentTimeMillis(),
    val relationship: String = "",
    val location: String = "",
    val work: String = "",
    val education: String = "",
    val website: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L
)

class UserProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _isUsernameAvailable = MutableStateFlow<Boolean?>(null)
    val isUsernameAvailable = _isUsernameAvailable.asStateFlow()

    fun uploadProfilePicture(context: Context, uri: Uri, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: run {
            onComplete(false, "Not logged in")
            return
        }
        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("profile_pics/${user.uid}/$filename")

        viewModelScope.launch {
            // Read and compress image in background
            val bytes = withContext(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (originalBitmap != null) {
                        val maxDim = 1280
                        val ratio = Math.min(maxDim.toFloat() / originalBitmap.width, maxDim.toFloat() / originalBitmap.height)
                        val scaledBitmap = if (ratio < 1.0f) {
                            Bitmap.createScaledBitmap(
                                originalBitmap,
                                (originalBitmap.width * ratio).toInt(),
                                (originalBitmap.height * ratio).toInt(),
                                true
                            )
                        } else {
                            originalBitmap
                        }
                        val baos = ByteArrayOutputStream()
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 92, baos)
                        baos.toByteArray()
                    } else null
                } catch (e: Exception) {
                    null
                }
            }

            fun saveProfilePicToFirestore(finalUrl: String) {
                db.collection("users").document(user.uid).set(mapOf("profilePicUrl" to finalUrl), SetOptions.merge())
                    .addOnSuccessListener {
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setPhotoUri(Uri.parse(finalUrl))
                            .build()
                        user.updateProfile(profileUpdates)
                        _userProfile.value = _userProfile.value?.copy(profilePicUrl = finalUrl)

                        // Also instantly sync active call session if user is in a call
                        val activeSession = VoiceCallManager.callSession.value
                        if (activeSession != null) {
                            if (activeSession.callerId == user.uid) {
                                db.collection("voice_calls").document(activeSession.callId)
                                    .update("callerPhoto", finalUrl)
                            } else if (activeSession.receiverId == user.uid) {
                                db.collection("voice_calls").document(activeSession.callId)
                                    .update("receiverPhoto", finalUrl)
                            }
                        }

                        onComplete(true, finalUrl)
                    }
                    .addOnFailureListener {
                        onComplete(false, null)
                    }
            }

            if (bytes != null) {
                ref.putBytes(bytes).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveProfilePicToFirestore(downloadUrl.toString())
                    }.addOnFailureListener {
                        // Storage downloadUrl fallback to data URL
                        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        val dataUrl = "data:image/jpeg;base64,$base64"
                        saveProfilePicToFirestore(dataUrl)
                    }
                }.addOnFailureListener {
                    // Storage upload fallback to data URL
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    val dataUrl = "data:image/jpeg;base64,$base64"
                    saveProfilePicToFirestore(dataUrl)
                }
            } else {
                ref.putFile(uri).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveProfilePicToFirestore(downloadUrl.toString())
                    }.addOnFailureListener {
                        onComplete(false, null)
                    }
                }.addOnFailureListener {
                    onComplete(false, null)
                }
            }
        }
    }

    fun uploadCoverPicture(context: Context, uri: Uri, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: run {
            onComplete(false, "Not logged in")
            return
        }
        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("cover_pics/${user.uid}/$filename")

        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (originalBitmap != null) {
                        val maxDim = 800
                        val ratio = Math.min(maxDim.toFloat() / originalBitmap.width, maxDim.toFloat() / originalBitmap.height)
                        val scaledBitmap = if (ratio < 1.0f) {
                            Bitmap.createScaledBitmap(
                                originalBitmap,
                                (originalBitmap.width * ratio).toInt(),
                                (originalBitmap.height * ratio).toInt(),
                                true
                            )
                        } else {
                            originalBitmap
                        }
                        val baos = ByteArrayOutputStream()
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                        baos.toByteArray()
                    } else null
                } catch (e: Exception) {
                    null
                }
            }

            fun saveCoverPicToFirestore(finalUrl: String) {
                db.collection("users").document(user.uid).set(mapOf("coverPicUrl" to finalUrl), SetOptions.merge())
                    .addOnSuccessListener {
                        _userProfile.value = _userProfile.value?.copy(coverPicUrl = finalUrl)
                        onComplete(true, finalUrl)
                    }
                    .addOnFailureListener {
                        onComplete(false, null)
                    }
            }

            if (bytes != null) {
                ref.putBytes(bytes).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveCoverPicToFirestore(downloadUrl.toString())
                    }.addOnFailureListener {
                        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        val dataUrl = "data:image/jpeg;base64,$base64"
                        saveCoverPicToFirestore(dataUrl)
                    }
                }.addOnFailureListener {
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    val dataUrl = "data:image/jpeg;base64,$base64"
                    saveCoverPicToFirestore(dataUrl)
                }
            } else {
                ref.putFile(uri).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveCoverPicToFirestore(downloadUrl.toString())
                    }.addOnFailureListener {
                        onComplete(false, null)
                    }
                }.addOnFailureListener {
                    onComplete(false, null)
                }
            }
        }
    }

    fun fetchProfile(userId: String) {
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val profile = snapshot.toObject(UserProfile::class.java)?.copy(userId = snapshot.id)
                    if (profile != null) {
                        val authPhoto = if (userId == auth.currentUser?.uid) auth.currentUser?.photoUrl?.toString() ?: "" else ""
                        val finalPhoto = if (profile.profilePicUrl.startsWith("res://badge_")) {
                            // Automatically clean up previously overwritten badge from profilePicUrl in Firestore
                            db.collection("users").document(userId).update("profilePicUrl", authPhoto)
                            authPhoto
                        } else if (profile.profilePicUrl.isEmpty() && authPhoto.isNotEmpty()) {
                            authPhoto
                        } else {
                            profile.profilePicUrl
                        }
                        val cleanProfile = profile.copy(profilePicUrl = finalPhoto)
                        // If it's the current user, update the state
                        if (userId == auth.currentUser?.uid) {
                            _userProfile.value = cleanProfile
                        }
                    }
                }
            }
    }

    fun checkUsernameAvailability(username: String) {
        val cleanUsername = username.lowercase().trim()
        if (cleanUsername.length < 3) {
            _isUsernameAvailable.value = false
            return
        }
        db.collection("usernames").document(cleanUsername).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    _isUsernameAvailable.value = true
                } else {
                    val ownerId = doc.getString("userId")?.trim() ?: ""
                    if (ownerId.isBlank() || ownerId == auth.currentUser?.uid) {
                        if (ownerId.isBlank()) {
                            db.collection("usernames").document(cleanUsername).delete()
                        }
                        _isUsernameAvailable.value = true
                    } else {
                        // Verify the owner still exists in `users` collection
                        db.collection("users").document(ownerId).get()
                            .addOnSuccessListener { userDoc ->
                                if (!userDoc.exists() || userDoc.getBoolean("isDeleted") == true) {
                                    // Orphaned username from a deleted user -> purge it and mark available!
                                    db.collection("usernames").document(cleanUsername).delete()
                                    _isUsernameAvailable.value = true
                                } else {
                                    _isUsernameAvailable.value = false
                                }
                            }
                            .addOnFailureListener {
                                _isUsernameAvailable.value = false
                            }
                    }
                }
            }
    }

    fun setUsernameAndProfile(username: String, bio: String, name: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: return
        val cleanUsername = username.lowercase().trim()
        
        db.runTransaction { transaction ->
            val usernameRef = db.collection("usernames").document(cleanUsername)
            val usernameDoc = transaction.get(usernameRef)
            
            if (usernameDoc.exists()) {
                val existingOwner = usernameDoc.getString("userId")?.trim() ?: ""
                if (existingOwner.isNotBlank() && existingOwner != user.uid) {
                    val ownerDoc = transaction.get(db.collection("users").document(existingOwner))
                    if (ownerDoc.exists() && ownerDoc.getBoolean("isDeleted") != true) {
                        throw Exception("Username is already taken")
                    }
                }
            }
            
            val userRef = db.collection("users").document(user.uid)
            val profileUpdates = mapOf(
                "username" to cleanUsername,
                "bio" to bio,
                "name" to name,
                "email" to (user.email ?: "")
            )
            transaction.set(usernameRef, mapOf("userId" to user.uid))
            transaction.set(userRef, profileUpdates, SetOptions.merge())
            null
        }.addOnSuccessListener {
            onComplete(true, null)
        }.addOnFailureListener { e ->
            onComplete(false, e.message)
        }
    }

    fun updateProfile(bio: String, profilePicUrl: String, birthDate: String, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val updates = mapOf(
            "bio" to bio,
            "profilePicUrl" to profilePicUrl,
            "birthDate" to birthDate
        )
        db.collection("users").document(uid).set(updates, SetOptions.merge())
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateUsername(newUsername: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: run {
            onComplete(false, "User not logged in")
            return
        }
        val cleanNew = newUsername.lowercase().trim()
        if (cleanNew.length < 3) {
            onComplete(false, "Username must be at least 3 characters")
            return
        }
        val currentUsername = _userProfile.value?.username?.lowercase()?.trim() ?: ""
        if (cleanNew == currentUsername) {
            onComplete(true, null)
            return
        }

        db.runTransaction { transaction ->
            val newUsernameRef = db.collection("usernames").document(cleanNew)
            val newDoc = transaction.get(newUsernameRef)
            if (newDoc.exists()) {
                val existingOwner = newDoc.getString("userId")?.trim() ?: ""
                if (existingOwner.isNotBlank() && existingOwner != user.uid) {
                    val ownerDoc = transaction.get(db.collection("users").document(existingOwner))
                    if (ownerDoc.exists() && ownerDoc.getBoolean("isDeleted") != true) {
                        throw Exception("Username is already taken")
                    }
                }
            }

            // If user had an old username, remove the old mapping
            if (currentUsername.isNotEmpty() && currentUsername != cleanNew) {
                val oldUsernameRef = db.collection("usernames").document(currentUsername)
                transaction.delete(oldUsernameRef)
            }

            // Set new mapping
            transaction.set(newUsernameRef, mapOf("userId" to user.uid))

            // Update user document
            val userRef = db.collection("users").document(user.uid)
            transaction.set(userRef, mapOf("username" to cleanNew), SetOptions.merge())
            null
        }.addOnSuccessListener {
            _userProfile.value = _userProfile.value?.copy(username = cleanNew)
            onComplete(true, null)
        }.addOnFailureListener { e ->
            onComplete(false, e.message ?: "Failed to update username")
        }
    }

    fun updateDisplayName(newName: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: run {
            onComplete(false, "User not logged in")
            return
        }
        val cleanName = newName.trim()
        if (cleanName.isEmpty()) {
            onComplete(false, "Name cannot be empty")
            return
        }
        val userRef = db.collection("users").document(user.uid)
        userRef.set(mapOf("name" to cleanName), SetOptions.merge())
            .addOnSuccessListener {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(cleanName)
                    .build()
                user.updateProfile(profileUpdates)
                _userProfile.value = _userProfile.value?.copy(name = cleanName)
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    fun clearProfile() {
        _userProfile.value = null
        _isUsernameAvailable.value = null
    }
}
