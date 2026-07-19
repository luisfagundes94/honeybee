package com.luisfagundes.library.impl.presentation.provider

import android.net.Uri
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.impl.presentation.state.MediaDetailsUiState

internal class MediaDetailsUiStateProvider : PreviewParameterProvider<MediaDetailsUiState> {
    override val values = sequenceOf(
        MediaDetailsUiState.Loading,
        MediaDetailsUiState.Error,
        MediaDetailsUiState.Content(
            mediaList = listOf(
                Media(id = 1L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L, isVideo = false),
                Media(id = 2L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L, isVideo = true),
                Media(id = 3L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L, isVideo = false)
            ),
            initialIndex = 0,
            trashCount = 2,
            favoriteMediaIds = emptySet()
        )
    )
}
