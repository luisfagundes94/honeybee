package com.luisfagundes.albums.impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.luisfagundes.core.designsystem.components.HoneybeeErrorTemplate
import com.luisfagundes.core.designsystem.components.HoneybeeLoadingTemplate
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.core.designsystem.R.string.video_content_description
import com.luisfagundes.core.designsystem.R.string.retry
import com.luisfagundes.core.designsystem.R.string.cancel
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.albums.impl.R
import com.luisfagundes.albums.impl.presentation.effect.AlbumDetailsUiEffect
import com.luisfagundes.albums.impl.presentation.event.AlbumDetailsUiEvent
import com.luisfagundes.albums.impl.presentation.provider.AlbumDetailsUiStateProvider
import com.luisfagundes.albums.impl.presentation.state.AlbumDetailsUiState
import com.luisfagundes.albums.impl.presentation.viewmodel.AlbumDetailsViewModel

@Composable
internal fun AlbumDetailsScreen(
    albumId: String,
    albumName: String,
    onNavigateBack: () -> Unit,
    onNavigateToMediaDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            AlbumDetailsUiEffect.NavigateBack -> onNavigateBack()
            is AlbumDetailsUiEffect.NavigateToMediaDetail -> onNavigateToMediaDetail(effect.mediaId)
        }
    }

    LaunchedEffect(albumId) {
        viewModel.dispatchEvent(AlbumDetailsUiEvent.LoadMedia(albumId))
    }

    AlbumDetailsScreen(
        albumName = albumName,
        uiState = uiState,
        onEvent = viewModel::dispatchEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AlbumDetailsScreen(
    albumName: String,
    uiState: AlbumDetailsUiState,
    onEvent: (AlbumDetailsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = albumName,
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onEvent(AlbumDetailsUiEvent.BackClick)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        when (uiState) {
            is AlbumDetailsUiState.Loading -> {
                HoneybeeLoadingTemplate(
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is AlbumDetailsUiState.Error -> {
                HoneybeeErrorTemplate(
                    message = stringResource(R.string.error_loading_album_media),
                    primaryButtonLabel = stringResource(retry),
                    onPrimaryButtonClick = { onEvent(AlbumDetailsUiEvent.Retry) },
                    secondaryButtonLabel = stringResource(cancel),
                    onSecondaryButtonClick = { onEvent(AlbumDetailsUiEvent.CancelClick) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is AlbumDetailsUiState.Content -> {
                AlbumDetailsContent(
                    uiState = uiState,
                    innerPadding = innerPadding,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun AlbumDetailsContent(
    uiState: AlbumDetailsUiState.Content,
    innerPadding: PaddingValues,
    onEvent: (AlbumDetailsUiEvent) -> Unit
) {
    if (uiState.mediaList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_media),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
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
            items(uiState.mediaList, key = { it.id }) { media ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            onEvent(AlbumDetailsUiEvent.MediaClick(media.id))
                        }
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
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .padding(MaterialTheme.spacing.verySmall)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = stringResource(video_content_description),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(MaterialTheme.spacing.default)
                            )
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun AlbumDetailsScreenPreview(
    @PreviewParameter(AlbumDetailsUiStateProvider::class) uiState: AlbumDetailsUiState
) {
    AlbumDetailsScreen(
        albumName = "Camera",
        uiState = uiState,
        onEvent = {}
    )
}
