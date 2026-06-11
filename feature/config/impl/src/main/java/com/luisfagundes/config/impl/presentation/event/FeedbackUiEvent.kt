package com.luisfagundes.config.impl.presentation.event

import com.luisfagundes.core.common.presentation.arch.event.UiEvent

internal sealed interface FeedbackUiEvent : UiEvent {
    data class UpdateFeedbackText(val text: String) : FeedbackUiEvent
    data object SubmitFeedback : FeedbackUiEvent
    data object BackClick : FeedbackUiEvent
}
