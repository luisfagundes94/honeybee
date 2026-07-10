package com.luisfagundes.albums.impl.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState
import com.luisfagundes.albums.impl.domain.model.AlbumMedia

internal sealed interface AlbumDetailsUiState : UiState {
    data object Loading : AlbumDetailsUiState
    data class Error(val message: String) : AlbumDetailsUiState
    data class Content(val mediaList: List<AlbumMedia>) : AlbumDetailsUiState
}
