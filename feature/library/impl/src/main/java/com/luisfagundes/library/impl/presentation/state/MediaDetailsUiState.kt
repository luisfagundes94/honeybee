package com.luisfagundes.library.impl.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState
import com.luisfagundes.library.impl.domain.model.Photo

internal sealed interface MediaDetailsUiState : UiState {
    data object Loading : MediaDetailsUiState
    data class Error(val message: String) : MediaDetailsUiState
    data class Content(
        val photos: List<Photo>,
        val initialIndex: Int,
        val trashCount: Int,
        val favoritePhotoIds: Set<Long> = emptySet()
    ) : MediaDetailsUiState
}
