package com.luisfagundes.library.impl.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.luisfagundes.library.impl.presentation.components.TrashBadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.luisfagundes.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.library.impl.domain.model.Photo
import com.luisfagundes.library.impl.domain.model.PhotoSection
import com.luisfagundes.library.impl.presentation.effect.LibraryUiEffect
import com.luisfagundes.library.impl.presentation.event.LibraryUiEvent
import com.luisfagundes.library.impl.presentation.state.LibraryUiState
import com.luisfagundes.library.impl.presentation.tools.getFormattedMonthName
import com.luisfagundes.library.impl.presentation.viewmodel.LibraryViewModel
import java.time.YearMonth

@Composable
internal fun LibraryScreen(
    onNavigateToPhotoDetail: (Long) -> Unit,
    onNavigateToTrash: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            is LibraryUiEffect.NavigateToPhotoDetail -> onNavigateToPhotoDetail(effect.photoId)
            LibraryUiEffect.NavigateToTrash -> onNavigateToTrash()
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
            itemsInTrash = uiState.itemsInTrash,
            onEvent = onEvent,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Library(
    photoSectionList: List<PhotoSection>,
    itemsInTrash: Int,
    onEvent: (LibraryUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.library)) },
                windowInsets = WindowInsets(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    TrashBadgedBox(
                        itemsInTrash = itemsInTrash,
                        onClick = { onEvent(LibraryUiEvent.TrashClick) },
                        contentDescription = stringResource(R.string.items_in_trash),
                        modifier = Modifier.padding(end = MaterialTheme.spacing.default)
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.default),
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun LibraryContentPreview() {
    Library(
        photoSectionList = listOf(
            PhotoSection(
                yearMonth = YearMonth.of(2026, 6),
                photos = listOf(
                    Photo(id = 1L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L),
                    Photo(id = 2L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L),
                    Photo(id = 3L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L),
                )
            ),
            PhotoSection(
                yearMonth = YearMonth.of(2026, 5),
                photos = listOf(
                    Photo(id = 4L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L),
                    Photo(id = 5L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L),
                )
            )
        ),
        itemsInTrash = 3,
        onEvent = {},
        modifier = Modifier.fillMaxWidth()
    )
}

