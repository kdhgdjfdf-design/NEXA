package com.example

import android.content.Context
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
    assertEquals("NEXA", appName)
  }

  @Test
  fun `nexa branding persists across ar en fr strings`() {
    assertEquals("NEXA", com.example.domain.i18n.NexaStrings.forLang(com.example.domain.i18n.NexaLang.AR).appName)
    assertEquals("NEXA", com.example.domain.i18n.NexaStrings.forLang(com.example.domain.i18n.NexaLang.EN).appName)
    assertEquals("NEXA", com.example.domain.i18n.NexaStrings.forLang(com.example.domain.i18n.NexaLang.FR).appName)
  }
}
