package com.luisfagundes.albums.impl.presentation.provider

import android.net.Uri
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.luisfagundes.albums.impl.domain.model.AlbumMedia
import com.luisfagundes.albums.impl.presentation.state.AlbumDetailsUiState

internal class AlbumDetailsUiStateProvider : PreviewParameterProvider<AlbumDetailsUiState> {
    override val values = sequenceOf(
        AlbumDetailsUiState.Loading,
        AlbumDetailsUiState.Error,
        AlbumDetailsUiState.Content(mediaList = emptyList()),
        AlbumDetailsUiState.Content(
            mediaList = listOf(
                AlbumMedia(id = 1L, uri = Uri.EMPTY, dateAdded = 0L, isVideo = false),
                AlbumMedia(
                    id = 2L,
                    uri = Uri.EMPTY,
                    dateAdded = 0L,
                    durationMillis = 65_000L,
                    isVideo = true
                ),
                AlbumMedia(id = 3L, uri = Uri.EMPTY, dateAdded = 0L, isVideo = false)
            )
        )
    )
}
