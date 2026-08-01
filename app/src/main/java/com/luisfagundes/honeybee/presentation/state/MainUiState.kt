package com.luisfagundes.honeybee.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState

internal sealed interface MainUiState : UiState {
    data object Loading : MainUiState
    data class Content(val isOnboardingCompleted: Boolean) : MainUiState
}
