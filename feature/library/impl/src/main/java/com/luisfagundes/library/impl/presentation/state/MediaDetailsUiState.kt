package com.luisfagundes.library.impl.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState
import com.luisfagundes.library.api.domain.model.Media

internal sealed interface MediaDetailsUiState : UiState {
    data object Loading : MediaDetailsUiState
    data object Error : MediaDetailsUiState
    data class Content(
        val mediaList: List<Media>,
        val initialIndex: Int,
        val trashCount: Int,
        val favoriteMediaIds: Set<Long> = emptySet()
    ) : MediaDetailsUiState
}
