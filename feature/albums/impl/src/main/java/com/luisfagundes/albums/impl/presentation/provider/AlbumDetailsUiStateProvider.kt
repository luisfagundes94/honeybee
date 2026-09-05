package com.luisfagundes.albums.impl.presentation.provider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.luisfagundes.albums.impl.presentation.state.AlbumDetailsUiState
import com.luisfagundes.library.api.domain.model.Media

internal class AlbumDetailsUiStateProvider : PreviewParameterProvider<AlbumDetailsUiState> {
    override val values = sequenceOf(
        AlbumDetailsUiState.Loading,
        AlbumDetailsUiState.Error,
        AlbumDetailsUiState.Content(mediaList = emptyList()),
        AlbumDetailsUiState.Content(
            mediaList = listOf(
                Media(id = 1L, uri = "", dateAdded = 0L, size = 0L, isVideo = false),
                Media(
                    id = 2L,
                    uri = "",
                    dateAdded = 0L,
                    size = 0L,
                    durationMillis = 65_000L,
                    isVideo = true
                ),
                Media(id = 3L, uri = "", dateAdded = 0L, size = 0L, isVideo = false)
            )
        )
    )
}
