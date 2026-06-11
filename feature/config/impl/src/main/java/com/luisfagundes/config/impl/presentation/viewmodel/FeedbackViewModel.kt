package com.luisfagundes.config.impl.presentation.viewmodel

import com.luisfagundes.config.impl.R
import com.luisfagundes.config.impl.presentation.effect.FeedbackUiEffect
import com.luisfagundes.config.impl.presentation.event.FeedbackUiEvent
import com.luisfagundes.config.impl.presentation.state.FeedbackUiState
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class FeedbackViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider
) : ViewModel<FeedbackUiState, FeedbackUiEvent, FeedbackUiEffect>(
    FeedbackUiState()
) {

    override fun dispatchEvent(event: FeedbackUiEvent) {
        when (event) {
            is FeedbackUiEvent.UpdateFeedbackText -> updateFeedbackText(event.text)
            is FeedbackUiEvent.SubmitFeedback -> submitFeedback()
            is FeedbackUiEvent.BackClick -> sendEffect { FeedbackUiEffect.NavigateBack }
        }
    }

    private fun updateFeedbackText(text: String) {
        setState { it.copy(feedbackText = text, isSubmitButtonEnabled = text.isNotBlank()) }
    }

    private fun submitFeedback() {
        if (getCurrentState().feedbackText.isBlank()) return

        // TODO: Actually submit feedback to a repository

        val successMessage = resourceProvider.getString(R.string.feedback_submitted_success)

        sendEffect { FeedbackUiEffect.ShowToast(successMessage) }
        sendEffect { FeedbackUiEffect.NavigateBack }
    }
}
