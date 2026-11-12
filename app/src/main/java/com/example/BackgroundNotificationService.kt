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
    private var isFriendsListenerActive: ListenerRegistration? = null
    private val directChatListeners = mutableMapOf<String, ListenerRegistration>()
    private var paymentRequestListener: ListenerRegistration? = null
    private var incomingCallListener: ListenerRegistration? = null
    private var streakSyncJob: kotlinx.coroutines.Job? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    private var initialLoadCompleted = false

    override fun onBind(intent: Intent?): IBinder? = null

    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setupNotificationChannels()
        VoiceCallManager.initialize(this)
        
        if (authStateListener == null) {
            authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user != null) {
                    startListeningToChallenges()
                    startListeningToFriends(user.uid)
                    startListeningToIncomingCalls(user.uid)
                    if (user.email == "muhaiminulhasananas@gmail.com") {
                        startListeningToPayments()
                    } else {
                        paymentRequestListener?.remove()
                        paymentRequestListener = null
                    }
                    startPeriodicStreakSync(user.uid)
                } else {
                    streakSyncJob?.cancel()
                    streakSyncJob = null
                    incomingCallListener?.remove()
                    incomingCallListener = null
                    isJoinedChallengesListenerActive?.remove()
                    isJoinedChallengesListenerActive = null
                    isFriendsListenerActive?.remove()
                    isFriendsListenerActive = null
                    directChatListeners.values.forEach { it.remove() }
                    directChatListeners.clear()
                    paymentRequestListener?.remove()
                    paymentRequestListener = null
                    listeners.forEach { it.remove() }
                    listeners.clear()
                }
            }
            auth.addAuthStateListener(authStateListener!!)
        }

        return START_STICKY
    }

    private fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val callChannel = NotificationChannel(
                "voice_call_channel",
                "Voice & Video Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming voice and video call notifications"
                setSound(null, null) // Handled by CallSoundManager
                enableVibration(true)
            }
            nm.createNotificationChannel(callChannel)

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

            val paymentChannel = NotificationChannel(
                "payment_notifications",
                "Payment Approvals",
                NotificationManager.IMPORTANCE_HIGH
            )
            paymentChannel.description = "Payment request alerts for Admins"
            nm.createNotificationChannel(paymentChannel)
        }
    }

    private fun startListeningToIncomingCalls(userId: String) {
        incomingCallListener?.remove()
        val serviceStartTime = System.currentTimeMillis() - 10000

        incomingCallListener = db.collection("voice_calls")
            .whereEqualTo("receiverId", userId)
            .whereIn("status", listOf("calling", "ringing"))
            .limit(1)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null || snapshots.isEmpty) {
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.cancel(77771)
                    if (VoiceCallManager.callSession.value?.role == CallRole.RECEIVER &&
                        VoiceCallManager.callSession.value?.status == CallStatus.RINGING
                    ) {
                        VoiceCallManager.endCall()
                    }
                    return@addSnapshotListener
                }

                val doc = snapshots.documents.firstOrNull() ?: return@addSnapshotListener
                val createdAt = doc.getLong("createdAt") ?: 0L
                if (createdAt < serviceStartTime) return@addSnapshotListener

                val callId = doc.getString("callId") ?: doc.id
                val callerId = doc.getString("callerId") ?: ""
                val callerName = doc.getString("callerName") ?: "Friend"
                val callerPhoto = doc.getString("callerPhoto") ?: ""
                val receiverName = doc.getString("receiverName") ?: "Me"
                val receiverPhoto = doc.getString("receiverPhoto") ?: ""
                val isVideo = doc.getBoolean("isVideo") ?: false

                // If not already handling in VoiceCallManager
                if (VoiceCallManager.callSession.value == null || VoiceCallManager.callSession.value?.status == CallStatus.IDLE) {
                    val session = ActiveCallSession(
                        callId = callId,
                        callerId = callerId,
                        callerName = callerName,
                        callerPhoto = callerPhoto,
                        receiverId = userId,
                        receiverName = receiverName,
                        receiverPhoto = receiverPhoto,
                        isVideo = isVideo,
                        role = CallRole.RECEIVER,
                        status = CallStatus.RINGING
                    )
                    showIncomingCallNotification(session)
                }
            }
    }

    private fun showIncomingCallNotification(session: ActiveCallSession) {
        // Full screen Intent to pop up IncomingCallActivity
        val fullScreenIntent = Intent(this, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("callId", session.callId)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            session.callId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Accept Action Intent
        val acceptIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = "ACTION_ACCEPT_CALL"
            putExtra("callId", session.callId)
        }
        val acceptPendingIntent = PendingIntent.getBroadcast(
            this,
            (session.callId + "_accept").hashCode(),
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Decline Action Intent
        val declineIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = "ACTION_DECLINE_CALL"
            putExtra("callId", session.callId)
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            this,
            (session.callId + "_decline").hashCode(),
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val callTypeTitle = if (session.isVideo) "📹 ইনকামিং ভিডিও কল..." else "🎙️ ইনকামিং ভয়েস কল..."

        val notification = NotificationCompat.Builder(this, "voice_call_channel")
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setColor(0xFF0284C7.toInt())
            .setLargeIcon(NotificationHelper.getAppLogoBitmap(this))
            .setContentTitle(session.callerName.ifBlank { "বন্ধু" })
            .setContentText(callTypeTitle)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(R.drawable.ic_notification_logo, "ধরুন (Accept)", acceptPendingIntent)
            .addAction(R.drawable.ic_notification_logo, "কাটুন (Decline)", declinePendingIntent)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(77771, notification)

        // Also trigger full screen activity immediately
        try {
            startActivity(fullScreenIntent)
        } catch (e: Exception) {
            android.util.Log.w("BackgroundNotif", "Could not launch direct activity: ${e.message}")
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
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setColor(0xFF0284C7.toInt())
            .setLargeIcon(NotificationHelper.getAppLogoBitmap(this))
            .setContentTitle("New message from $senderName")
            .setContentText("$senderName: $messageText")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify("chat", challengeId.hashCode(), notif)
    }

    private fun startListeningToPayments() {
        val startTime = System.currentTimeMillis()
        paymentRequestListener?.remove()
        paymentRequestListener = db.collection("payment_requests")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                for (change in snapshot.documentChanges) {
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val timestamp = change.document.getLong("timestamp") ?: 0L
                        if (timestamp > startTime) {
                            val userName = change.document.getString("userName") ?: "ব্যবহারকারী"
                            val amount = change.document.getString("amount") ?: ""
                            val method = change.document.getString("method") ?: "বিকাশ/নগদ"
                            showPaymentNotification(userName, amount, method)
                        }
                    }
                }
            }
    }

    private fun showPaymentNotification(userName: String, amount: String, method: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_tab", "profile")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 99912, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, "payment_notifications")
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setColor(0xFF0284C7.toInt())
            .setLargeIcon(NotificationHelper.getAppLogoBitmap(this))
            .setContentTitle("নতুন পেমেন্ট রিকোয়েস্ট! ৳")
            .setContentText("$userName $amount পাঠিয়েছেন ($method-এ)")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify("payment_approval", 99912, notif)
    }

    private fun startListeningToFriends(userId: String) {
        isFriendsListenerActive?.remove()
        isFriendsListenerActive = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val friendsList = snapshot.get("friends") as? List<String> ?: emptyList()
                updateDirectMessageListeners(userId, friendsList)
            }
    }

    private fun updateDirectMessageListeners(userId: String, friends: List<String>) {
        val currentListeners = directChatListeners.keys.toSet()
        val newFriends = friends.toSet()

        // remove old
        (currentListeners - newFriends).forEach { friendId ->
            directChatListeners[friendId]?.remove()
            directChatListeners.remove(friendId)
        }

        // add new
        (newFriends - currentListeners).forEach { friendId ->
            val chatId = if (userId < friendId) "${userId}_$friendId" else "${friendId}_${userId}"
            
            var friendName = "Friend"
            db.collection("users").document(friendId).get().addOnSuccessListener { userDoc ->
                if (userDoc != null && userDoc.exists()) {
                    friendName = userDoc.getString("name") ?: userDoc.getString("userName") ?: "Friend"
                }
            }

            val startTime = System.currentTimeMillis()

            val ref = db.collection("chats").document(chatId).collection("messages")
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("isRead", false)

            val listener = ref.addSnapshotListener { snapshot, err ->
                if (err != null || snapshot == null) return@addSnapshotListener
                
                for (change in snapshot.documentChanges) {
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val timestamp = change.document.getLong("timestamp") ?: 0L
                        if (timestamp > startTime) {
                            val msgText = change.document.getString("text") ?: "Sent an attachment"
                            val senderId = change.document.getString("senderId") ?: ""
                            if (senderId == friendId) {
                                showDirectMessageNotification(friendId, friendName, msgText)
                            }
                        }
                    }
                }
            }
            directChatListeners[friendId] = listener
        }
    }

    private fun showDirectMessageNotification(friendId: String, senderName: String, messageText: String) = scope.launch(Dispatchers.Main) {
        val intent = Intent(this@BackgroundNotificationService, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_direct_chat", friendId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this@BackgroundNotificationService, friendId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this@BackgroundNotificationService, "chat_notifications")
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setColor(0xFF0284C7.toInt())
            .setLargeIcon(NotificationHelper.getAppLogoBitmap(this@BackgroundNotificationService))
            .setContentTitle("New message from $senderName")
            .setContentText(messageText)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify("direct_chat", friendId.hashCode(), notif)
    }

    private fun startPeriodicStreakSync(userId: String) {
        streakSyncJob?.cancel()
        streakSyncJob = scope.launch {
            while (true) {
                try {
                    val prefs = getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
                    val startTime = prefs.getLong("start_time", 0L)
                    if (startTime > 0L) {
                        val now = System.currentTimeMillis()
                        val streakMs = now - startTime
                        if (streakMs > 0L) {
                            db.collection("users").document(userId).update(
                                "streakMs", streakMs,
                                "timestamp", now
                            )
                        }
                    }
                } catch (e: Exception) {}
                kotlinx.coroutines.delay(1800000L) // runs every 30 minutes in background
            }
        }
    }

    override fun onDestroy() {
        isJoinedChallengesListenerActive?.remove()
        isFriendsListenerActive?.remove()
        directChatListeners.values.forEach { it.remove() }
        directChatListeners.clear()
        paymentRequestListener?.remove()
        listeners.forEach { it.remove() }
        job.cancel()
        super.onDestroy()
    }
}
