package com.luisfagundes.library.impl.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState
import com.luisfagundes.library.impl.domain.model.PhotoSection

internal sealed interface LibraryUiState : UiState {
    data object Loading : LibraryUiState
    data class Error(val message: String) : LibraryUiState
    data class Content(
        val photoSectionList: List<PhotoSection>,
        val itemsInTrash: Int
    ) : LibraryUiState
}