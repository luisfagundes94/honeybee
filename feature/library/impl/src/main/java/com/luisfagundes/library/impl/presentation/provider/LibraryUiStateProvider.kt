package com.luisfagundes.library.impl.presentation.provider

import android.net.Uri
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.api.domain.model.MediaSection
import com.luisfagundes.library.impl.presentation.state.LibraryUiState
import java.time.YearMonth

internal class LibraryUiStateProvider : PreviewParameterProvider<LibraryUiState> {
    override val values = sequenceOf(
        LibraryUiState.Loading,
        LibraryUiState.Error,
        LibraryUiState.Content(
            mediaSectionList = emptyList(),
            itemsInTrash = 0
        ),
        LibraryUiState.Content(
            mediaSectionList = mediaSections,
            itemsInTrash = 3
        )
    )
}

private val mediaSections = listOf(
    MediaSection(
        yearMonth = YearMonth.of(2026, 6),
        mediaList = listOf(
            Media(
                id = 1L,
                uri = Uri.EMPTY,
                dateAdded = 0L,
                size = 0L,
                isVideo = false
            ),
            Media(
                id = 2L,
                uri = Uri.EMPTY,
                dateAdded = 0L,
                size = 0L,
                durationMillis = 65_000L,
                isVideo = true
            ),
            Media(
                id = 3L,
                uri = Uri.EMPTY,
                dateAdded = 0L,
                size = 0L,
                isVideo = false
            )
        )
    ),
    MediaSection(
        yearMonth = YearMonth.of(2026, 5),
        mediaList = listOf(
            Media(
                id = 4L,
                uri = Uri.EMPTY,
                dateAdded = 0L,
                size = 0L,
                durationMillis = 3_725_000L,
                isVideo = true
            ),
            Media(
                id = 5L,
                uri = Uri.EMPTY,
                dateAdded = 0L,
                size = 0L,
                isVideo = false
            )
        )
    )
)
