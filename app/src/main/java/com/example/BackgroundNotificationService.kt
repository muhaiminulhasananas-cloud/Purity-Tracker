package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BackgroundNotificationService : Service() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val listeners = mutableListOf<ListenerRegistration>()
    private var isJoinedChallengesListenerActive : ListenerRegistration? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    private var initialLoadCompleted = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (auth.currentUser == null) {
            return START_NOT_STICKY
        }
        
        setupNotificationChannels()
        startListeningToChallenges()

        return START_STICKY
    }

    private fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val chatChannel = NotificationChannel(
                "chat_notifications",
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            )
            chatChannel.description = "Notifications for new messages in challenges"
            nm.createNotificationChannel(chatChannel)

            val prayerChannel = NotificationChannel(
                "prayer_notifications",
                "Prayer Times",
                NotificationManager.IMPORTANCE_HIGH
            )
            prayerChannel.description = "Prayer time alerts"
            nm.createNotificationChannel(prayerChannel)
        }
    }

    private fun startListeningToChallenges() {
        val userEmail = auth.currentUser?.email ?: return
        
        isJoinedChallengesListenerActive?.remove()
        isJoinedChallengesListenerActive = db.collection("challenges")
            .whereArrayContains("participants", userEmail)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                
                val currentChallengeIds = snapshot.documents.map { it.id }
                updateMessageListeners(currentChallengeIds)
            }
    }

    private var currentChallengeMap = mutableSetOf<String>()

    private fun updateMessageListeners(challengeIds: List<String>) {
        val newKeys = challengeIds.toSet()
        val added = newKeys - currentChallengeMap
        
        // We only add listeners to new ones to avoid resetting.
        // It's a bit hard to manage removals without mapping registration objects, 
        // but for simplicity, we just add missing ones. You wouldn't leave a challenge often.
        if (added.isNotEmpty()) {
            val userEmail = auth.currentUser?.email ?: return
            for (cid in added) {
                // Get the last message to avoid alerting old messages on startup
                val ref = db.collection("challenges").document(cid).collection("messages")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                
                // Track if we already loaded initially
                var isFirstLoad = true
                
                ref.addSnapshotListener { snapshot, e ->
                    if (e != null || snapshot == null) return@addSnapshotListener
                    
                    if (isFirstLoad) {
                        isFirstLoad = false
                        return@addSnapshotListener
                    }
                    
                    for (change in snapshot.documentChanges) {
                        if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            val msg = change.document.toObject(ChallengeMessage::class.java)
                            if (msg.senderEmail != userEmail && msg.senderEmail.isNotEmpty()) {
                                showChatNotification(cid, msg.senderName, msg.message)
                            }
                        }
                    }
                }.let {
                    listeners.add(it)
                }
            }
            currentChallengeMap.addAll(added)
        }
    }

    private fun showChatNotification(challengeId: String, senderName: String, messageText: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_challenge_chat", challengeId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, challengeId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, "chat_notifications")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("New message from $senderName")
            .setContentText("$senderName: $messageText")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify("chat", challengeId.hashCode(), notif)
    }

    override fun onDestroy() {
        isJoinedChallengesListenerActive?.remove()
        listeners.forEach { it.remove() }
        job.cancel()
        super.onDestroy()
    }
}
