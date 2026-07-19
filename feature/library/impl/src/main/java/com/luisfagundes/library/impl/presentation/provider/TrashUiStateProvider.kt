package com.luisfagundes.library.impl.presentation.provider

import android.net.Uri
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.impl.presentation.state.TrashUiState

internal class TrashUiStateProvider : PreviewParameterProvider<TrashUiState> {
    override val values = sequenceOf(
        TrashUiState.Loading,
        TrashUiState.Error,
        TrashUiState.Content(mediaToBeDeleted = emptyList()),
        TrashUiState.Content(mediaToBeDeleted = mediaToBeDeleted)
    )
}

private val mediaToBeDeleted = listOf(
    Media(id = 1L, uri = Uri.EMPTY, dateAdded = 0L, size = 1024L, isVideo = false),
    Media(id = 2L, uri = Uri.EMPTY, dateAdded = 0L, size = 2048L, isVideo = true),
    Media(id = 3L, uri = Uri.EMPTY, dateAdded = 0L, size = 4096L, isVideo = false)
)
