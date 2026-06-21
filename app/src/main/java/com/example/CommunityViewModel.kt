package com.example

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

data class Post(
    val id: String = "",
    val name: String = "",
    val streak: String = "",
    val badge: String = "",
    val content: String = "",
    val claps: Int = 0,
    val likedBy: List<String> = emptyList(),
    val commentsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "",
    val userEmail: String = "",
    val isPremium: Boolean = false,
    val imageUrl: String = "",
    val videoUrl: String = ""
)

data class Comment(
    val id: String = "",
    val postId: String = "",
    val name: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userEmail: String = "",
    val isPremium: Boolean = false
)

data class LeaderboardUser(
    val id: String = "",
    val name: String = "",
    val streakMs: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isPremium: Boolean = false,
    val badge: String = ""
)

data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val durationDays: Int = 10,
    val participantCount: Int = 0,
    val participants: List<String> = emptyList(),
    val creatorName: String = "",
    val creatorEmail: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ChallengeMessage(
    val id: String = "",
    val challengeId: String = "",
    val senderName: String = "",
    val senderEmail: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val voiceUrl: String = "",
    val voiceDurationSec: Int = 0,
    val isPremium: Boolean = false
)

class CommunityViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _leaders = MutableStateFlow<List<LeaderboardUser>>(emptyList())
    val leaders = _leaders.asStateFlow()
    
    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val comments = _comments.asStateFlow()

    private val _uploadProgress = MutableStateFlow<Float?>(null)
    val uploadProgress = _uploadProgress.asStateFlow()

    private val _challenges = MutableStateFlow<List<Challenge>>(emptyList())
    val challenges = _challenges.asStateFlow()

    private val _challengeMessages = MutableStateFlow<Map<String, List<ChallengeMessage>>>(emptyMap())
    val challengeMessages = _challengeMessages.asStateFlow()
    
    private val _premiumUsers = MutableStateFlow<Set<String>>(emptySet())
    val premiumUsers = _premiumUsers.asStateFlow()
    private val _premiumUserEmails = MutableStateFlow<Set<String>>(emptySet())
    val premiumUserEmails = _premiumUserEmails.asStateFlow()

    init {
        fetchPosts()
        fetchLeaderboard()
        fetchChallenges()
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

    fun refreshPosts() {
        fetchPosts()
    }

    private fun fetchPosts() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("CommunityViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val postList = snapshot.documents.mapNotNull { 
                        it.toObject(Post::class.java)?.copy(id = it.id, isPremium = it.getBoolean("isPremium") ?: false) 
                    }
                    _posts.value = postList
                }
            }
    }

    private fun fetchLeaderboard() {
        db.collection("users")
            .orderBy("streakMs", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("CommunityViewModel", "Leaderboard Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val leaderList = snapshot.documents.mapNotNull {
                        it.toObject(LeaderboardUser::class.java)?.copy(id = it.id, isPremium = it.getBoolean("isPremium") ?: false)
                    }
                    _leaders.value = leaderList
                }
            }
    }

    private fun fetchChallenges() {
        db.collection("challenges")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("CommunityViewModel", "Challenges listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    var list = snapshot.documents.mapNotNull {
                        it.toObject(Challenge::class.java)?.copy(id = it.id)
                    }
                    // Auto-seed some helpful community challenges if empty
                    if (list.isEmpty()) {
                        seedDefaultChallenges()
                    }
                    _challenges.value = list
                }
            }
    }

    private fun seedDefaultChallenges() {
        val defaults = listOf(
            Challenge(
                title = "১০০ দিনের নো-ফ্যাপ চ্যালেঞ্জ 🏆",
                description = "আসক্তিমুক্ত পবিত্র জীবন গঠনের শ্রেষ্ঠ যুদ্ধ। আমাদের সাথে যোগ দিয়ে নিজেকে পরিবর্তন করুন!",
                durationDays = 100,
                participantCount = 0,
                participants = emptyList(),
                creatorName = "System",
                creatorEmail = "system",
                timestamp = System.currentTimeMillis() - 1000
            ),
            Challenge(
                title = "৩০ দিনের রুকইয়াহ্ ও আমল চ্যালেঞ্জ 🕋",
                description = "প্রতিদিন আল্লাহর যিকির, নামাজ এবং রুকইয়াহ্ শ্রবণের মাধ্যমে মন পবিত্র রাখুন।",
                durationDays = 30,
                participantCount = 0,
                participants = emptyList(),
                creatorName = "System",
                creatorEmail = "system",
                timestamp = System.currentTimeMillis() - 500
            ),
            Challenge(
                title = "১০ দিনের নো-সোশ্যাল মিডিয়া চ্যালেঞ্জ 📵",
                description = "১০ দিন কোনো সোশ্যাল মিডিয়া স্ক্রোলিং না করে মানসিক শান্তি ফিরিয়ে আনুন।",
                durationDays = 10,
                participantCount = 0,
                participants = emptyList(),
                creatorName = "System",
                creatorEmail = "system",
                timestamp = System.currentTimeMillis()
            )
        )
        for (challenge in defaults) {
            db.collection("challenges").add(challenge)
        }
    }

    fun createChallenge(title: String, description: String, durationDays: Int) {
        val user = auth.currentUser ?: return
        val challenge = Challenge(
            title = title,
            description = description,
            durationDays = durationDays,
            participantCount = 1,
            participants = listOf(user.email ?: ""),
            creatorName = user.displayName ?: "Anonymous Warrior",
            creatorEmail = user.email ?: "",
            timestamp = System.currentTimeMillis()
        )
        db.collection("challenges").add(challenge)
    }

    fun joinChallenge(challengeId: String) {
        val user = auth.currentUser ?: return
        val email = user.email ?: ""
        val challengeRef = db.collection("challenges").document(challengeId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(challengeRef)
            val participants = snapshot.get("participants") as? List<*> ?: emptyList<Any>()
            val participantList = participants.mapNotNull { it as? String }.toMutableList()
            if (!participantList.contains(email)) {
                participantList.add(email)
                transaction.update(challengeRef, "participants", participantList)
                transaction.update(challengeRef, "participantCount", participantList.size)
            }
        }.addOnSuccessListener {
            Log.d("CommunityViewModel", "Successfully joined challenge")
        }
    }

    fun leaveChallenge(challengeId: String) {
        val user = auth.currentUser ?: return
        val email = user.email ?: ""
        val challengeRef = db.collection("challenges").document(challengeId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(challengeRef)
            val participants = snapshot.get("participants") as? List<*> ?: emptyList<Any>()
            val participantList = participants.mapNotNull { it as? String }.toMutableList()
            if (participantList.contains(email)) {
                participantList.remove(email)
                transaction.update(challengeRef, "participants", participantList)
                transaction.update(challengeRef, "participantCount", participantList.size)
            }
        }
    }

    fun fetchChallengeMessages(challengeId: String) {
        db.collection("challenges").document(challengeId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val msgList = snapshot.documents.mapNotNull {
                        it.toObject(ChallengeMessage::class.java)?.copy(id = it.id, challengeId = challengeId, isPremium = it.getBoolean("isPremium") ?: false)
                    }
                    _challengeMessages.value = _challengeMessages.value.toMutableMap().apply {
                        put(challengeId, msgList)
                    }
                }
            }
    }

    fun sendChallengeMessage(challengeId: String, text: String, voiceUrl: String = "", voiceDurationSec: Int = 0) {
        val user = auth.currentUser ?: return
        val msg = ChallengeMessage(
            challengeId = challengeId,
            senderName = user.displayName ?: "Anonymous Warrior",
            senderEmail = user.email ?: "",
            message = text,
            timestamp = System.currentTimeMillis(),
            voiceUrl = voiceUrl,
            voiceDurationSec = voiceDurationSec,
            isPremium = PremiumManager.isPremium.value
        )
        db.collection("challenges").document(challengeId).collection("messages").add(msg)
    }

    fun syncUserStreak(streakMs: Long, badgeName: String = "New Warrior", badgeImageUrl: String = "") {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
                val cloudStreakMs = doc.getLong("streakMs") ?: 0L
                val cloudTimestamp = doc.getLong("timestamp") ?: 0L
                val elapsed = System.currentTimeMillis() - cloudTimestamp
                val realCloudStreak = cloudStreakMs + elapsed
                
                // If cloud is better than local by more than 5 minutes, DO NOT overwrite it.
                // This prevents accidentally wiping the cloud streak when they just installed the app on a new device.
                if (realCloudStreak > streakMs + 300000) {
                     return@addOnSuccessListener
                }
                
                val cloudPremium = doc.getBoolean("isPremium") ?: false
                val isLocalPremium = PremiumManager.isPremium.value
                val finalPremium = cloudPremium || isLocalPremium
                
                val userRecord = mutableMapOf<String, Any>(
                    "name" to (user.displayName ?: "Anonymous Warrior"),
                    "email" to (user.email ?: ""),
                    "streakMs" to streakMs,
                    "timestamp" to System.currentTimeMillis(),
                    "isPremium" to finalPremium,
                    "badge" to badgeName
                )
                if (badgeImageUrl.isNotEmpty()) {
                    userRecord["profilePicUrl"] = badgeImageUrl
                }
                db.collection("users").document(user.uid).set(userRecord, com.google.firebase.firestore.SetOptions.merge())
            }
        }
    }
    
    fun uploadMediaAndAddPost(uri: android.net.Uri, isVideo: Boolean, content: String, streak: String, badge: String) {
        val user = auth.currentUser ?: return
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
        val fileName = "${user.uid}_${System.currentTimeMillis()}${if(isVideo) ".mp4" else ".jpg"}"
        val fileRef = storageRef.child("posts_media/$fileName")
        
        _uploadProgress.value = 0f
        
        fileRef.putFile(uri).addOnProgressListener { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
            _uploadProgress.value = progress.toFloat()
        }.addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                if (isVideo) {
                    addPost(content, streak, badge, imageUrl = "", videoUrl = downloadUrl.toString())
                } else {
                    addPost(content, streak, badge, imageUrl = downloadUrl.toString(), videoUrl = "")
                }
                _uploadProgress.value = null
            }
        }.addOnFailureListener { e ->
            Log.e("CommunityViewModel", "Failed to upload media", e)
            _uploadProgress.value = null
        }
    }

    fun addPost(content: String, streak: String, badge: String, imageUrl: String = "", videoUrl: String = "") {
        val user = auth.currentUser
        if (user != null) {
            val post = Post(
                name = user.displayName ?: "Anonymous Warrior",
                streak = streak,
                badge = badge,
                content = content,
                claps = 0,
                likedBy = emptyList(),
                commentsCount = 0,
                timestamp = System.currentTimeMillis(),
                userId = user.uid,
                userEmail = user.email ?: "",
                isPremium = PremiumManager.isPremium.value,
                imageUrl = imageUrl,
                videoUrl = videoUrl
            )
            db.collection("posts").add(post)
                .addOnSuccessListener {
                    Log.d("CommunityViewModel", "DocumentSnapshot written with ID: ${it.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("CommunityViewModel", "Error adding document", e)
                }
        }
    }
    
    fun toggleLike(postId: String, likedBy: List<String>) {
        val user = auth.currentUser ?: return
        val currentUserId = user.uid

        val postRef = db.collection("posts").document(postId)
        if (likedBy.contains(currentUserId)) {
            // Unlike
            postRef.update(
                "likedBy", FieldValue.arrayRemove(currentUserId),
                "claps", FieldValue.increment(-1)
            )
        } else {
            // Like
            postRef.update(
                "likedBy", FieldValue.arrayUnion(currentUserId),
                "claps", FieldValue.increment(1)
            )
        }
    }
    
    fun fetchComments(postId: String) {
        db.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val commentList = snapshot.documents.mapNotNull {
                        it.toObject(Comment::class.java)?.copy(id = it.id, postId = postId, isPremium = it.getBoolean("isPremium") ?: false)
                    }
                    _comments.value = _comments.value.toMutableMap().apply {
                        put(postId, commentList)
                    }
                }
            }
    }

    fun addComment(postId: String, content: String) {
        val user = auth.currentUser ?: return
        val comment = Comment(
            postId = postId,
            name = user.displayName ?: "Anonymous Warrior",
            content = content,
            timestamp = System.currentTimeMillis(),
            userEmail = user.email ?: "",
            isPremium = PremiumManager.isPremium.value
        )
        db.collection("posts").document(postId).collection("comments").add(comment)
        db.collection("posts").document(postId).update("commentsCount", FieldValue.increment(1))
    }
}

