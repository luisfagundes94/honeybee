package com.luisfagundes.config.impl.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState
import com.luisfagundes.library.api.domain.model.Statistics

internal sealed interface StatisticsUiState : UiState {
    data object Loading : StatisticsUiState
    data class Content(val statistics: Statistics) : StatisticsUiState
    data class Error(val message: String) : StatisticsUiState
}
