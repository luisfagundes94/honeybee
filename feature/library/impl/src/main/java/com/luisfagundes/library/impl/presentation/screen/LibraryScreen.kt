package com.luisfagundes.library.impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.designsystem.components.HoneybeeErrorTemplate
import com.luisfagundes.designsystem.components.HoneybeeLoadingTemplate
import com.luisfagundes.designsystem.theme.spacing
import com.luisfagundes.library.impl.R
import com.luisfagundes.library.impl.domain.model.PhotoSection
import com.luisfagundes.library.impl.presentation.effect.LibraryUiEffect
import com.luisfagundes.library.impl.presentation.event.LibraryUiEvent
import com.luisfagundes.library.impl.presentation.state.LibraryUiState
import com.luisfagundes.library.impl.presentation.tools.getFormattedMonthName
import com.luisfagundes.library.impl.presentation.viewmodel.LibraryViewModel

@Composable
internal fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            is LibraryUiEffect.NavigateToPhotoDetail -> Unit
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatchEvent(LibraryUiEvent.LoadPhotos)
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
            itemsInTrash = 10,
            onEvent = onEvent,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Library(
    photoSectionList: List<PhotoSection>,
    itemsInTrash: Int,
    onEvent: (LibraryUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.library)) },
                windowInsets = WindowInsets(),
                actions = {
                    BadgedBox(
                        badge = {
                            if (itemsInTrash > 0) {
                                Badge(
                                    modifier = Modifier.offset(x = (-12).dp, y = 12.dp)
                                ) {
                                    val displayCount = if (itemsInTrash > 99) {
                                        "99+"
                                    } else {
                                        itemsInTrash.toString()
                                    }
                                    Text(text = displayCount)
                                }
                            }
                        },
                        modifier = Modifier.padding(end = MaterialTheme.spacing.default)
                    ) {
                        IconButton(
                            onClick = { onEvent(LibraryUiEvent.TrashClick) }
                        ) {
                            Icon(
                                imageVector = if (itemsInTrash > 0) {
                                    Icons.Default.Delete
                                } else {
                                    Icons.Default.DeleteOutline
                                },
                                contentDescription = stringResource(R.string.items_in_trash)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(MaterialTheme.spacing.default),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.verySmall),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.verySmall)
        ) {
            photoSectionList.forEach { photoSection ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val month = photoSection.yearMonth.getFormattedMonthName()
                    val year = photoSection.yearMonth.year

                    Text(
                        text = "$month $year",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
                    )
                }
                items(photoSection.photos) { photo ->
                    AsyncImage(
                        model = photo.uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onEvent(LibraryUiEvent.PhotoClick(photo.id)) }
                    )
                }
            }
        }
    }
}