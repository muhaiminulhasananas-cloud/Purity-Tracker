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
 * Manages the BlockerHero-style Simple Blocker (v1.3.0):
 * 1. Accountability Partner email (stored in GuardianPrefs & mirrored to device-protected BlockerPrefs).
 *    Strictly rejects the user's own account email.
 * 2. Adult content blocker toggle state (OFF by default on first install).
 *    Cannot be turned OFF by PIN — turning OFF sends a disable request to Firestore + Trigger Email
 *    and waits for the partner to approve (`status == "approved"`).
 * 3. Browser URL & keyword matching for `ProtectionService` (no DNS, no VPN).
 */
object BlockerManager {

    private const val PREFS_NAME = "BlockerPrefs"
    private const val GUARDIAN_PREFS_NAME = "GuardianPrefs"

    private const val KEY_BLOCKER_ENABLED = "adult_blocker_enabled"
    private const val KEY_PARTNER_EMAIL = "partner_email"
    private const val KEY_PENDING_REQUEST_ID = "pending_disable_request_id"
    private const val KEY_PENDING_REQUEST_TIME = "pending_disable_request_time"

    private var approvalListener: ListenerRegistration? = null
    private var listenedRequestId: String? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private fun getDeviceProtectedContext(context: Context): Context {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createDeviceProtectedStorageContext() ?: context
        } else {
            context
        }
    }

    // =========================================================================
    // 1. ACCOUNTABILITY PARTNER EMAIL
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
     * Validates and saves the Accountability Partner email.
     * Returns null on success, or an error/warning string if invalid.
     */
    fun validateAndSavePartnerEmail(context: Context, rawEmail: String): String? {
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

        // Also mirror to BlockerPrefs (both standard and device-protected)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PARTNER_EMAIL, email)
            .apply()

        getDeviceProtectedContext(context)
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PARTNER_EMAIL, email)
            .apply()

        return null
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
    // 3. PARTNER EMAIL APPROVAL FLOW (FIRESTORE + TRIGGER EMAIL)
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
                // Save pending request ID locally
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

                // Start real-time listener immediately
                ensurePartnerApprovalListener(context) {}

                // Send email to accountability partner via Firebase Trigger Email (`mail` collection)
                val githubApproveUrl = "https://muhaiminulhasananas-cloud.github.io/purity-approve/approve.html?req=$requestId&token=$token"
                val firebaseApproveUrl = "https://purity-tracker-f4d76.web.app/approve.html?req=$requestId&token=$token"

                val emailSubject = "Action Required: $userName requested to turn OFF Adult Content Blocker"
                val emailText = """
                    Hello,

                    $userName (${ownEmail.ifEmpty { "Purity Tracker user" }}) has requested to turn OFF "Block adult content" in Purity Tracker.

                    As their Accountability Partner, the blocker will stay ON unless you approve this request.

                    To APPROVE turning off the blocker, open this link:
                    $githubApproveUrl

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
                            <a href="$githubApproveUrl" style="background-color: #10b981; color: #061018; font-weight: bold; text-decoration: none; padding: 14px 28px; border-radius: 10px; display: inline-block; font-size: 15px;">
                                Approve Turning OFF Blocker
                            </a>
                        </div>
                        <p style="font-size: 12px; color: #94a3b8; text-align: center;">
                            Or open directly: <a href="$githubApproveUrl" style="color: #38bdf8;">$githubApproveUrl</a>
                        </p>
                        <hr style="border: none; border-top: 1px solid #1e293b; margin: 20px 0;" />
                        <p style="font-size: 12px; color: #94a3b8; margin-bottom: 0;">
                            If you deny this request or do not click approve, the adult content blocker will remain <strong>ON</strong>.
                        </p>
                    </div>
                """.trimIndent()

                val mailDoc = hashMapOf(
                    "to" to partnerEmail,
                    "message" to hashMapOf(
                        "subject" to emailSubject,
                        "text" to emailText,
                        "html" to emailHtml
                    ),
                    "requestId" to requestId,
                    "timestamp" to now
                )

                db.collection("mail").add(mailDoc)
                    .addOnSuccessListener {
                        mainHandler.post {
                            onComplete(true, "Request sent to $partnerEmail. Waiting for partner approval.")
                        }
                    }
                    .addOnFailureListener {
                        // Request document is saved in Firestore; inform user
                        mainHandler.post {
                            onComplete(true, "Request queued for $partnerEmail. Waiting for partner approval.")
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
                        // Clear pending request ID so user knows it was denied; blocker stays ON
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
        "com. via.gp",
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

    // High-confidence adult domains & substrings in URLs/queries
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

    // Strict word-level adult keywords (matched on token boundaries to avoid false positives)
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

    /**
     * Inspects URL bar text or visible browser text.
     * Returns the matched adult domain/keyword if detected, or null if clean.
     */
    fun detectAdultContent(text: String): String? {
        if (text.isBlank()) return null
        val lower = text.lowercase()

        // Ignore our own block screen text if present
        if (lower.contains("purity tracker") && lower.contains("adult content blocked")) {
            return null
        }

        // 1. Check domain markers
        for (domain in ADULT_DOMAIN_MARKERS) {
            if (lower.contains(domain)) {
                return domain
            }
        }

        // 2. Check multi-word phrases
        for (phrase in ADULT_PHRASES) {
            if (lower.contains(phrase)) {
                return phrase
            }
        }

        // 3. Check exact word tokens (avoids false positives on substrings like "essex", "document", "class")
        val tokens = lower.split(WORD_SPLIT_REGEX)
        for (token in tokens) {
            if (token.length >= 3 && token in ADULT_WORD_TOKENS) {
                return token
            }
        }

        return null
    }
}
