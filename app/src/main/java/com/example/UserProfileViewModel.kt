package com.example

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val bio: String = "",
    val profilePicUrl: String = "",
    val birthDate: String = "",
    val isPremium: Boolean = false,
    val badge: String = "",
    val joinDate: Long = System.currentTimeMillis()
)

class UserProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _isUsernameAvailable = MutableStateFlow<Boolean?>(null)
    val isUsernameAvailable = _isUsernameAvailable.asStateFlow()

    fun uploadProfilePicture(uri: Uri, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: return
        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("profile_pics/${user.uid}/$filename")
        
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                db.collection("users").document(user.uid).set(mapOf("profilePicUrl" to downloadUrl.toString()), SetOptions.merge())
                    .addOnSuccessListener {
                        onComplete(true, downloadUrl.toString())
                    }
                    .addOnFailureListener {
                        onComplete(false, null)
                    }
            }.addOnFailureListener {
                onComplete(false, null)
            }
        }.addOnFailureListener {
            onComplete(false, null)
        }
    }

    fun fetchProfile(userId: String) {
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val profile = snapshot.toObject(UserProfile::class.java)?.copy(userId = snapshot.id)
                    // If it's the current user, update the state
                    if (userId == auth.currentUser?.uid) {
                        _userProfile.value = profile
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
                _isUsernameAvailable.value = !doc.exists()
            }
    }

    fun setUsernameAndProfile(username: String, bio: String, name: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: return
        val cleanUsername = username.lowercase().trim()
        
        db.runTransaction { transaction ->
            val usernameRef = db.collection("usernames").document(cleanUsername)
            val usernameDoc = transaction.get(usernameRef)
            
            if (usernameDoc.exists()) {
                throw Exception("Username is already taken")
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
}
