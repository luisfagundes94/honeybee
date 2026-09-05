package com.luisfagundes.library.impl.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.core.designsystem.components.HoneybeeErrorTemplate
import com.luisfagundes.core.designsystem.components.HoneybeeLoadingTemplate
import com.luisfagundes.core.designsystem.components.MediaThumbnail
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.api.domain.model.MediaSection
import com.luisfagundes.library.impl.R
import com.luisfagundes.library.impl.presentation.components.TrashBadgedBox
import com.luisfagundes.library.impl.presentation.effect.LibraryUiEffect
import com.luisfagundes.library.impl.presentation.event.LibraryUiEvent
import com.luisfagundes.library.impl.presentation.provider.LibraryUiStateProvider
import com.luisfagundes.library.impl.presentation.state.LibraryUiState
import com.luisfagundes.library.impl.presentation.tools.getFormattedMonthName
import com.luisfagundes.library.impl.presentation.viewmodel.LibraryViewModel
import com.luisfagundes.core.designsystem.R as DesignSystemResources

@Composable
internal fun LibraryScreen(
    onNavigateToMediaDetail: (Long) -> Unit,
    onNavigateToTrash: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.dispatchEvent(LibraryUiEvent.LoadMedia)
    }

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            is LibraryUiEffect.NavigateToMediaDetail -> onNavigateToMediaDetail(effect.mediaId)
            LibraryUiEffect.NavigateToTrash -> onNavigateToTrash()
        }
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
        is LibraryUiState.Loading -> HoneybeeLoadingTemplate()

        is LibraryUiState.Error -> HoneybeeErrorTemplate(
            title = stringResource(R.string.error_loading_media_title),
            description = stringResource(R.string.error_loading_media_description),
            primaryButtonLabel = stringResource(DesignSystemResources.string.retry),
            onPrimaryButtonClick = { onEvent(LibraryUiEvent.LoadMedia) },
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
        if (mediaSectionList.isEmpty()) {
            LibraryEmptyContent(innerPadding)
        } else {
            LibraryMediaGrid(
                mediaSectionList = mediaSectionList,
                innerPadding = innerPadding,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun LibraryEmptyContent(innerPadding: PaddingValues) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Text(
            text = stringResource(R.string.library_is_empty),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun LibraryMediaGrid(
    mediaSectionList: List<MediaSection>,
    innerPadding: PaddingValues,
    onEvent: (LibraryUiEvent) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = MaterialTheme.spacing.mediaTileMin),
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
            item(
                key = "header_${mediaSection.yearMonth}",
                span = { GridItemSpan(maxLineSpan) },
                contentType = "header"
            ) {
                Text(
                    text = stringResource(
                        R.string.media_section_header,
                        mediaSection.yearMonth.getFormattedMonthName(),
                        mediaSection.yearMonth.year
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(vertical = MaterialTheme.spacing.small)
                        .semantics { heading() }
                )
            }
            items(
                items = mediaSection.mediaList,
                key = { media -> media.id },
                contentType = { "media" }
            ) { media ->
                MediaGridItem(
                    media = media,
                    onClick = { onEvent(LibraryUiEvent.MediaClick(media.id)) }
                )
            }
        }
    }
}

@Composable
private fun MediaGridItem(
    media: Media,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mediaContentDescription = stringResource(
        if (media.isVideo) R.string.open_video else R.string.open_photo
    )

    MediaThumbnail(
        uri = media.uri,
        isVideo = media.isVideo,
        durationMillis = media.durationMillis,
        contentDescription = mediaContentDescription,
        onClick = onClick,
        modifier = modifier
    )
}

@PreviewLightDark
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun LibraryScreenPreview(
    @PreviewParameter(LibraryUiStateProvider::class) uiState: LibraryUiState
) {
    LibraryScreen(
        uiState = uiState,
        onEvent = {}
    )
}

