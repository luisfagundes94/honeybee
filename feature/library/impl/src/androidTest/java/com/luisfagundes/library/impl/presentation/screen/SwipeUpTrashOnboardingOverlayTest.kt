package com.luisfagundes.library.impl.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.luisfagundes.core.designsystem.theme.HoneybeeTheme
import com.luisfagundes.library.impl.R
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SwipeUpTrashOnboardingOverlayTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tappingOverlayDismissesItWithoutInvokingUnderlyingAction() {
        val overlayVisible = mutableStateOf(true)
        val underlyingActionInvoked = mutableStateOf(false)

        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            HoneybeeTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { underlyingActionInvoked.value = true }
                    )
                    if (overlayVisible.value) {
                        SwipeUpTrashOnboardingOverlay(
                            onDismiss = { overlayVisible.value = false }
                        )
                    }
                }
            }
        }

        composeRule
            .onNodeWithContentDescription(
                InstrumentationRegistry.getInstrumentation()
                    .targetContext
                    .getString(R.string.swipe_up_trash_onboarding_description)
            )
            .performTouchInput { click() }

        composeRule.runOnIdle {
            assertFalse(overlayVisible.value)
            assertFalse(underlyingActionInvoked.value)
        }
    }
}
