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
}
