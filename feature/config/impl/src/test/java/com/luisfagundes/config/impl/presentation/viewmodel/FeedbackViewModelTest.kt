package com.luisfagundes.config.impl.presentation.viewmodel

import app.cash.turbine.test
import com.luisfagundes.config.impl.presentation.effect.FeedbackUiEffect
import com.luisfagundes.config.impl.presentation.event.FeedbackUiEvent
import com.luisfagundes.config.impl.presentation.state.FeedbackUiState
import com.luisfagundes.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class FeedbackViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private lateinit var viewModel: FeedbackViewModel

    @BeforeEach
    fun setUp() {
        viewModel = FeedbackViewModel()
    }

    @Test
    fun `dispatchEvent UpdateFeedbackText should update feedbackText and enable submit button when not blank`() = runTest {
        viewModel.uiState.test {
            assertEquals(FeedbackUiState(), awaitItem())

            // When
            viewModel.dispatchEvent(FeedbackUiEvent.UpdateFeedbackText("Great app!"))

            // Then
            assertEquals(FeedbackUiState(feedbackText = "Great app!", isSubmitButtonEnabled = true), awaitItem())

            // When
            viewModel.dispatchEvent(FeedbackUiEvent.UpdateFeedbackText(""))

            // Then
            assertEquals(FeedbackUiState(), awaitItem())
        }
    }

    @Test
    fun `dispatchEvent BackClick should send NavigateBack effect`() = runTest {
        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(FeedbackUiEvent.BackClick)

            // Then
            assertEquals(FeedbackUiEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `dispatchEvent SubmitFeedback with blank text should not dispatch any effects`() = runTest {
        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(FeedbackUiEvent.SubmitFeedback)

            // Then
            expectNoEvents()
        }
    }

    @Test
    fun `dispatchEvent SubmitFeedback with valid text should dispatch OpenEmailClient and NavigateBack effects`() = runTest {
        // Given
        val feedbackText = "This app is wonderful!"
        viewModel.dispatchEvent(FeedbackUiEvent.UpdateFeedbackText(feedbackText))

        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(FeedbackUiEvent.SubmitFeedback)

            // Then
            assertEquals(FeedbackUiEffect.OpenEmailClient(feedbackText), awaitItem())
            assertEquals(FeedbackUiEffect.NavigateBack, awaitItem())
        }
    }
}
