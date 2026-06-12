package com.luisfagundes.config.impl.presentation.viewmodel

import app.cash.turbine.test
import com.luisfagundes.config.impl.presentation.effect.FeedbackUiEffect
import com.luisfagundes.config.impl.presentation.event.FeedbackUiEvent
import com.luisfagundes.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class FeedbackViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private lateinit var viewModel: FeedbackViewModel

    @BeforeEach
    fun setUp() {
        viewModel = FeedbackViewModel()
    }

    @Test
    fun `dispatchEvent UpdateFeedbackText should update feedbackText and enable submit button when not blank`() = runTest {
        // When & Then
        viewModel.uiState.test {
            // Initial state
            var state = awaitItem()
            assertEquals("", state.feedbackText)
            assertFalse(state.isSubmitButtonEnabled)

            // Update with non-blank text
            viewModel.dispatchEvent(FeedbackUiEvent.UpdateFeedbackText("Great app!"))
            state = awaitItem()
            assertEquals("Great app!", state.feedbackText)
            assertTrue(state.isSubmitButtonEnabled)

            // Update with blank text
            viewModel.dispatchEvent(FeedbackUiEvent.UpdateFeedbackText(""))
            state = awaitItem()
            assertEquals("", state.feedbackText)
            assertFalse(state.isSubmitButtonEnabled)
        }
    }

    @Test
    fun `dispatchEvent BackClick should send NavigateBack effect`() = runTest {
        // When & Then
        viewModel.uiEffect.test {
            viewModel.dispatchEvent(FeedbackUiEvent.BackClick)
            assertEquals(FeedbackUiEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `dispatchEvent SubmitFeedback with blank text should not dispatch any effects`() = runTest {
        // When & Then
        viewModel.uiEffect.test {
            viewModel.dispatchEvent(FeedbackUiEvent.SubmitFeedback)
            expectNoEvents()
        }
    }

    @Test
    fun `dispatchEvent SubmitFeedback with valid text should dispatch OpenEmailClient and NavigateBack effects`() = runTest {
        // Given
        val feedbackText = "This app is wonderful!"
        viewModel.dispatchEvent(FeedbackUiEvent.UpdateFeedbackText(feedbackText))

        // When & Then
        viewModel.uiEffect.test {
            viewModel.dispatchEvent(FeedbackUiEvent.SubmitFeedback)

            assertEquals(FeedbackUiEffect.OpenEmailClient(feedbackText), awaitItem())
            assertEquals(FeedbackUiEffect.NavigateBack, awaitItem())
        }
    }
}
