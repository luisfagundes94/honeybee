package com.luisfagundes.config.impl.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState

internal data class FeedbackUiState(
    val feedbackText: String = "",
    val isSubmitButtonEnabled: Boolean = false
) : UiState
