package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AyraActionType
import com.example.ai.LocalRuleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Ayra", appName)
  }

  @Test
  fun `local rule engine recognizes voice commands`() {
    val flashlightCmd = LocalRuleEngine.matchLocalIntent("Hey Ayra, turn on flashlight")
    assertNotNull(flashlightCmd)
    assertEquals(AyraActionType.TOGGLE_FLASHLIGHT, flashlightCmd?.actionType)
    assertEquals("on", flashlightCmd?.target)

    val bengaliTorch = LocalRuleEngine.matchLocalIntent("টর্চ জ্বালাও")
    assertNotNull(bengaliTorch)
    assertEquals(AyraActionType.TOGGLE_FLASHLIGHT, bengaliTorch?.actionType)

    val stopCmd = LocalRuleEngine.matchLocalIntent("stop listening")
    assertNotNull(stopCmd)
    assertEquals(AyraActionType.STOP_LISTENING, stopCmd?.actionType)

    val openApp = LocalRuleEngine.matchLocalIntent("open youtube")
    assertNotNull(openApp)
    assertEquals(AyraActionType.OPEN_APP, openApp?.actionType)
  }
}
