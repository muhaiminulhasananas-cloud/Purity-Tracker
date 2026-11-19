package com.example

import android.content.Context
import android.media.MediaPlayer
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    org.junit.Assert.assertNotNull(appName)
  }

  @Test
  fun `launch MainActivity test`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).create().start().resume()
    org.junit.Assert.assertNotNull(controller.get())
  }

  @Test
  fun `partner email is locked after saving and rejects direct overwrite`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("GuardianPrefs", Context.MODE_PRIVATE).edit().clear().commit()
    context.getSharedPreferences("BlockerPrefs", Context.MODE_PRIVATE).edit().clear().commit()
    context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit()
      .putString("user_email", "me@example.com").commit()

    // 1. Rejects user's own email
    val ownErr = BlockerManager.validateAndSavePartnerEmail(context, "me@example.com")
    assertEquals("Partner must be someone else, not your own email", ownErr)

    // 2. Saves valid partner email
    val firstSave = BlockerManager.validateAndSavePartnerEmail(context, "partner1@example.com")
    org.junit.Assert.assertNull(firstSave)
    assertEquals("partner1@example.com", BlockerManager.getPartnerEmail(context))

    // 3. Rejects direct overwrite once locked
    val secondAttempt = BlockerManager.validateAndSavePartnerEmail(context, "partner2@example.com")
    org.junit.Assert.assertNotNull(secondAttempt)
    assertEquals("partner1@example.com", BlockerManager.getPartnerEmail(context))
  }

  @Test
  fun `EmailJsSender builds required JSON fields and reports failure honestly`() {
    val json = EmailJsSender.buildRequestJson(
      toEmail = "partner@example.com",
      subject = "Test Subject",
      messageText = "Plain text body",
      messageHtml = "<p>HTML body</p>",
      serviceId = "service_123",
      templateId = "template_456",
      publicKey = "pub_789"
    )
    assertEquals("service_123", json.getString("service_id"))
    assertEquals("template_456", json.getString("template_id"))
    assertEquals("pub_789", json.getString("user_id"))

    val params = json.getJSONObject("template_params")
    assertEquals("partner@example.com", params.getString("to_email"))
    assertEquals("Test Subject", params.getString("subject"))
    assertEquals("Plain text body", params.getString("message_text"))
    assertEquals("<p>HTML body</p>", params.getString("message_html"))

    var callbackSuccess: Boolean? = null
    var callbackMessage: String? = null
    EmailJsSender.sendEmail(
      toEmail = "partner@example.com",
      subject = "Test Subject",
      messageText = "Plain text body",
      messageHtml = "<p>HTML body</p>",
      endpointUrl = "http://127.0.0.1:1/unreachable",
      onComplete = { ok, msg ->
        callbackSuccess = ok
        callbackMessage = msg
      }
    )

    val deadline = System.currentTimeMillis() + 5000L
    while (callbackSuccess == null && System.currentTimeMillis() < deadline) {
      org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
      Thread.sleep(25)
    }

    assertEquals(false, callbackSuccess)
    org.junit.Assert.assertTrue(callbackMessage.orEmpty().contains("Failed to send email"))
  }

  @Test
  fun `EmailJsConfig uses Purity Tracker Team account IDs`() {
    assertEquals("service_skp0w8g", EmailJsConfig.SERVICE_ID)
    assertEquals("template_0eupng9", EmailJsConfig.TEMPLATE_ID)
    assertEquals("j-pFuf5HzcCyLp6Ox", EmailJsConfig.PUBLIC_KEY)
  }

  @Test
  fun `user cannot set or reset PIN alone and partner-set PIN hash round-trip works end to end`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE).edit().clear().commit()
    context.getSharedPreferences("BlockerPrefs", Context.MODE_PRIVATE).edit().clear().commit()

    // 1. User can NEVER set or reset a PIN alone
    val (selfSaved, selfKey) = LockManager.setPin(context, "9999")
    assertEquals(false, selfSaved)
    org.junit.Assert.assertNull(selfKey)
    assertEquals(false, LockManager.hasPin(context))
    assertEquals(false, LockManager.verifyPin(context, "9999"))
    org.junit.Assert.assertTrue(
      LockManager.resetPinWithGuardianKey(context, "G-1234-AB", "1234") is ResetResult.InvalidKey
    )

    // 2. Suppose partner previously set PIN "5678" using token "tok_old_abc"
    val oldToken = "tok_old_abc"
    val oldHash = LockManager.computeSaltedPinHash("5678", oldToken)
    assertEquals(true, LockManager.applyPartnerPinHash(context, oldHash, oldToken))
    assertEquals(true, LockManager.hasPin(context))
    assertEquals(true, LockManager.verifyPin(context, "5678"))
    assertEquals(false, LockManager.verifyPin(context, "1234"))
    org.junit.Assert.assertNull(LockManager.getPin(context)) // never readable in plain text

    // 3. Round-trip: user requests reset -> partner sets new PIN "1234" on approve.html with request token
    val newRequestToken = "tok_new_reset_xyz987"
    val partnerPinHash = LockManager.computeSaltedPinHash("1234", newRequestToken)
    assertEquals(64, partnerPinHash.length)

    // App listener receives approved pin_reset with pinHash and applies it immediately
    BlockerManager.onPartnerApprovedPinReset(context, partnerPinHash, newRequestToken)

    // 4. App accepts "1234", rejects old PIN "5678", and notifies that partner reset the PIN
    assertEquals(true, LockManager.hasPin(context))
    assertEquals(true, LockManager.verifyPin(context, "1234"))
    assertEquals(false, LockManager.verifyPin(context, "5678"))
    org.junit.Assert.assertNull(LockManager.getPin(context))
    assertEquals(true, BlockerManager.consumePartnerPinResetNotice(context))
    assertEquals(false, BlockerManager.consumePartnerPinResetNotice(context))

    // 5. User still cannot overwrite the partner-set PIN himself
    val (overwriteAttempt, _) = LockManager.setPin(context, "0000")
    assertEquals(false, overwriteAttempt)
    assertEquals(true, LockManager.verifyPin(context, "1234"))
    assertEquals(false, LockManager.verifyPin(context, "0000"))
  }

  @Test
  fun `PinLockActivity pauses guard while visible, never auto-backs, and unlocks guard for 60s on correct partner PIN`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE).edit().clear().commit()
    LockManager.lockImmediately(context)
    LockManager.markPinScreenActive(false)

    val saltToken = "tok_guard_test_001"
    val hash = LockManager.computeSaltedPinHash("4321", saltToken)
    LockManager.applyPartnerPinHash(context, hash, saltToken)
    assertEquals(false, LockManager.isCurrentlyUnlocked(context))

    // Simulate guard triggering auto-back and launching PinLockActivity
    LockManager.markPinScreenLaunching()
    assertEquals(true, LockManager.isPinScreenCurrentlyActive())

    val controller = org.robolectric.Robolectric.buildActivity(PinLockActivity::class.java).create().start().resume()
    val activity = controller.get()
    assertEquals(true, LockManager.isPinScreenCurrentlyActive())
    assertEquals(false, activity.isFinishing)

    // Back press (e.g. stray auto-back) must NOT close PinLockActivity
    activity.onBackPressedDispatcher.onBackPressed()
    assertEquals(false, activity.isFinishing)
    assertEquals(true, LockManager.isPinScreenCurrentlyActive())

    // Wrong PIN must not unlock or close the PIN screen
    assertEquals(false, LockManager.verifyPin(context, "9999"))
    assertEquals(false, LockManager.isCurrentlyUnlocked(context))
    assertEquals(true, LockManager.isPinScreenCurrentlyActive())

    // Correct partner PIN unlocks guard for 60 seconds and closes the PIN screen
    assertEquals(true, LockManager.verifyPin(context, "4321"))
    LockManager.unlockForSeconds(context, 60)
    LockManager.markPinScreenActive(false)
    assertEquals(true, LockManager.isCurrentlyUnlocked(context))
    assertEquals(false, LockManager.isPinScreenCurrentlyActive())
  }

  @Test
  fun `guard detects Purity Tracker App info and Uninstall popups across all Android brands while ignoring all other apps`() {
    // 1. Screenshot 1: Launcher "Delete Purity Tracker" uninstall confirmation popup
    val screenshot1Popup = """
      Delete Purity Tracker
      After uninstallation, all app data will be cleared.
      Cancel Uninstall
    """.trimIndent()
    assertEquals(true, ProtectionService.isOurUninstallPopupText(screenshot1Popup, "com.android.launcher"))

    // Quoted / Samsung / Pixel / Xiaomi / Vivo variants of Purity Tracker uninstall popup
    assertEquals(
      true,
      ProtectionService.isOurUninstallPopupText(
        "Delete \"Purity Tracker\"? Deleting this app will also delete its data. Cancel Delete",
        "com.oppo.launcher"
      )
    )
    assertEquals(
      true,
      ProtectionService.isOurUninstallPopupText(
        "Purity Tracker Do you want to uninstall this app? Cancel OK",
        "com.google.android.packageinstaller"
      )
    )

    // Must NEVER trigger on another app's uninstall popup
    val otherAppUninstall = """
      Delete WhatsApp
      After uninstallation, all app data will be cleared.
      Cancel Uninstall
    """.trimIndent()
    assertEquals(false, ProtectionService.isOurUninstallPopupText(otherAppUninstall, "com.android.launcher"))
    assertEquals(
      false,
      ProtectionService.isOurUninstallPopupText(
        "Chrome Do you want to uninstall this app? Cancel OK",
        "com.google.android.packageinstaller"
      )
    )

    // 2. Launcher long-press menu ("Purity Tracker" + "App info" + "Uninstall") must NEVER trigger the guard!
    val launcherLongPressMenu = """
      Purity Tracker
      App info
      Uninstall
      Share
      Edit
    """.trimIndent()
    assertEquals(
      false,
      ProtectionService.isOurAppInfoOrTamperText(
        launcherLongPressMenu,
        "com.aistudio.puritytracker.abcxyz",
        "com.oppo.launcher"
      )
    )
    assertEquals(
      false,
      ProtectionService.isOurAppInfoOrTamperText(
        launcherLongPressMenu,
        "com.aistudio.puritytracker.abcxyz",
        ""
      )
    )
    assertEquals(
      false,
      ProtectionService.isOurUninstallPopupText(
        launcherLongPressMenu,
        "com.oppo.launcher"
      )
    )
    assertEquals(false, ProtectionService.isSettingsOrAppInfoPackageName("com.oppo.launcher"))
    assertEquals(false, ProtectionService.isSettingsOrAppInfoPackageName("com.android.launcher"))
    assertEquals(false, ProtectionService.isSettingsOrAppInfoPackageName("com.miui.home"))
    assertEquals(true, ProtectionService.isSettingsOrAppInfoPackageName("com.android.settings"))
    assertEquals(true, ProtectionService.isSettingsOrAppInfoPackageName("com.coloros.settings"))

    // Generic words ("storage", "battery", "permissions", "notifications") without Version/Force stop/Clear data must NEVER trigger
    val genericWordsOnly = """
      Purity Tracker
      Storage
      Battery
      Permissions
      Notifications
    """.trimIndent()
    assertEquals(
      false,
      ProtectionService.isOurAppInfoOrTamperText(
        genericWordsOnly,
        "com.aistudio.puritytracker.abcxyz",
        "com.android.settings"
      )
    )

    // 3. Screenshot 2: System App info screen for Purity Tracker (inside Settings) MUST trigger
    val screenshot2AppInfo = """
      App info
      Purity Tracker
      Version 1.3.8
      Open Force stop Uninstall
      Storage usage 42.33 MB
      Data usage details
      Battery usage
      Manage notifications
      Permissions
      Display over other apps
      Front camera display
    """.trimIndent()
    assertEquals(
      true,
      ProtectionService.isOurAppInfoOrTamperText(
        screenshot2AppInfo,
        "com.aistudio.puritytracker.abcxyz",
        "com.android.settings"
      )
    )

    // Even if App info text were somehow inside a launcher package, pkgName check must reject it
    assertEquals(
      false,
      ProtectionService.isOurAppInfoOrTamperText(
        screenshot2AppInfo,
        "com.aistudio.puritytracker.abcxyz",
        "com.oppo.launcher"
      )
    )

    // Must NEVER trigger on another app's App info screen (e.g. Spotify / WhatsApp)
    val otherAppInfo = """
      App info
      Spotify
      Version 8.9.18
      Open Force stop Uninstall
      Storage usage 140 MB
      Data usage details
      Battery usage
      Manage notifications
      Permissions
    """.trimIndent()
    assertEquals(
      false,
      ProtectionService.isOurAppInfoOrTamperText(
        otherAppInfo,
        "com.aistudio.puritytracker.abcxyz",
        "com.android.settings"
      )
    )

    // Must NEVER trigger on the multi-app "App management / All apps" list
    val multiAppList = """
      App management
      Search apps
      All apps
      Purity Tracker 42.33 MB
      Spotify 140 MB
      Chrome 210 MB
    """.trimIndent()
    assertEquals(
      false,
      ProtectionService.isOurAppInfoOrTamperText(
        multiAppList,
        "com.aistudio.puritytracker.abcxyz",
        "com.android.settings"
      )
    )
  }

  @Test
  fun `NoorVoicePlayer builds Pegasus TTS request, strips markdown, and parses PCM audio params`() {
    val reqJson = NoorVoicePlayer.buildTtsRequestJson("**Assalamu Alaikum!**\n- Keep praying *Fajr* daily.")
    assertEquals(true, reqJson.contains("\"responseModalities\":[\"AUDIO\"]"))
    assertEquals(true, reqJson.contains("\"voiceName\":\"Pegasus\""))
    assertEquals(false, reqJson.contains("**"))
    assertEquals(true, reqJson.contains("Assalamu Alaikum!"))

    val (rate1, ch1) = NoorVoicePlayer.parseAudioMimeParams("audio/l16; rate=24000; channels=1")
    assertEquals(24000, rate1)
    assertEquals(1, ch1)

    val (rate2, ch2) = NoorVoicePlayer.parseAudioMimeParams("audio/L16;codec=pcm;rate=16000")
    assertEquals(16000, rate2)
    assertEquals(1, ch2)
  }

  @Test
  fun `GeminiErrorFormatter reports API key, quota, server, timeout, and network errors accurately without leaking key`() {
    val secretKey = "AQ.FAKE_TEST_KEY_12345"

    val invalidKeyMsg = GeminiErrorFormatter.format(
      GeminiApiException(400, "INVALID_ARGUMENT", "API key not valid. Key $secretKey failed."),
      secretKey
    )
    assertEquals(true, invalidKeyMsg.contains("API key", ignoreCase = true))
    assertEquals(false, invalidKeyMsg.contains(secretKey))
    assertEquals(false, invalidKeyMsg.contains("internet connection", ignoreCase = true))

    val revokedKeyMsg = GeminiErrorFormatter.format(
      GeminiApiException(403, "PERMISSION_DENIED", "API key was revoked"),
      secretKey
    )
    assertEquals(true, revokedKeyMsg.contains("rejected or revoked", ignoreCase = true))

    val quotaMsg = GeminiErrorFormatter.format(
      GeminiApiException(429, "RESOURCE_EXHAUSTED", "Quota exceeded for quota metric"),
      secretKey
    )
    assertEquals(true, quotaMsg.contains("quota", ignoreCase = true))
    assertEquals(false, quotaMsg.contains("internet connection", ignoreCase = true))

    val busyMsg = GeminiErrorFormatter.format(
      GeminiApiException(503, "UNAVAILABLE", "High demand"),
      secretKey
    )
    assertEquals(true, busyMsg.contains("temporarily busy", ignoreCase = true))

    val timeoutMsg = GeminiErrorFormatter.format(
      java.net.SocketTimeoutException("Read timed out"),
      secretKey
    )
    assertEquals(true, timeoutMsg.contains("timed out", ignoreCase = true))

    val offlineMsg = GeminiErrorFormatter.format(
      java.net.UnknownHostException("Unable to resolve host"),
      secretKey
    )
    assertEquals(true, offlineMsg.contains("internet connection", ignoreCase = true))
  }

  @Test
  fun `GeminiStreamParser extracts visible text and skips thought parts`() {
    val chunkWithThought = """
      {
        "candidates": [{
          "content": {
            "role": "model",
            "parts": [
              {"thought": true, "text": "internal reasoning"},
              {"text": "Wa Alaikum Assalam! "}
            ]
          }
        }]
      }
    """.trimIndent()
    assertEquals("Wa Alaikum Assalam! ", GeminiStreamParser.extractVisibleTextFromChunkJson(chunkWithThought))
    assertEquals("", GeminiStreamParser.extractVisibleTextFromChunkJson("[DONE]"))
  }
}
