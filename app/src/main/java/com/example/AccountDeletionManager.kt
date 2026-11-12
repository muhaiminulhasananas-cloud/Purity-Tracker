package com.example

import android.util.Log
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object AccountDeletionManager {
    private const val TAG = "AccountDeletionManager"
    const val ADMIN_EMAIL = "muhaiminulhasananas@gmail.com"

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

    fun isAdminEmail(email: String?): Boolean {
        return email?.trim()?.lowercase() == ADMIN_EMAIL
    }

    fun isUserVerified(user: FirebaseUser?): Boolean {
        if (user == null) return false
        val email = user.email?.trim()?.lowercase() ?: ""
        if (email == ADMIN_EMAIL) return true
        val isGoogleProvider = user.providerData.any { it.providerId == "google.com" }
        if (isGoogleProvider) return true
        return user.isEmailVerified
    }

    fun isValidRealEmail(rawEmail: String?): Boolean {
        val email = rawEmail?.trim()?.lowercase() ?: return false
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

    /**
     * Completely wipes a user's existence from Firestore (users, usernames, leaderboard,
     * posts, comments, friend_requests, friends lists, challenges, payment_requests, prayer data)
     * and optionally deletes their Firebase Authentication account if isSelfDelete == true.
     */
    fun purgeUserCompletely(
        userId: String,
        username: String = "",
        email: String = "",
        isSelfDelete: Boolean = false,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        if (userId.isBlank()) {
            onComplete(false, "Invalid user ID")
            return
        }

        if (isSelfDelete) {
            UserPresenceManager.isDeletingAccount = true
            UserPresenceManager.onAppBackground()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                purgeUserInternal(userId = userId, knownUsername = username, knownEmail = email)

                if (isSelfDelete) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null && currentUser.uid == userId) {
                        try {
                            currentUser.delete().await()
                        } catch (e: Exception) {
                            Log.w(TAG, "FirebaseAuth user.delete() notice: ${e.message}")
                        }
                    }
                    FirebaseAuth.getInstance().signOut()
                }

                withContext(Dispatchers.Main) {
                    if (isSelfDelete) {
                        UserPresenceManager.isDeletingAccount = false
                    }
                    onComplete(true, null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error purging user $userId", e)
                withContext(Dispatchers.Main) {
                    if (isSelfDelete) {
                        UserPresenceManager.isDeletingAccount = false
                    }
                    onComplete(false, e.localizedMessage ?: "Failed to delete account")
                }
            }
        }
    }

    suspend fun purgeUserInternal(
        userId: String,
        knownUsername: String = "",
        knownEmail: String = ""
    ) {
        val db = FirebaseFirestore.getInstance()

        // 1. Fetch user document first in case username or email wasn't passed in
        var resolvedUsername = knownUsername.trim().lowercase()
        var resolvedEmail = knownEmail.trim().lowercase()
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            if (userDoc.exists()) {
                if (resolvedUsername.isBlank()) {
                    resolvedUsername = userDoc.getString("username")?.trim()?.lowercase() ?: ""
                }
                if (resolvedEmail.isBlank()) {
                    resolvedEmail = userDoc.getString("email")?.trim()?.lowercase() ?: ""
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not read user doc before purge: ${e.message}")
        }

        // 2. Record tombstone in deleted_accounts so the account can never be resurrected
        try {
            db.collection("deleted_accounts").document(userId).set(
                mapOf(
                    "userId" to userId,
                    "username" to resolvedUsername,
                    "email" to resolvedEmail,
                    "deletedAt" to System.currentTimeMillis()
                )
            ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Could not write deleted_accounts tombstone: ${e.message}")
        }

        // 3. Delete username mappings from `usernames` collection
        if (resolvedUsername.isNotBlank()) {
            try {
                db.collection("usernames").document(resolvedUsername).delete().await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete username doc $resolvedUsername: ${e.message}")
            }
        }
        try {
            val usernameSnap = db.collection("usernames")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            for (doc in usernameSnap.documents) {
                try {
                    doc.reference.delete().await()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed querying usernames for $userId: ${e.message}")
        }

        // 4. Delete subcollections under `users/{userId}` (prayer_data, prayer_records, history)
        try {
            db.collection("users").document(userId)
                .collection("prayer_data").document("state")
                .delete().await()
        } catch (_: Exception) {}

        try {
            val prayerRecords = db.collection("users").document(userId)
                .collection("prayer_records").get().await()
            for (doc in prayerRecords.documents) {
                try { doc.reference.delete().await() } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        try {
            val historyDocs = db.collection("users").document(userId)
                .collection("history").get().await()
            for (doc in historyDocs.documents) {
                try { doc.reference.delete().await() } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        // 5. Remove userId from all other users' `friends` arrays
        try {
            val friendsSnap = db.collection("users")
                .whereArrayContains("friends", userId)
                .get()
                .await()
            for (doc in friendsSnap.documents) {
                try {
                    doc.reference.update("friends", FieldValue.arrayRemove(userId)).await()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed removing $userId from friends lists: ${e.message}")
        }

        // 6. Delete all friend_requests sent or received by userId
        try {
            val sentReqs = db.collection("friend_requests")
                .whereEqualTo("fromUserId", userId)
                .get()
                .await()
            for (doc in sentReqs.documents) {
                try { doc.reference.delete().await() } catch (_: Exception) {}
            }
            val recReqs = db.collection("friend_requests")
                .whereEqualTo("toUserId", userId)
                .get()
                .await()
            for (doc in recReqs.documents) {
                try { doc.reference.delete().await() } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed deleting friend requests for $userId: ${e.message}")
        }

        // 7. Delete all community posts by userId (and by email if present)
        try {
            val postsByUid = db.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            for (doc in postsByUid.documents) {
                try { doc.reference.delete().await() } catch (_: Exception) {}
            }
            if (resolvedEmail.isNotBlank()) {
                val postsByEmail = db.collection("posts")
                    .whereEqualTo("userEmail", resolvedEmail)
                    .get()
                    .await()
                for (doc in postsByEmail.documents) {
                    try { doc.reference.delete().await() } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed deleting posts for $userId: ${e.message}")
        }

        // 8. Remove from challenges participants if email is known
        if (resolvedEmail.isNotBlank()) {
            try {
                val challengesSnap = db.collection("challenges")
                    .whereArrayContains("participants", resolvedEmail)
                    .get()
                    .await()
                for (doc in challengesSnap.documents) {
                    try {
                        val currentList = (doc.get("participants") as? List<*>)?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()
                        if (currentList.remove(resolvedEmail)) {
                            doc.reference.update(
                                mapOf(
                                    "participants" to currentList,
                                    "participantCount" to currentList.size
                                )
                            ).await()
                        }
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }

        // 9. Delete payment_requests by userId
        try {
            val paySnap = db.collection("payment_requests")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            for (doc in paySnap.documents) {
                try { doc.reference.delete().await() } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        // 10. Finally, delete the `users/{userId}` document itself (removes from Leaderboard & Search)
        try {
            db.collection("users").document(userId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed deleting users/$userId document: ${e.message}")
        }
    }

    /**
     * Automatically cleans up:
     * 1) Users in `users` collection that were marked deleted, exist in `deleted_accounts`,
     *    have disposable/fake emails, or were deleted from Firebase Auth Console.
     * 2) Orphaned `usernames` documents whose `userId` no longer exists in `users`.
     * 3) Orphaned `posts` and `friend_requests` whose `userId` no longer exists in `users`.
     */
    fun cleanupOrphanedAndGhostAccounts(onComplete: ((Int) -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            var purgedCount = 0
            try {
                val db = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance()

                val deletedSnap = try {
                    db.collection("deleted_accounts").get().await()
                } catch (_: Exception) { null }
                val deletedIds = deletedSnap?.documents?.map { it.id }?.toSet() ?: emptySet()

                val usersSnap = db.collection("users").get().await()
                val validUserIds = mutableSetOf<String>()

                // Canary test to check if FirebaseAuth.fetchSignInMethodsForEmail is supported
                // (i.e. Email Enumeration Protection is off in Firebase Console)
                val currentUser = auth.currentUser
                val canaryEmail = currentUser?.email?.trim()?.takeIf { it.isNotBlank() } ?: ADMIN_EMAIL
                val canProbeAuthEmails = try {
                    val canaryRes = auth.fetchSignInMethodsForEmail(canaryEmail).await()
                    !canaryRes.signInMethods.isNullOrEmpty()
                } catch (_: Exception) {
                    false
                }

                for (doc in usersSnap.documents) {
                    val uid = doc.id
                    val email = doc.getString("email")?.trim()?.lowercase() ?: ""
                    val username = doc.getString("username")?.trim()?.lowercase() ?: ""
                    val name = doc.getString("name")?.trim() ?: ""
                    val isMarkedDeleted = doc.getBoolean("isDeleted") == true

                    val isGhostOrInvalid = isMarkedDeleted ||
                        uid in deletedIds ||
                        (email.isNotBlank() && !isValidRealEmail(email)) ||
                        (email.isBlank() && username.isBlank() && (name.isBlank() || name == "Anonymous Warrior"))

                    var isDeletedInFirebaseAuth = false
                    if (!isGhostOrInvalid && canProbeAuthEmails && email.isNotBlank() && email != ADMIN_EMAIL && uid != currentUser?.uid) {
                        try {
                            val methodsRes = auth.fetchSignInMethodsForEmail(email).await()
                            if (methodsRes.signInMethods.isNullOrEmpty()) {
                                isDeletedInFirebaseAuth = true
                            }
                        } catch (_: Exception) {}
                    }

                    if (isGhostOrInvalid || isDeletedInFirebaseAuth) {
                        purgeUserInternal(uid, username, email)
                        purgedCount++
                    } else {
                        validUserIds.add(uid)
                    }
                }

                // Clean up any orphaned `usernames` documents whose userId is not in validUserIds
                try {
                    val usernamesSnap = db.collection("usernames").get().await()
                    for (uDoc in usernamesSnap.documents) {
                        val ownerId = uDoc.getString("userId")?.trim() ?: ""
                        if (ownerId.isBlank() || ownerId !in validUserIds) {
                            try {
                                uDoc.reference.delete().await()
                                purgedCount++
                            } catch (_: Exception) {}
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error cleaning orphaned usernames: ${e.message}")
                }

                // Clean up any orphaned `friend_requests` referencing non-existent users
                try {
                    val reqSnap = db.collection("friend_requests").get().await()
                    for (rDoc in reqSnap.documents) {
                        val fromId = rDoc.getString("fromUserId") ?: ""
                        val toId = rDoc.getString("toUserId") ?: ""
                        if (fromId !in validUserIds || toId !in validUserIds) {
                            try { rDoc.reference.delete().await() } catch (_: Exception) {}
                        }
                    }
                } catch (_: Exception) {}

                // Clean up any orphaned `posts` referencing non-existent users
                try {
                    val postsSnap = db.collection("posts").get().await()
                    for (pDoc in postsSnap.documents) {
                        val authorId = pDoc.getString("userId") ?: ""
                        if (authorId.isNotBlank() && authorId !in validUserIds) {
                            try { pDoc.reference.delete().await() } catch (_: Exception) {}
                        }
                    }
                } catch (_: Exception) {}

            } catch (e: Exception) {
                Log.w(TAG, "cleanupOrphanedAndGhostAccounts error: ${e.message}")
            } finally {
                if (onComplete != null) {
                    withContext(Dispatchers.Main) {
                        onComplete(purgedCount)
                    }
                }
            }
        }
    }
}
