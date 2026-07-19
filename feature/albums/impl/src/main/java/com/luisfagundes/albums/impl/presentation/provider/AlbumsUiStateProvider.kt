package com.luisfagundes.albums.impl.presentation.provider

import android.net.Uri
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.presentation.state.AlbumsUiState

internal class AlbumsUiStateProvider : PreviewParameterProvider<AlbumsUiState> {
    override val values = sequenceOf(
        AlbumsUiState.Loading,
        AlbumsUiState.Error,
        AlbumsUiState.Content(albums = emptyList()),
        AlbumsUiState.Content(albums = albums)
    )
}

private val albums = listOf(
    Album.Virtual.Favorites(count = 12, coverUri = Uri.EMPTY, isVideo = false),
    Album.Virtual.Videos(count = 8, coverUri = Uri.EMPTY, isVideo = true),
    Album.Physical(
        id = "camera",
        name = "Camera",
        count = 24,
        coverUri = Uri.EMPTY,
        isVideo = false
    )
)
