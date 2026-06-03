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
    val userEmail: String = ""
)

data class Comment(
    val id: String = "",
    val postId: String = "",
    val name: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userEmail: String = ""
)

data class LeaderboardUser(
    val id: String = "",
    val name: String = "",
    val streakMs: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
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
    
    init {
        fetchPosts()
        fetchLeaderboard()
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
                        it.toObject(Post::class.java)?.copy(id = it.id) 
                    }
                    _posts.value = postList
                }
            }
    }

    private fun fetchLeaderboard() {
        db.collection("users")
            .orderBy("streakMs", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("CommunityViewModel", "Leaderboard Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val leaderList = snapshot.documents.mapNotNull {
                        it.toObject(LeaderboardUser::class.java)?.copy(id = it.id)
                    }
                    _leaders.value = leaderList
                }
            }
    }

    fun syncUserStreak(streakMs: Long) {
        val user = auth.currentUser
        if (user != null) {
            val userRecord = LeaderboardUser(
                name = user.displayName ?: "Anonymous Warrior",
                streakMs = streakMs,
                timestamp = System.currentTimeMillis()
            )
            db.collection("users").document(user.uid).set(userRecord)
        }
    }
    
    fun addPost(content: String, streak: String, badge: String) {
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
                userEmail = user.email ?: ""
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
                        it.toObject(Comment::class.java)?.copy(id = it.id, postId = postId)
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
            userEmail = user.email ?: ""
        )
        db.collection("posts").document(postId).collection("comments").add(comment)
        db.collection("posts").document(postId).update("commentsCount", FieldValue.increment(1))
    }
}

