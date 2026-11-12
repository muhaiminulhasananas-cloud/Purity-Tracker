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
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import android.media.MediaMetadataRetriever
import android.widget.Toast

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
    val reaction: String = "",
    @get:com.google.firebase.firestore.PropertyName("isRead")
    @set:com.google.firebase.firestore.PropertyName("isRead")
    var isRead: Boolean = false
)

class SocialViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests = _friendRequests.asStateFlow()

    private val _friends = MutableStateFlow<List<String>>(emptyList())
    val friends = _friends.asStateFlow()

    private val _lastMessageTimestamps = MutableStateFlow<Map<String, Long>>(emptyMap())
    val lastMessageTimestamps = _lastMessageTimestamps.asStateFlow()

    val sortedFriends = combine(_friends, _lastMessageTimestamps) { friendsList, timestamps ->
        friendsList.sortedByDescending { friendId ->
            timestamps[friendId] ?: 0L
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _chatMessages = MutableStateFlow<Map<String, List<SocialMessage>>>(emptyMap())
    val chatMessages = _chatMessages.asStateFlow()
    
    private val _messages = MutableStateFlow<List<SocialMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _unreadCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadCounts = _unreadCounts.asStateFlow()

    private val _premiumUsers = MutableStateFlow<Set<String>>(emptySet())
    val premiumUsers = _premiumUsers.asStateFlow()
    private val _premiumUserEmails = MutableStateFlow<Set<String>>(emptySet())
    val premiumUserEmails = _premiumUserEmails.asStateFlow()

    private val _isUploadingMedia = MutableStateFlow(false)
    val isUploadingMedia = _isUploadingMedia.asStateFlow()

    private val _uploadProgressText = MutableStateFlow<String?>(null)
    val uploadProgressText = _uploadProgressText.asStateFlow()

    private val chatListeners = mutableMapOf<String, com.google.firebase.firestore.ListenerRegistration>()
    private val lastMsgListeners = mutableMapOf<String, com.google.firebase.firestore.ListenerRegistration>()

    init {
        listenForFriendRequests()
        listenForFriends()
        fetchPremiumUsers()
    }

    private fun fetchPremiumUsers() {
        db.collection("users")
            .whereEqualTo("isPremium", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val pUsers = snapshot.documents.map { it.id }.toSet()
                    val pEmails = snapshot.documents.mapNotNull { it.getString("email") }.toSet()
                    _premiumUsers.value = pUsers
                    _premiumUserEmails.value = pEmails
                }
            }
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
            
            lastMsgListeners[friendId]?.remove()
            lastMsgListeners.remove(friendId)
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

            val lastMsgListener = db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    val lastMsg = snapshot?.documents?.firstOrNull()?.toObject(SocialMessage::class.java)
                    val timestamp = lastMsg?.timestamp ?: 0L
                    _lastMessageTimestamps.value = _lastMessageTimestamps.value.toMutableMap().apply {
                        put(friendId, timestamp)
                    }
                }
            lastMsgListeners[friendId] = lastMsgListener
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

    fun cancelFriendRequest(toUserId: String) {
        val user = auth.currentUser ?: return
        val requestId = "${user.uid}_$toUserId"
        db.collection("friend_requests").document(requestId).delete()
    }

    fun unfriend(friendId: String) {
        val user = auth.currentUser ?: return
        db.runTransaction { transaction ->
            val userRef = db.collection("users").document(user.uid)
            val friendRef = db.collection("users").document(friendId)
            
            transaction.update(userRef, "friends", com.google.firebase.firestore.FieldValue.arrayRemove(friendId))
            transaction.update(friendRef, "friends", com.google.firebase.firestore.FieldValue.arrayRemove(user.uid))
            null
        }
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

    fun reactToMessage(friendId: String, messageId: String, reaction: String) {
        val user = auth.currentUser ?: return
        val chatId = if (user.uid < friendId) "${user.uid}_$friendId" else "${friendId}_${user.uid}"
        db.collection("chats").document(chatId).collection("messages").document(messageId)
            .update("reaction", reaction)
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
        _isUploadingMedia.value = true
        _uploadProgressText.value = if (isVideo) "ভিডিও আপলোড হচ্ছে..." else "HD ছবি পাঠানো হচ্ছে..."
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
        val fileName = "${user.uid}_${System.currentTimeMillis()}${if(isVideo) ".mp4" else ".jpg"}"
        val fileRef = storageRef.child("chat_media/$fileName")
        
        fileRef.putFile(uri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                _isUploadingMedia.value = false
                _uploadProgressText.value = null
                if (isVideo) {
                    sendMessage(friendId, messageText, videoUrl = downloadUrl.toString())
                } else {
                    sendMessage(friendId, messageText, imageUrl = downloadUrl.toString())
                }
            }
        }.addOnFailureListener { e ->
            Log.e("SocialViewModel", "Failed to upload chat media to Storage, attempting fallback", e)
            if (!isVideo) {
                // Base64 fallback for images
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val inputStream = com.google.firebase.FirebaseApp.getInstance().applicationContext.contentResolver.openInputStream(uri)
                        val originalBitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        if (originalBitmap != null) {
                            val scaled = scaleBitmapHd(originalBitmap, maxDimension = 1280)
                            val bos = ByteArrayOutputStream()
                            scaled.compress(Bitmap.CompressFormat.JPEG, 80, bos)
                            val bytes = bos.toByteArray()
                            val base64 = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                            withContext(Dispatchers.Main) {
                                _isUploadingMedia.value = false
                                _uploadProgressText.value = null
                                sendMessage(friendId, messageText, imageUrl = base64)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                _isUploadingMedia.value = false
                                _uploadProgressText.value = null
                            }
                        }
                    } catch (ex: Exception) {
                        Log.e("SocialViewModel", "Fallback upload failed", ex)
                        withContext(Dispatchers.Main) {
                            _isUploadingMedia.value = false
                            _uploadProgressText.value = null
                        }
                    }
                }
            } else {
                _isUploadingMedia.value = false
                _uploadProgressText.value = null
            }
        }
    }

    fun sendHdImage(context: Context, friendId: String, uri: Uri, caption: String = "") {
        val user = auth.currentUser ?: return
        _isUploadingMedia.value = true
        _uploadProgressText.value = "ক্রিস্টাল ক্লিয়ার HD ছবি পাঠানো হচ্ছে..."

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap == null) {
                    withContext(Dispatchers.Main) {
                        _isUploadingMedia.value = false
                        _uploadProgressText.value = null
                    }
                    return@launch
                }

                // Preserve true crisp Ultra HD / 1080p+ original resolution up to 2560px with 94% high-fidelity JPEG quality
                val hdBitmap = scaleBitmapHd(originalBitmap, maxDimension = 2560)
                val tempFile = File(context.cacheDir, "hd_chat_${System.currentTimeMillis()}.jpg")
                val fos = FileOutputStream(tempFile)
                hdBitmap.compress(Bitmap.CompressFormat.JPEG, 94, fos)
                fos.flush()
                fos.close()

                val fileUri = Uri.fromFile(tempFile)
                val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                val fileName = "${user.uid}_${System.currentTimeMillis()}.jpg"
                val fileRef = storageRef.child("chat_media/$fileName")

                fileRef.putFile(fileUri).addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        _isUploadingMedia.value = false
                        _uploadProgressText.value = null
                        sendMessage(friendId, caption, imageUrl = downloadUrl.toString())
                        try { tempFile.delete() } catch (e: Exception) {}
                    }
                }.addOnFailureListener {
                    // Fallback to high-quality base64 if Firebase Storage has quota/permission issues
                    val bos = ByteArrayOutputStream()
                    val fallbackBitmap = scaleBitmapHd(hdBitmap, maxDimension = 1600)
                    fallbackBitmap.compress(Bitmap.CompressFormat.JPEG, 88, bos)
                    val base64 = "data:image/jpeg;base64," + Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
                    _isUploadingMedia.value = false
                    _uploadProgressText.value = null
                    sendMessage(friendId, caption, imageUrl = base64)
                    try { tempFile.delete() } catch (e: Exception) {}
                }
            } catch (e: Exception) {
                Log.e("SocialViewModel", "Error processing HD image", e)
                withContext(Dispatchers.Main) {
                    _isUploadingMedia.value = false
                    _uploadProgressText.value = null
                }
            }
        }
    }

    fun sendLiveCameraPhoto(context: Context, friendId: String, bitmap: Bitmap, caption: String = "") {
        val user = auth.currentUser ?: return
        _isUploadingMedia.value = true
        _uploadProgressText.value = "হাই কোয়ালিটি ক্যামেরা ছবি পাঠানো হচ্ছে..."

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val hdBitmap = scaleBitmapHd(bitmap, maxDimension = 2560)
                val tempFile = File(context.cacheDir, "camera_chat_${System.currentTimeMillis()}.jpg")
                val fos = FileOutputStream(tempFile)
                hdBitmap.compress(Bitmap.CompressFormat.JPEG, 94, fos)
                fos.flush()
                fos.close()

                val fileUri = Uri.fromFile(tempFile)
                val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                val fileName = "${user.uid}_${System.currentTimeMillis()}.jpg"
                val fileRef = storageRef.child("chat_media/$fileName")

                fileRef.putFile(fileUri).addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        _isUploadingMedia.value = false
                        _uploadProgressText.value = null
                        sendMessage(friendId, caption, imageUrl = downloadUrl.toString())
                        try { tempFile.delete() } catch (e: Exception) {}
                    }
                }.addOnFailureListener {
                    val bos = ByteArrayOutputStream()
                    val fallbackBitmap = scaleBitmapHd(hdBitmap, maxDimension = 1600)
                    fallbackBitmap.compress(Bitmap.CompressFormat.JPEG, 88, bos)
                    val base64 = "data:image/jpeg;base64," + Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
                    _isUploadingMedia.value = false
                    _uploadProgressText.value = null
                    sendMessage(friendId, caption, imageUrl = base64)
                    try { tempFile.delete() } catch (e: Exception) {}
                }
            } catch (e: Exception) {
                Log.e("SocialViewModel", "Error processing live camera photo", e)
                withContext(Dispatchers.Main) {
                    _isUploadingMedia.value = false
                    _uploadProgressText.value = null
                }
            }
        }
    }

    fun sendHdVideo(context: Context, friendId: String, uri: Uri, caption: String = "") {
        val user = auth.currentUser ?: return

        // 1. Verify Video Duration (Max 2 Minutes = 120,000 ms)
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()

            val durationMs = durationStr?.toLongOrNull() ?: 0L
            val maxDurationMs = 2 * 60 * 1000L // 2 minutes in milliseconds

            if (durationMs > maxDurationMs) {
                val isBn = LanguageStore.currentLanguage.value == "bn"
                Toast.makeText(
                    context,
                    if (isBn) "ভিডিওটি ২ মিনিটের চেয়ে বড়! সর্বোচ্চ ২ মিনিটের ভিডিও পাঠানো যাবে।"
                    else "Video exceeds 2 minutes! Maximum allowed duration is 2 minutes.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        } catch (e: Exception) {
            Log.w("SocialViewModel", "Could not retrieve video metadata, proceeding with upload", e)
        }

        _isUploadingMedia.value = true
        _uploadProgressText.value = "হাই কোয়ালিটি ভিডিও আপলোড হচ্ছে... অনুগ্রহ করে অপেক্ষা করুন"

        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
        val fileName = "${user.uid}_${System.currentTimeMillis()}.mp4"
        val fileRef = storageRef.child("chat_media/$fileName")

        fileRef.putFile(uri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                _isUploadingMedia.value = false
                _uploadProgressText.value = null
                sendMessage(friendId, caption, videoUrl = downloadUrl.toString())
            }
        }.addOnFailureListener { e ->
            Log.e("SocialViewModel", "Failed to upload video", e)
            _isUploadingMedia.value = false
            _uploadProgressText.value = null
            val isBn = LanguageStore.currentLanguage.value == "bn"
            Toast.makeText(
                context,
                if (isBn) "ভিডিও আপলোড ব্যর্থ হয়েছে! ইন্টারনেট সংযোগ বা ভিডিওর সাইজ চেক করুন।"
                else "Failed to upload video. Please check connection or video size.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun scaleBitmapHd(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }
        val ratio: Float = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (ratio > 1) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
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
