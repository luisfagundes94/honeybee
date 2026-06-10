package com.luisfagundes.library.impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import com.luisfagundes.library.impl.presentation.components.TrashBadgedBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.luisfagundes.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.library.impl.domain.model.Media
import com.luisfagundes.library.impl.domain.model.MediaSection
import com.luisfagundes.library.impl.presentation.effect.LibraryUiEffect
import com.luisfagundes.library.impl.presentation.event.LibraryUiEvent
import com.luisfagundes.library.impl.presentation.state.LibraryUiState
import com.luisfagundes.library.impl.presentation.tools.getFormattedMonthName
import com.luisfagundes.library.impl.presentation.viewmodel.LibraryViewModel
import java.time.YearMonth

@Composable
internal fun LibraryScreen(
    onNavigateToMediaDetail: (Long) -> Unit,
    onNavigateToTrash: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            is LibraryUiEffect.NavigateToMediaDetail -> onNavigateToMediaDetail(effect.mediaId)
            LibraryUiEffect.NavigateToTrash -> onNavigateToTrash()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatchEvent(LibraryUiEvent.LoadMedia)
    }

    LibraryScreen(
        uiState = uiState,
        onEvent = viewModel::dispatchEvent
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LibraryScreen(
    uiState: LibraryUiState,
    onEvent: (LibraryUiEvent) -> Unit
) {
    when (uiState) {
        is LibraryUiState.Loading -> HoneybeeLoadingTemplate(
            modifier = Modifier.fillMaxSize()
        )

        is LibraryUiState.Error -> HoneybeeErrorTemplate(
            message = uiState.message,
            onRetry = { onEvent(LibraryUiEvent.LoadMedia) }
        )

        is LibraryUiState.Content -> LibraryContent(
            mediaSectionList = uiState.mediaSectionList,
            itemsInTrash = uiState.itemsInTrash,
            onEvent = onEvent,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryContent(
    mediaSectionList: List<MediaSection>,
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
                title = {
                    Text(
                        text = stringResource(R.string.library),
                        modifier = Modifier.semantics { heading() }
                    )
                },
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
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding(),
                start = MaterialTheme.spacing.default,
                end = MaterialTheme.spacing.default
            ),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.verySmall),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.verySmall)
        ) {
            mediaSectionList.forEach { mediaSection ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val month = mediaSection.yearMonth.getFormattedMonthName()
                    val year = mediaSection.yearMonth.year

                    Text(
                        text = "$month $year",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
                    )
                }
                items(mediaSection.mediaList) { media ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onEvent(LibraryUiEvent.MediaClick(media.id)) }
                    ) {
                        AsyncImage(
                            model = media.uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (media.isVideo) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(MaterialTheme.spacing.verySmall)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
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
private fun LibraryContentContentPreview() {
    LibraryContent(
        mediaSectionList = listOf(
            MediaSection(
                yearMonth = YearMonth.of(2026, 6),
                mediaList = listOf(
                    Media(id = 1L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L, isVideo = false),
                    Media(id = 2L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L, isVideo = true),
                    Media(id = 3L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L, isVideo = false),
                )
            ),
            MediaSection(
                yearMonth = YearMonth.of(2026, 5),
                mediaList = listOf(
                    Media(id = 4L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L, isVideo = true),
                    Media(id = 5L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L, isVideo = false),
                )
            )
        ),
        itemsInTrash = 3,
        onEvent = {},
        modifier = Modifier.fillMaxWidth()
    )
}

