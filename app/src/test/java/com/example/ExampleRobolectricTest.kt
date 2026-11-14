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
  fun `PIN cannot be overwritten alone and unlocks only after partner approval`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE).edit().clear().commit()

    val (firstSaved, _) = LockManager.setPin(context, "1234")
    assertEquals(true, firstSaved)
    assertEquals("1234", LockManager.getPin(context))

    // User cannot overwrite PIN directly
    val (secondSaved, _) = LockManager.setPin(context, "5678")
    assertEquals(false, secondSaved)
    assertEquals("1234", LockManager.getPin(context))

    // Only after partner approval clears the locked PIN can a new PIN be set
    LockManager.clearPinAfterPartnerApproval(context)
    assertEquals(false, LockManager.hasPin(context))

    val (thirdSaved, _) = LockManager.setPin(context, "5678")
    assertEquals(true, thirdSaved)
    assertEquals("5678", LockManager.getPin(context))
  }
}
