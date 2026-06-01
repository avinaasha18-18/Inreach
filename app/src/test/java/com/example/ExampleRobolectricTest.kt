package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.InReachApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("InReach", appName)
  }

  @Test
  fun `test MainViewModel database initialization`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = MainViewModel(app)
    assertNotNull(viewModel)
    
    // Check if profiles are populated nicely or if it throws any SQLite exceptions
    val profilesList = viewModel.profiles.value
    assertNotNull(profilesList)
  }

  @Test
  fun `test InReachApp UI rendering`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = MainViewModel(app)
    
    composeTestRule.setContent {
      InReachApp(viewModel = viewModel)
    }
    
    assertNotNull(composeTestRule.onRoot())
  }
}


