package com.luisfagundes.config.impl.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState

internal sealed interface FeedbackUiState : UiState {
    val feedbackText: String
    val isSubmitButtonEnabled: Boolean

    data class Content(
        override val feedbackText: String = "",
        override val isSubmitButtonEnabled: Boolean = false,
    ) : FeedbackUiState
}
