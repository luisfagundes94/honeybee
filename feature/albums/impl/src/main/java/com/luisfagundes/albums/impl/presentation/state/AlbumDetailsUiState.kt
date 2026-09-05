package com.luisfagundes.albums.impl.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState
import com.luisfagundes.library.api.domain.model.Media

internal sealed interface AlbumDetailsUiState : UiState {
    data object Loading : AlbumDetailsUiState
    data object Error : AlbumDetailsUiState
    data class Content(val mediaList: List<Media>) : AlbumDetailsUiState
}
