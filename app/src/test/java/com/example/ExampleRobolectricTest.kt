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
}
