package com.luisfagundes.config.impl.presentation.viewmodel

import com.luisfagundes.config.impl.presentation.effect.FeedbackUiEffect
import com.luisfagundes.config.impl.presentation.event.FeedbackUiEvent
import com.luisfagundes.config.impl.presentation.state.FeedbackUiState
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class FeedbackViewModel @Inject constructor() :
    ViewModel<FeedbackUiState, FeedbackUiEvent, FeedbackUiEffect>(FeedbackUiState()) {
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
        val feedbackText = getCurrentState().feedbackText
        if (feedbackText.isBlank()) return

        sendEffect { FeedbackUiEffect.OpenEmailClient(feedbackText) }
        sendEffect { FeedbackUiEffect.NavigateBack }
    }
}
