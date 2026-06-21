package com.example

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FriendRequest(
    val fromUserId: String = "",
    val toUserId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "pending" // pending, accepted, rejected
)

data class SocialMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "",
    val audioUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

class SocialViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests = _friendRequests.asStateFlow()

    private val _friends = MutableStateFlow<List<String>>(emptyList())
    val friends = _friends.asStateFlow()

    private val _chatMessages = MutableStateFlow<Map<String, List<SocialMessage>>>(emptyMap())
    val chatMessages = _chatMessages.asStateFlow()
    
    private val _messages = MutableStateFlow<List<SocialMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _unreadCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadCounts = _unreadCounts.asStateFlow()

    private val chatListeners = mutableMapOf<String, com.google.firebase.firestore.ListenerRegistration>()

    init {
        listenForFriendRequests()
        listenForFriends()
    }

    private fun listenForFriendRequests() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                db.collection("friend_requests")
                    .whereEqualTo("toUserId", user.uid)
                    .whereEqualTo("status", "pending")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val requests = snapshot.documents.mapNotNull { it.toObject(FriendRequest::class.java) }
                            _friendRequests.value = requests
                        }
                    }
            }
        }
    }

    private fun listenForFriends() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                db.collection("users").document(user.uid)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) return@addSnapshotListener
                        if (snapshot != null && snapshot.exists()) {
                            val friendsList = snapshot.get("friends") as? List<String> ?: emptyList()
                            _friends.value = friendsList
                            updateChatListeners(user.uid, friendsList)
                        }
                    }
            }
        }
    }

    private fun updateChatListeners(userId: String, friends: List<String>) {
        val currentFriends = chatListeners.keys.toSet()
        val newFriends = friends.toSet()

        // remove old
        (currentFriends - newFriends).forEach { friendId ->
            chatListeners[friendId]?.remove()
            chatListeners.remove(friendId)
        }

        // add new
        (newFriends - currentFriends).forEach { friendId ->
            val chatId = if (userId < friendId) "${userId}_$friendId" else "${friendId}_${userId}"
            val listener = db.collection("chats").document(chatId).collection("messages")
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    val count = snapshot?.documents?.size ?: 0
                    _unreadCounts.value = _unreadCounts.value.toMutableMap().apply {
                        put(friendId, count)
                    }
                }
            chatListeners[friendId] = listener
        }
    }

    fun markChatAsRead(friendId: String) {
        val user = auth.currentUser ?: return
        val chatId = if (user.uid < friendId) "${user.uid}_$friendId" else "${friendId}_${user.uid}"
        db.collection("chats").document(chatId).collection("messages")
            .whereEqualTo("receiverId", user.uid)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                db.runBatch { batch ->
                    snapshot.documents.forEach { doc ->
                        batch.update(doc.reference, "isRead", true)
                    }
                }
            }
    }

    fun sendFriendRequest(toUserId: String) {
        val user = auth.currentUser ?: return
        if (user.uid == toUserId) return
        
        val requestId = "${user.uid}_$toUserId"
        val request = FriendRequest(
            fromUserId = user.uid,
            toUserId = toUserId,
            status = "pending"
        )
        db.collection("friend_requests").document(requestId).set(request)
    }

    fun acceptFriendRequest(fromUserId: String) {
        val user = auth.currentUser ?: return
        val requestId = "${fromUserId}_${user.uid}"
        
        db.runTransaction { transaction ->
            val requestRef = db.collection("friend_requests").document(requestId)
            val userRef = db.collection("users").document(user.uid)
            val senderRef = db.collection("users").document(fromUserId)

            transaction.update(requestRef, "status", "accepted")
            
            // Add to both friends lists
            transaction.update(userRef, "friends", com.google.firebase.firestore.FieldValue.arrayUnion(fromUserId))
            transaction.update(senderRef, "friends", com.google.firebase.firestore.FieldValue.arrayUnion(user.uid))
            null
        }
    }

    fun rejectFriendRequest(fromUserId: String) {
        val user = auth.currentUser ?: return
        val requestId = "${fromUserId}_${user.uid}"
        db.collection("friend_requests").document(requestId).update("status", "rejected")
    }

    fun updateMessage(friendId: String, messageId: String, newText: String) {
        val user = auth.currentUser ?: return
        val chatId = if (user.uid < friendId) "${user.uid}_$friendId" else "${friendId}_${user.uid}"
        db.collection("chats").document(chatId).collection("messages").document(messageId)
            .update("text", newText)
    }

    fun deleteMessage(friendId: String, messageId: String) {
        val user = auth.currentUser ?: return
        val chatId = if (user.uid < friendId) "${user.uid}_$friendId" else "${friendId}_${user.uid}"
        db.collection("chats").document(chatId).collection("messages").document(messageId)
            .delete()
    }

    fun listenForMessages(friendId: String) {
        val user = auth.currentUser ?: return
        val chatId = if (user.uid < friendId) "${user.uid}_$friendId" else "${friendId}_${user.uid}"
        
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { it.toObject(SocialMessage::class.java)?.copy(id = it.id) }
                    _messages.value = msgs
                    _chatMessages.value = _chatMessages.value.toMutableMap().apply {
                        put(friendId, msgs)
                    }
                }
            }
    }

    fun sendMessage(friendId: String, messageText: String, imageUrl: String = "", videoUrl: String = "", audioUrl: String = "") {
        val user = auth.currentUser ?: return
        if (messageText.isBlank() && imageUrl.isBlank() && videoUrl.isBlank() && audioUrl.isBlank()) return
        
        val chatId = if (user.uid < friendId) "${user.uid}_$friendId" else "${friendId}_${user.uid}"
        val msg = SocialMessage(
            senderId = user.uid,
            receiverId = friendId,
            text = messageText,
            imageUrl = imageUrl,
            videoUrl = videoUrl,
            audioUrl = audioUrl
        )
        db.collection("chats").document(chatId).collection("messages").add(msg)
    }

    fun uploadMediaAndSendMessage(friendId: String, uri: android.net.Uri, isVideo: Boolean, messageText: String) {
        val user = auth.currentUser ?: return
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
        val fileName = "${user.uid}_${System.currentTimeMillis()}${if(isVideo) ".mp4" else ".jpg"}"
        val fileRef = storageRef.child("chat_media/$fileName")
        
        fileRef.putFile(uri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                if (isVideo) {
                    sendMessage(friendId, messageText, videoUrl = downloadUrl.toString())
                } else {
                    sendMessage(friendId, messageText, imageUrl = downloadUrl.toString())
                }
            }
        }.addOnFailureListener { e ->
            Log.e("SocialViewModel", "Failed to upload chat media", e)
        }
    }

    fun uploadVoiceAndSendMessage(friendId: String, uri: android.net.Uri) {
        val user = auth.currentUser ?: return
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
        val fileName = "${user.uid}_${System.currentTimeMillis()}.3gp"
        val fileRef = storageRef.child("chat_media/$fileName")
        
        fileRef.putFile(uri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                sendMessage(friendId, "", audioUrl = downloadUrl.toString())
            }
        }.addOnFailureListener { e ->
            Log.e("SocialViewModel", "Failed to upload voice message", e)
        }
    }
}
