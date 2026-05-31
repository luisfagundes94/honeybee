package com.luisfagundes.library.impl.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState
import com.luisfagundes.library.impl.domain.model.Photo

internal sealed interface TrashUiState : UiState {
    data object Loading : TrashUiState
    data class Error(val message: String) : TrashUiState
    data class Content(
        val deletePhotos: List<Photo>,
        val keepPhotos: List<Photo>
    ) : TrashUiState
}
