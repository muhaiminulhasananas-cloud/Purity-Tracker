package com.example

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.UUID

/**
 * Manages the BlockerHero-style Simple Blocker (v1.3.1):
 * 1. Accountability Partner email (stored in GuardianPrefs & mirrored to device-protected BlockerPrefs).
 *    - Strictly rejects the user's own account email.
 *    - Once saved, the partner email is LOCKED (read-only). Changing or removing the partner
 *      requires sending an approval request to the CURRENT partner via Firestore
 *      `blocker_disable_requests` + Trigger Email (`mail` collection). Only when the current
 *      partner approves (`status == "approved"`) is the partner email unlocked for editing.
 * 2. Adult content blocker toggle state (OFF by default on first install).
 *    - Cannot be turned OFF by PIN — turning OFF sends a disable request to Firestore + Trigger Email
 *      and waits for the partner to approve (`status == "approved"`).
 * 3. Browser URL & keyword matching for `ProtectionService` (no DNS, no VPN).
 */
object BlockerManager {

    private const val PREFS_NAME = "BlockerPrefs"
    private const val GUARDIAN_PREFS_NAME = "GuardianPrefs"

    private const val KEY_BLOCKER_ENABLED = "adult_blocker_enabled"
    private const val KEY_PARTNER_EMAIL = "partner_email"
    private const val KEY_PENDING_REQUEST_ID = "pending_disable_request_id"
    private const val KEY_PENDING_REQUEST_TIME = "pending_disable_request_time"
    private const val KEY_PENDING_PARTNER_CHANGE_ID = "pending_partner_change_request_id"
    private const val KEY_PENDING_PARTNER_CHANGE_TIME = "pending_partner_change_request_time"
    private const val KEY_PENDING_PIN_RESET_ID = "pending_pin_reset_request_id"
    private const val KEY_PENDING_PIN_RESET_TOKEN = "pending_pin_reset_request_token"
    private const val KEY_PENDING_PIN_RESET_TIME = "pending_pin_reset_request_time"
    private const val KEY_PIN_RESET_AUTHORIZED = "pin_reset_authorized_by_partner"
    private const val KEY_PARTNER_PIN_RESET_NOTICE = "partner_pin_reset_notice"

    /**
     * Single canonical approval web page URL (hosted on GitHub Pages `purity-approve` repository).
     */
    const val APPROVAL_PAGE_URL = "https://muhaiminulhasananas-cloud.github.io/purity-approve/approve.html"

    private var approvalListener: ListenerRegistration? = null
    private var listenedRequestId: String? = null

    private var partnerChangeListener: ListenerRegistration? = null
    private var listenedPartnerChangeId: String? = null

    private var pinResetListener: ListenerRegistration? = null
    private var listenedPinResetId: String? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    private fun getDeviceProtectedContext(context: Context): Context {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createDeviceProtectedStorageContext() ?: context
        } else {
            context
        }
    }

    // =========================================================================
    // 1. ACCOUNTABILITY PARTNER EMAIL (LOCKED ONCE SAVED)
    // =========================================================================

    fun getCurrentUserEmail(context: Context): String {
        val firebaseEmail = try {
            FirebaseAuth.getInstance().currentUser?.email?.trim().orEmpty()
        } catch (e: Exception) {
            ""
        }
        if (firebaseEmail.isNotEmpty()) return firebaseEmail

        val userPrefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return userPrefs.getString("user_email", "")?.trim().orEmpty()
    }

    fun getPartnerEmail(context: Context): String {
        val guardianPrefs = context.getSharedPreferences(GUARDIAN_PREFS_NAME, Context.MODE_PRIVATE)
        val fromGuardian = guardianPrefs.getString("guardianEmail", "")?.trim().orEmpty()
        if (fromGuardian.isNotEmpty()) return fromGuardian

        val dpPrefs = getDeviceProtectedContext(context).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return dpPrefs.getString(KEY_PARTNER_EMAIL, "")?.trim().orEmpty()
    }

    fun hasValidPartnerEmail(context: Context): Boolean {
        val partner = getPartnerEmail(context)
        if (partner.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(partner).matches()) return false
        val ownEmail = getCurrentUserEmail(context)
        if (ownEmail.isNotEmpty() && partner.equals(ownEmail, ignoreCase = true)) return false
        return true
    }

    /**
     * Validates and saves the Accountability Partner email ONLY when no valid partner email
     * is currently locked. Once saved, overwriting is blocked until the current partner
     * approves a partner-change request.
     * Returns null on success, or an error/warning string if invalid or locked.
     */
    fun validateAndSavePartnerEmail(context: Context, rawEmail: String): String? {
        if (hasValidPartnerEmail(context)) {
            return "Partner email is locked. Request approval from your current partner to change it."
        }

        val email = rawEmail.trim()
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Please enter a valid email address"
        }

        val ownEmail = getCurrentUserEmail(context)
        if (ownEmail.isNotEmpty() && email.equals(ownEmail, ignoreCase = true)) {
            return "Partner must be someone else, not your own email"
        }

        // Save in GuardianPrefs so existing Guardian alerts also use this partner email
        val guardianPrefs = context.getSharedPreferences(GUARDIAN_PREFS_NAME, Context.MODE_PRIVATE)
        val existingUserName = guardianPrefs.getString("userName", "")?.trim().orEmpty()
        val fallbackName = try {
            FirebaseAuth.getInstance().currentUser?.displayName?.trim().orEmpty()
        } catch (e: Exception) {
            ""
        }.ifEmpty {
            ownEmail.substringBefore("@").ifEmpty { "User" }
        }

        guardianPrefs.edit().apply {
            putString("guardianEmail", email)
            if (existingUserName.isEmpty()) {
                putString("userName", fallbackName)
            }
            if (guardianPrefs.getString("guardianName", "").isNullOrBlank()) {
                putString("guardianName", "Accountability Partner")
            }
            putBoolean("isShieldActive", true)
        }.apply()

        // Mirror to BlockerPrefs (both standard and device-protected)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PARTNER_EMAIL, email)
            .remove(KEY_PENDING_PARTNER_CHANGE_ID)
            .remove(KEY_PENDING_PARTNER_CHANGE_TIME)
            .apply()

        getDeviceProtectedContext(context)
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PARTNER_EMAIL, email)
            .remove(KEY_PENDING_PARTNER_CHANGE_ID)
            .remove(KEY_PENDING_PARTNER_CHANGE_TIME)
            .apply()

        return null
    }

    /**
     * Called ONLY when the current partner approves a partner-change request (`status == "approved"`).
     * Clears the locked partner email so the user can enter a new partner email.
     */
    private fun onPartnerApprovedEmailChange(context: Context) {
        context.getSharedPreferences(GUARDIAN_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove("guardianEmail")
            .apply()

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PARTNER_EMAIL)
            .remove(KEY_PENDING_PARTNER_CHANGE_ID)
            .remove(KEY_PENDING_PARTNER_CHANGE_TIME)
            .apply()

        getDeviceProtectedContext(context)
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PARTNER_EMAIL)
            .remove(KEY_PENDING_PARTNER_CHANGE_ID)
            .remove(KEY_PENDING_PARTNER_CHANGE_TIME)
            .apply()

        stopListeningForPartnerChangeApproval()
    }

    fun getPendingPartnerChangeRequestId(context: Context): String? {
        val stdPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dpPrefs = getDeviceProtectedContext(context).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return stdPrefs.getString(KEY_PENDING_PARTNER_CHANGE_ID, null)
            ?: dpPrefs.getString(KEY_PENDING_PARTNER_CHANGE_ID, null)
    }

    /**
     * Sends a request to the CURRENT Accountability Partner to approve changing/removing
     * the partner email, using the same Firestore `blocker_disable_requests` + Trigger Email flow.
     */
    fun sendPartnerChangeRequestToCurrentPartner(
        context: Context,
        onComplete: (Boolean, String) -> Unit
    ) {
        val currentPartnerEmail = getPartnerEmail(context)
        if (currentPartnerEmail.isEmpty()) {
            onComplete(false, "No active partner email to change.")
            return
        }

        val ownEmail = getCurrentUserEmail(context)
        val guardianPrefs = context.getSharedPreferences(GUARDIAN_PREFS_NAME, Context.MODE_PRIVATE)
        val userName = guardianPrefs.getString("userName", "")?.takeIf { it.isNotBlank() }
            ?: ownEmail.substringBefore("@").ifEmpty { "Your friend" }

        val userId = try {
            FirebaseAuth.getInstance().currentUser?.uid ?: "user"
        } catch (e: Exception) {
            "user"
        }

        val requestId = UUID.randomUUID().toString()
        val token = UUID.randomUUID().toString().replace("-", "")
        val now = System.currentTimeMillis()

        val db = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            onComplete(false, "Network error: Could not connect to Firebase.")
            return
        }

        val requestDoc = hashMapOf(
            "requestId" to requestId,
            "requestType" to "change_partner",
            "userId" to userId,
            "userEmail" to ownEmail,
            "userName" to userName,
            "partnerEmail" to currentPartnerEmail,
            "status" to "pending",
            "token" to token,
            "createdAt" to now
        )

        db.collection("blocker_disable_requests")
            .document(requestId)
            .set(requestDoc)
            .addOnSuccessListener {
                val approveUrl = "$APPROVAL_PAGE_URL?req=$requestId&token=$token&type=change_partner"

                val emailSubject = "Action Required: $userName requested to change their Accountability Partner"
                val emailText = """
                    Hello,

                    $userName (${ownEmail.ifEmpty { "Purity Tracker user" }}) has requested to change or remove you ($currentPartnerEmail) as their Accountability Partner in Purity Tracker.

                    Their partner email will stay LOCKED unless you approve this request.

                    To APPROVE or DENY this partner change request, open this link:
                    $approveUrl

                    If you do NOT want to allow changing the partner email, ignore this email or click Deny.

                    - Purity Tracker Accountability System
                """.trimIndent()

                val emailHtml = """
                    <div style="font-family: Arial, sans-serif; max-width: 560px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 14px; background-color: #0f172a; color: #f8fafc;">
                        <h2 style="color: #38bdf8; margin-top: 0;">🛡️ Purity Tracker — Partner Change Request</h2>
                        <p style="font-size: 15px; line-height: 1.6; color: #e2e8f0;">
                            <strong>$userName</strong> (${ownEmail.ifEmpty { "Purity Tracker User" }}) requested to <strong>change or remove</strong> their Accountability Partner email (currently <strong>$currentPartnerEmail</strong>).
                        </p>
                        <p style="font-size: 14px; line-height: 1.6; color: #cbd5e1;">
                            To prevent bypassing accountability, the partner email cannot be changed without your approval.
                        </p>
                        <div style="margin: 28px 0; text-align: center;">
                            <a href="$approveUrl" style="background-color: #10b981; color: #061018; font-weight: bold; text-decoration: none; padding: 14px 28px; border-radius: 10px; display: inline-block; font-size: 15px;">
                                Review &amp; Approve Partner Change
                            </a>
                        </div>
                        <p style="font-size: 12px; color: #94a3b8; text-align: center;">
                            Or open directly: <a href="$approveUrl" style="color: #38bdf8;">$approveUrl</a>
                        </p>
                        <hr style="border: none; border-top: 1px solid #1e293b; margin: 20px 0;" />
                        <p style="font-size: 12px; color: #94a3b8; margin-bottom: 0;">
                            If you deny this request or do not click approve, you will remain their active Accountability Partner.
                        </p>
                    </div>
                """.trimIndent()

                EmailJsSender.sendEmail(
                    toEmail = currentPartnerEmail,
                    subject = emailSubject,
                    messageText = emailText,
                    messageHtml = emailHtml
                ) { emailOk, emailMsg ->
                    if (emailOk) {
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putString(KEY_PENDING_PARTNER_CHANGE_ID, requestId)
                            .putLong(KEY_PENDING_PARTNER_CHANGE_TIME, now)
                            .apply()

                        getDeviceProtectedContext(context)
                            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putString(KEY_PENDING_PARTNER_CHANGE_ID, requestId)
                            .putLong(KEY_PENDING_PARTNER_CHANGE_TIME, now)
                            .apply()

                        ensurePartnerChangeApprovalListener(context) {}
                        onComplete(true, emailMsg)
                    } else {
                        try {
                            db.collection("blocker_disable_requests").document(requestId).delete()
                        } catch (e: Exception) {}
                        onComplete(false, emailMsg)
                    }
                }
            }
            .addOnFailureListener { e ->
                mainHandler.post {
                    onComplete(false, "Failed to send request: ${e.localizedMessage ?: "Check internet connection"}")
                }
            }
    }

    fun ensurePartnerChangeApprovalListener(
        context: Context,
        onStatusChanged: (String) -> Unit
    ) {
        val pendingId = getPendingPartnerChangeRequestId(context)
        if (pendingId.isNullOrBlank() || !hasValidPartnerEmail(context)) {
            stopListeningForPartnerChangeApproval()
            return
        }

        if (listenedPartnerChangeId == pendingId && partnerChangeListener != null) {
            return
        }

        stopListeningForPartnerChangeApproval()
        listenedPartnerChangeId = pendingId
        val appContext = context.applicationContext

        try {
            val db = FirebaseFirestore.getInstance()
            partnerChangeListener = db.collection("blocker_disable_requests")
                .document(pendingId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                    val status = snapshot.getString("status")?.lowercase()?.trim().orEmpty()
                    if (status == "approved") {
                        onPartnerApprovedEmailChange(appContext)
                        mainHandler.post {
                            onStatusChanged("approved")
                        }
                    } else if (status == "denied" || status == "rejected") {
                        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .remove(KEY_PENDING_PARTNER_CHANGE_ID)
                            .remove(KEY_PENDING_PARTNER_CHANGE_TIME)
                            .apply()
                        getDeviceProtectedContext(appContext)
                            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .remove(KEY_PENDING_PARTNER_CHANGE_ID)
                            .remove(KEY_PENDING_PARTNER_CHANGE_TIME)
                            .apply()
                        stopListeningForPartnerChangeApproval()
                        mainHandler.post {
                            onStatusChanged("denied")
                        }
                    }
                }
        } catch (e: Exception) {}
    }

    fun stopListeningForPartnerChangeApproval() {
        try {
            partnerChangeListener?.remove()
        } catch (e: Exception) {}
        partnerChangeListener = null
        listenedPartnerChangeId = null
    }

    // =========================================================================
    // 2. ADULT CONTENT BLOCKER SWITCH STATE
    // =========================================================================

    /**
     * OFF by default on first install.
     */
    fun isAdultBlockerEnabled(context: Context): Boolean {
        val stdPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dpPrefs = getDeviceProtectedContext(context).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return stdPrefs.getBoolean(KEY_BLOCKER_ENABLED, false) ||
                dpPrefs.getBoolean(KEY_BLOCKER_ENABLED, false)
    }

    /**
     * Enables the Adult Content Blocker.
     * Note: Turning OFF is strictly restricted to `onPartnerApprovedDisable(context)`.
     */
    fun enableAdultBlocker(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BLOCKER_ENABLED, true)
            .remove(KEY_PENDING_REQUEST_ID)
            .apply()

        getDeviceProtectedContext(context)
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BLOCKER_ENABLED, true)
            .remove(KEY_PENDING_REQUEST_ID)
            .apply()
    }

    /**
     * Called ONLY when the Firestore disable-request document has `status == "approved"`.
     * NEVER called by PIN verification.
     */
    private fun onPartnerApprovedDisable(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BLOCKER_ENABLED, false)
            .remove(KEY_PENDING_REQUEST_ID)
            .remove(KEY_PENDING_REQUEST_TIME)
            .apply()

        getDeviceProtectedContext(context)
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BLOCKER_ENABLED, false)
            .remove(KEY_PENDING_REQUEST_ID)
            .remove(KEY_PENDING_REQUEST_TIME)
            .apply()

        stopListeningForPartnerApproval()
    }

    fun getPendingRequestId(context: Context): String? {
        val stdPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dpPrefs = getDeviceProtectedContext(context).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return stdPrefs.getString(KEY_PENDING_REQUEST_ID, null)
            ?: dpPrefs.getString(KEY_PENDING_REQUEST_ID, null)
    }

    // =========================================================================
    // 3. PARTNER EMAIL APPROVAL FLOW FOR TURNING OFF BLOCKER
    // =========================================================================

    /**
     * Sends a disable request to Firestore (`blocker_disable_requests/{requestId}`)
     * and emails the Accountability Partner via the `mail` collection (Firebase Trigger Email extension).
     */
    fun sendDisableRequestToPartner(
        context: Context,
        onComplete: (Boolean, String) -> Unit
    ) {
        val partnerEmail = getPartnerEmail(context)
        if (partnerEmail.isEmpty()) {
            onComplete(false, "Please save an accountability partner email first.")
            return
        }

        val ownEmail = getCurrentUserEmail(context)
        if (ownEmail.isNotEmpty() && partnerEmail.equals(ownEmail, ignoreCase = true)) {
            onComplete(false, "Partner must be someone else, not your own email")
            return
        }

        val guardianPrefs = context.getSharedPreferences(GUARDIAN_PREFS_NAME, Context.MODE_PRIVATE)
        val userName = guardianPrefs.getString("userName", "")?.takeIf { it.isNotBlank() }
            ?: ownEmail.substringBefore("@").ifEmpty { "Your friend" }

        val userId = try {
            FirebaseAuth.getInstance().currentUser?.uid ?: "user"
        } catch (e: Exception) {
            "user"
        }

        val requestId = UUID.randomUUID().toString()
        val token = UUID.randomUUID().toString().replace("-", "")
        val now = System.currentTimeMillis()

        val db = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            onComplete(false, "Network error: Could not connect to Firebase.")
            return
        }

        val requestDoc = hashMapOf(
            "requestId" to requestId,
            "requestType" to "disable_blocker",
            "userId" to userId,
            "userEmail" to ownEmail,
            "userName" to userName,
            "partnerEmail" to partnerEmail,
            "status" to "pending",
            "token" to token,
            "createdAt" to now
        )

        db.collection("blocker_disable_requests")
            .document(requestId)
            .set(requestDoc)
            .addOnSuccessListener {
                // Single approval URL (GitHub Pages `purity-approve` live site)
                val approveUrl = "$APPROVAL_PAGE_URL?req=$requestId&token=$token"

                val emailSubject = "Action Required: $userName requested to turn OFF Adult Content Blocker"
                val emailText = """
                    Hello,

                    $userName (${ownEmail.ifEmpty { "Purity Tracker user" }}) has requested to turn OFF "Block adult content" in Purity Tracker.

                    As their Accountability Partner, the blocker will stay ON unless you approve this request.

                    To APPROVE turning off the blocker, open this link:
                    $approveUrl

                    If you do NOT want to turn off their blocker, simply ignore this email and the blocker will remain active.

                    - Purity Tracker Accountability System
                """.trimIndent()

                val emailHtml = """
                    <div style="font-family: Arial, sans-serif; max-width: 560px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 14px; background-color: #0f172a; color: #f8fafc;">
                        <h2 style="color: #38bdf8; margin-top: 0;">🛡️ Purity Tracker — Partner Approval Request</h2>
                        <p style="font-size: 15px; line-height: 1.6; color: #e2e8f0;">
                            <strong>$userName</strong> (${ownEmail.ifEmpty { "Purity Tracker User" }}) wants to turn <strong>OFF</strong> the <strong>Block adult content</strong> protection on their phone.
                        </p>
                        <p style="font-size: 14px; line-height: 1.6; color: #cbd5e1;">
                            Because you are registered as their Accountability Partner, the blocker cannot be turned off without your approval.
                        </p>
                        <div style="margin: 28px 0; text-align: center;">
                            <a href="$approveUrl" style="background-color: #10b981; color: #061018; font-weight: bold; text-decoration: none; padding: 14px 28px; border-radius: 10px; display: inline-block; font-size: 15px;">
                                Approve Turning OFF Blocker
                            </a>
                        </div>
                        <p style="font-size: 12px; color: #94a3b8; text-align: center;">
                            Or open directly: <a href="$approveUrl" style="color: #38bdf8;">$approveUrl</a>
                        </p>
                        <hr style="border: none; border-top: 1px solid #1e293b; margin: 20px 0;" />
                        <p style="font-size: 12px; color: #94a3b8; margin-bottom: 0;">
                            If you deny this request or do not click approve, the adult content blocker will remain <strong>ON</strong>.
                        </p>
                    </div>
                """.trimIndent()

                EmailJsSender.sendEmail(
                    toEmail = partnerEmail,
                    subject = emailSubject,
                    messageText = emailText,
                    messageHtml = emailHtml
                ) { emailOk, emailMsg ->
                    if (emailOk) {
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putString(KEY_PENDING_REQUEST_ID, requestId)
                            .putLong(KEY_PENDING_REQUEST_TIME, now)
                            .apply()

                        getDeviceProtectedContext(context)
                            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putString(KEY_PENDING_REQUEST_ID, requestId)
                            .putLong(KEY_PENDING_REQUEST_TIME, now)
                            .apply()

                        ensurePartnerApprovalListener(context) {}
                        onComplete(true, emailMsg)
                    } else {
                        try {
                            db.collection("blocker_disable_requests").document(requestId).delete()
                        } catch (e: Exception) {}
                        onComplete(false, emailMsg)
                    }
                }
            }
            .addOnFailureListener { e ->
                mainHandler.post {
                    onComplete(false, "Failed to send request: ${e.localizedMessage ?: "Check internet connection"}")
                }
            }
    }

    /**
     * Attaches a Firestore real-time listener to `blocker_disable_requests/{pendingRequestId}`.
     * When `status == "approved"`, automatically turns OFF the adult content blocker switch.
     */
    fun ensurePartnerApprovalListener(
        context: Context,
        onStatusChanged: (String) -> Unit
    ) {
        val pendingId = getPendingRequestId(context)
        if (pendingId.isNullOrBlank() || !isAdultBlockerEnabled(context)) {
            stopListeningForPartnerApproval()
            return
        }

        if (listenedRequestId == pendingId && approvalListener != null) {
            return
        }

        stopListeningForPartnerApproval()
        listenedRequestId = pendingId
        val appContext = context.applicationContext

        try {
            val db = FirebaseFirestore.getInstance()
            approvalListener = db.collection("blocker_disable_requests")
                .document(pendingId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                    val status = snapshot.getString("status")?.lowercase()?.trim().orEmpty()
                    if (status == "approved") {
                        onPartnerApprovedDisable(appContext)
                        mainHandler.post {
                            onStatusChanged("approved")
                        }
                    } else if (status == "denied" || status == "rejected") {
                        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .remove(KEY_PENDING_REQUEST_ID)
                            .apply()
                        getDeviceProtectedContext(appContext)
                            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .remove(KEY_PENDING_REQUEST_ID)
                            .apply()
                        stopListeningForPartnerApproval()
                        mainHandler.post {
                            onStatusChanged("denied")
                        }
                    }
                }
        } catch (e: Exception) {}
    }

    fun stopListeningForPartnerApproval() {
        try {
            approvalListener?.remove()
        } catch (e: Exception) {}
        approvalListener = null
        listenedRequestId = null
    }

    // =========================================================================
    // 3B. PARTNER EMAIL APPROVAL FLOW FOR PIN RECOVERY ("Forgot PIN?")
    // =========================================================================

    fun getPendingPinResetRequestId(context: Context): String? {
        val stdPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dpPrefs = getDeviceProtectedContext(context).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return stdPrefs.getString(KEY_PENDING_PIN_RESET_ID, null)
            ?: dpPrefs.getString(KEY_PENDING_PIN_RESET_ID, null)
    }

    fun getPendingPinResetToken(context: Context): String? {
        val stdPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dpPrefs = getDeviceProtectedContext(context).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return stdPrefs.getString(KEY_PENDING_PIN_RESET_TOKEN, null)
            ?: dpPrefs.getString(KEY_PENDING_PIN_RESET_TOKEN, null)
    }

    fun isPinResetAuthorized(context: Context): Boolean {
        return false
    }

    fun clearPinResetAuthorized(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PIN_RESET_AUTHORIZED, false)
            .apply()

        getDeviceProtectedContext(context)
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PIN_RESET_AUTHORIZED, false)
            .apply()
    }

    fun consumePartnerPinResetNotice(context: Context): Boolean {
        val stdPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dpPrefs = getDeviceProtectedContext(context).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasNotice = stdPrefs.getBoolean(KEY_PARTNER_PIN_RESET_NOTICE, false) ||
                dpPrefs.getBoolean(KEY_PARTNER_PIN_RESET_NOTICE, false)
        if (hasNotice) {
            stdPrefs.edit().putBoolean(KEY_PARTNER_PIN_RESET_NOTICE, false).apply()
            dpPrefs.edit().putBoolean(KEY_PARTNER_PIN_RESET_NOTICE, false).apply()
        }
        return hasNotice
    }

    /**
     * Applies the partner-submitted SHA-256 `pinHash` (salted with the one-time request `token`)
     * directly as the new app PIN. The old PIN stops working immediately, and the user never sees
     * or types the new PIN to set it.
     */
    fun onPartnerApprovedPinReset(
        context: Context,
        pinHash: String,
        saltToken: String
    ): Boolean {
        val applied = LockManager.applyPartnerPinHash(context, pinHash, saltToken)
        if (!applied) return false

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PENDING_PIN_RESET_ID)
            .remove(KEY_PENDING_PIN_RESET_TOKEN)
            .remove(KEY_PENDING_PIN_RESET_TIME)
            .putBoolean(KEY_PIN_RESET_AUTHORIZED, false)
            .putBoolean(KEY_PARTNER_PIN_RESET_NOTICE, true)
            .commit()

        getDeviceProtectedContext(context)
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PENDING_PIN_RESET_ID)
            .remove(KEY_PENDING_PIN_RESET_TOKEN)
            .remove(KEY_PENDING_PIN_RESET_TIME)
            .putBoolean(KEY_PIN_RESET_AUTHORIZED, false)
            .putBoolean(KEY_PARTNER_PIN_RESET_NOTICE, true)
            .commit()

        stopListeningForPinResetApproval()
        return true
    }

    /**
     * Sends a tokenized PIN reset request to the Accountability Partner
     * via Firestore `blocker_disable_requests/{requestId}` and `EmailJsSender.sendEmail`.
     * The email contains ONLY the link — never the PIN. The partner sets the new PIN on approve.html.
     */
    fun sendPinResetRequestToPartner(
        context: Context,
        onComplete: (Boolean, String) -> Unit
    ) {
        val partnerEmail = getPartnerEmail(context)
        if (partnerEmail.isEmpty() || !hasValidPartnerEmail(context)) {
            onComplete(false, "Save a valid accountability partner email first.")
            return
        }

        val ownEmail = getCurrentUserEmail(context)
        val guardianPrefs = context.getSharedPreferences(GUARDIAN_PREFS_NAME, Context.MODE_PRIVATE)
        val userName = guardianPrefs.getString("userName", "")?.takeIf { it.isNotBlank() }
            ?: ownEmail.substringBefore("@").ifEmpty { "Your friend" }

        val userId = try {
            FirebaseAuth.getInstance().currentUser?.uid ?: "user"
        } catch (e: Exception) {
            "user"
        }

        val requestId = UUID.randomUUID().toString()
        val token = UUID.randomUUID().toString().replace("-", "")
        val now = System.currentTimeMillis()

        val db = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            onComplete(false, "Network error: Could not connect to Firebase.")
            return
        }

        val requestDoc = hashMapOf(
            "requestId" to requestId,
            "requestType" to "pin_reset",
            "userId" to userId,
            "userEmail" to ownEmail,
            "userName" to userName,
            "partnerEmail" to partnerEmail,
            "status" to "pending",
            "token" to token,
            "createdAt" to now
        )

        db.collection("blocker_disable_requests")
            .document(requestId)
            .set(requestDoc)
            .addOnSuccessListener {
                val approveUrl = "$APPROVAL_PAGE_URL?req=$requestId&token=$token&type=pin_reset"

                val emailSubject = "Action Required: Set a new App Security PIN for $userName"
                val emailText = """
                    Hello,

                    $userName (${ownEmail.ifEmpty { "Purity Tracker user" }}) requested a PIN reset in Purity Tracker.

                    They cannot set or reset their PIN alone. Only you can set a new 4–6 digit PIN for them using the secure link below:
                    $approveUrl

                    If you do NOT want to reset their PIN, ignore this email or click Deny on the page.

                    - Purity Tracker Accountability System
                """.trimIndent()

                val emailHtml = """
                    <div style="font-family: Arial, sans-serif; max-width: 560px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 14px; background-color: #0f172a; color: #f8fafc;">
                        <h2 style="color: #38bdf8; margin-top: 0;">🛡️ Purity Tracker — Partner PIN Reset Request</h2>
                        <p style="font-size: 15px; line-height: 1.6; color: #e2e8f0;">
                            <strong>$userName</strong> (${ownEmail.ifEmpty { "Purity Tracker User" }}) requested a reset of their <strong>App Security PIN</strong>.
                        </p>
                        <p style="font-size: 14px; line-height: 1.6; color: #cbd5e1;">
                            They cannot set or reset their PIN alone. Open the secure link below to set a new 4–6 digit PIN for their device.
                        </p>
                        <div style="margin: 28px 0; text-align: center;">
                            <a href="$approveUrl" style="background-color: #10b981; color: #061018; font-weight: bold; text-decoration: none; padding: 14px 28px; border-radius: 10px; display: inline-block; font-size: 15px;">
                                Set a New PIN for User
                            </a>
                        </div>
                        <p style="font-size: 12px; color: #94a3b8; text-align: center;">
                            Or open directly: <a href="$approveUrl" style="color: #38bdf8;">$approveUrl</a>
                        </p>
                        <hr style="border: none; border-top: 1px solid #1e293b; margin: 20px 0;" />
                        <p style="font-size: 12px; color: #94a3b8; margin-bottom: 0;">
                            If you deny this request or do not submit a new PIN, their current PIN remains unchanged.
                        </p>
                    </div>
                """.trimIndent()

                EmailJsSender.sendEmail(
                    toEmail = partnerEmail,
                    subject = emailSubject,
                    messageText = emailText,
                    messageHtml = emailHtml
                ) { emailOk, emailMsg ->
                    if (emailOk) {
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putString(KEY_PENDING_PIN_RESET_ID, requestId)
                            .putString(KEY_PENDING_PIN_RESET_TOKEN, token)
                            .putLong(KEY_PENDING_PIN_RESET_TIME, now)
                            .apply()

                        getDeviceProtectedContext(context)
                            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putString(KEY_PENDING_PIN_RESET_ID, requestId)
                            .putString(KEY_PENDING_PIN_RESET_TOKEN, token)
                            .putLong(KEY_PENDING_PIN_RESET_TIME, now)
                            .apply()

                        ensurePinResetApprovalListener(context) {}
                        onComplete(true, "PIN reset request sent — waiting for partner approval")
                    } else {
                        try {
                            db.collection("blocker_disable_requests").document(requestId).delete()
                        } catch (e: Exception) {}
                        onComplete(false, emailMsg)
                    }
                }
            }
            .addOnFailureListener { e ->
                mainHandler.post {
                    onComplete(false, "Failed to send PIN reset request: ${e.localizedMessage ?: "Check internet connection"}")
                }
            }
    }

    fun ensurePinResetApprovalListener(
        context: Context,
        onStatusChanged: (String) -> Unit
    ) {
        val pendingId = getPendingPinResetRequestId(context)
        if (pendingId.isNullOrBlank()) {
            stopListeningForPinResetApproval()
            return
        }

        if (listenedPinResetId == pendingId && pinResetListener != null) {
            return
        }

        stopListeningForPinResetApproval()
        listenedPinResetId = pendingId
        val appContext = context.applicationContext

        try {
            val db = FirebaseFirestore.getInstance()
            pinResetListener = db.collection("blocker_disable_requests")
                .document(pendingId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                    val status = snapshot.getString("status")?.lowercase()?.trim().orEmpty()
                    val pinHash = snapshot.getString("pinHash")?.trim().orEmpty()
                    val docToken = snapshot.getString("token")?.trim().orEmpty()
                        .ifEmpty { getPendingPinResetToken(appContext).orEmpty() }
                    if (status == "approved" && pinHash.length == 64 && docToken.isNotEmpty()) {
                        val applied = onPartnerApprovedPinReset(appContext, pinHash, docToken)
                        if (applied) {
                            mainHandler.post {
                                onStatusChanged("approved")
                            }
                        }
                    } else if (status == "denied" || status == "rejected") {
                        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .remove(KEY_PENDING_PIN_RESET_ID)
                            .remove(KEY_PENDING_PIN_RESET_TOKEN)
                            .remove(KEY_PENDING_PIN_RESET_TIME)
                            .apply()
                        getDeviceProtectedContext(appContext)
                            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .remove(KEY_PENDING_PIN_RESET_ID)
                            .remove(KEY_PENDING_PIN_RESET_TOKEN)
                            .remove(KEY_PENDING_PIN_RESET_TIME)
                            .apply()
                        stopListeningForPinResetApproval()
                        mainHandler.post {
                            onStatusChanged("denied")
                        }
                    }
                }
        } catch (e: Exception) {}
    }

    fun stopListeningForPinResetApproval() {
        try {
            pinResetListener?.remove()
        } catch (e: Exception) {}
        pinResetListener = null
        listenedPinResetId = null
    }

    // =========================================================================
    // 4. BROWSER PACKAGE & ADULT URL / KEYWORD DETECTION (NO DNS, NO VPN)
    // =========================================================================

    private val BROWSER_PACKAGES = setOf(
        "com.android.chrome",
        "com.chrome.beta",
        "com.chrome.dev",
        "com.chrome.canary",
        "com.sec.android.app.sbrowser",
        "com.sec.android.app.sbrowser.beta",
        "org.mozilla.firefox",
        "org.mozilla.firefox_beta",
        "org.mozilla.fenix",
        "org.mozilla.focus",
        "com.opera.browser",
        "com.opera.mini.native",
        "com.opera.gx",
        "com.opera.touch",
        "com.microsoft.emmx",
        "com.brave.browser",
        "com.duckduckgo.mobile.android",
        "com.UCMobile.intl",
        "com.uc.browser.en",
        "com.mi.globalbrowser",
        "com.android.browser",
        "com.heytap.browser",
        "com.coloros.browser",
        "com.nearme.browser",
        "com.vivo.browser",
        "com.vivaldi.browser",
        "com.kiwibrowser.browser",
        "com.yandex.browser",
        "com.ecosia.android",
        "com.torproject.torbrowser",
        "com.aloha.browser",
        "com.puffin.browser",
        "mark.via.gp"
    )

    fun isBrowserPackage(pkgName: String): Boolean {
        if (pkgName.isEmpty()) return false
        val lower = pkgName.lowercase()
        if (lower in BROWSER_PACKAGES) return true
        return lower.contains(".browser") ||
                lower.endsWith("browser") ||
                lower.contains("chrome") ||
                lower.contains("sbrowser") ||
                lower.contains("firefox")
    }

    private val ADULT_DOMAIN_MARKERS = listOf(
        "pornhub", "xvideos", "xnxx", "xhamster", "redtube", "youporn",
        "spankbang", "eporner", "tube8", "beeg.com", "chaturbate", "stripchat",
        "onlyfans.com", "fansly.com", "brazzers", "naughtyamerica", "realitykings",
        "bangbros", "rule34", "nhentai", "hanime", "hentaihaven", "hqporner",
        "daftsex", "sxyprn", "motherless.com", "fuq.com", "tnaflix", "drtuber",
        "txxx.com", "upornia", "javhd", "javmost", "missav", "jable.tv",
        "cam4.com", "bongacams", "myfreecams", "livejasmin", "erome.com",
        "fapello", "coomer.su", "kemono.su", "literotica", "sexstories",
        "antarvasna", "masalaseen", "desixnxx", "indianpornvideos", "redgifs",
        "xxx.com", "porn.com", "sex.com", "porntrex", "thumbzilla", "xtube",
        "pornone", "vjav", "javfinder", "supjav", "hentaigasm", "muchohentai"
    )

    private val ADULT_WORD_TOKENS = setOf(
        "porn", "porno", "pornhub", "xxx", "xvideos", "xnxx", "xhamster",
        "hentai", "nhentai", "rule34", "nsfw", "redtube", "youporn",
        "spankbang", "chaturbate", "stripchat", "brazzers", "camgirl",
        "camgirls", "blowjob", "handjob", "cumshot", "creampie", "gangbang",
        "bdsm", "milf", "boobs", "pussy", "dildo", "vibrator", "masturbate",
        "masturbation", "nude", "nudes", "sexvideo", "sexvideos", "redgifs",
        "choti", "chuda", "chudai", "পর্ন", "চটি", "সেক্স"
    )

    private val ADULT_PHRASES = listOf(
        "porn video", "sex video", "xxx video", "nude video", "naked girls",
        "naked women", "hot sex", "desi sex", "indian porn", "bangla sex",
        "bangla choti", "bf video", "blue film", "সেক্স ভিডিও", "পর্ন ভিডিও",
        " নগ্ন ভিডিও", "চটি গল্প"
    )

    private val WORD_SPLIT_REGEX = Regex("[^\\p{L}\\p{N}]+")

    fun detectAdultContent(text: String): String? {
        if (text.isBlank()) return null
        val lower = text.lowercase()

        if (lower.contains("purity tracker") && lower.contains("adult content blocked")) {
            return null
        }

        for (domain in ADULT_DOMAIN_MARKERS) {
            if (lower.contains(domain)) {
                return domain
            }
        }

        for (phrase in ADULT_PHRASES) {
            if (lower.contains(phrase)) {
                return phrase
            }
        }

        val tokens = lower.split(WORD_SPLIT_REGEX)
        for (token in tokens) {
            if (token.length >= 3 && token in ADULT_WORD_TOKENS) {
                return token
            }
        }

        return null
    }
}
