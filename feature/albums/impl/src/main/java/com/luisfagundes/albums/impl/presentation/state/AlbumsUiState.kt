package com.luisfagundes.albums.impl.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState
import com.luisfagundes.albums.impl.domain.model.Album

internal sealed interface AlbumsUiState : UiState {
    data object Loading : AlbumsUiState
    data object Error : AlbumsUiState
    data class Content(val albums: List<Album>) : AlbumsUiState
}
