package com.luisfagundes.config.impl.presentation.provider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.luisfagundes.config.impl.presentation.state.StatisticsUiState
import com.luisfagundes.library.api.domain.model.Statistics

internal class StatisticsUiStateProvider : PreviewParameterProvider<StatisticsUiState> {
    override val values = sequenceOf(
        StatisticsUiState.Loading,
        StatisticsUiState.Error,
        StatisticsUiState.Content(
            statistics = Statistics(
                memoryCleared = 120 * 1024 * 1024L,
                mediaDeleted = 42,
                photosDeleted = 30,
                videosDeleted = 12
            )
        )
    )
}
