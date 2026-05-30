package com.luisfagundes.library.impl.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.designsystem.components.HoneybeeErrorTemplate
import com.luisfagundes.designsystem.components.HoneybeeLoadingTemplate
import com.luisfagundes.designsystem.theme.spacing
import com.luisfagundes.impl.domain.model.PhotoSection
import com.luisfagundes.impl.presentation.effect.LibraryUiEffect
import com.luisfagundes.impl.presentation.event.LibraryUiEvent
import com.luisfagundes.impl.presentation.state.LibraryUiState
import com.luisfagundes.impl.presentation.tools.getFormattedMonthName
import com.luisfagundes.impl.presentation.viewmodel.LibraryViewModel

@Composable
internal fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            is LibraryUiEffect -> Unit
        }
    }

    LibraryContent(
        uiState = uiState,
        onEvent = viewModel::dispatchEvent
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LibraryContent(
    uiState: LibraryUiState,
    onEvent: (LibraryUiEvent) -> Unit
) {
    when (uiState) {
        is LibraryUiState.Loading -> HoneybeeLoadingTemplate(
            modifier = Modifier.fillMaxSize()
        )

        is LibraryUiState.Error -> HoneybeeErrorTemplate(
            message = uiState.message,
            onRetry = { onEvent(LibraryUiEvent.LoadPhotos) }
        )

        is LibraryUiState.Content -> Library(
            photoSectionList = uiState.photoSectionList,
            onEvent = onEvent,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Library(
    photoSectionList: List<PhotoSection>,
    onEvent: (LibraryUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(MaterialTheme.spacing.default),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.default),
        modifier = modifier
    ) {
        photoSectionList.forEach { photoSection ->
            item {
                val month = photoSection.yearMonth.getFormattedMonthName()
                val year = photoSection.yearMonth.year

                Text(
                    text = "$month $year",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            items(photoSection.photos) { photo ->
                AsyncImage(
                    model = photo.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onEvent(LibraryUiEvent.PhotoClick(photo.id)) }
                )
            }
        }
    }
}