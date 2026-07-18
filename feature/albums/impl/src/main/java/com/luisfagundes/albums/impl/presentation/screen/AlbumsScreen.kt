package com.luisfagundes.albums.impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.luisfagundes.albums.impl.R.string.error_loading_albums
import com.luisfagundes.albums.impl.R.string.albums
import com.luisfagundes.albums.impl.R.string.no_albums
import com.luisfagundes.albums.impl.R.string.favorites
import com.luisfagundes.albums.impl.R.string.videos
import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.presentation.effect.AlbumsUiEffect
import com.luisfagundes.albums.impl.presentation.event.AlbumsUiEvent
import com.luisfagundes.albums.impl.presentation.state.AlbumsUiState
import com.luisfagundes.albums.impl.presentation.tools.getAlbumStyle
import com.luisfagundes.albums.impl.presentation.tools.getDisplayName
import com.luisfagundes.albums.impl.presentation.tools.getCountText
import com.luisfagundes.albums.impl.presentation.viewmodel.AlbumsViewModel
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.core.designsystem.components.HoneybeeErrorTemplate
import com.luisfagundes.core.designsystem.components.HoneybeeLoadingTemplate
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.core.designsystem.R.string.retry

@Composable
internal fun AlbumsScreen(
    onNavigateToAlbumDetails: (String, String) -> Unit,
    viewModel: AlbumsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            is AlbumsUiEffect.NavigateToAlbumDetails -> onNavigateToAlbumDetails(
                effect.albumId,
                effect.albumName
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatchEvent(AlbumsUiEvent.LoadAlbums)
    }

    AlbumsScreen(
        uiState = uiState,
        onEvent = viewModel::dispatchEvent,
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AlbumsScreen(
    uiState: AlbumsUiState,
    onEvent: (AlbumsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(albums),
                        modifier = Modifier.semantics { heading() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        when (uiState) {
            is AlbumsUiState.Loading -> {
                HoneybeeLoadingTemplate(
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is AlbumsUiState.Error -> {
                HoneybeeErrorTemplate(
                    message = stringResource(error_loading_albums),
                    primaryButtonLabel = stringResource(retry),
                    onPrimaryButtonClick = { onEvent(AlbumsUiEvent.LoadAlbums) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is AlbumsUiState.Content -> {
                if (uiState.albums.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(no_albums),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding() + MaterialTheme.spacing.default,
                            bottom = innerPadding.calculateBottomPadding() + MaterialTheme.spacing.default,
                            start = MaterialTheme.spacing.default,
                            end = MaterialTheme.spacing.default
                        ),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.default),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.default),
                        modifier = Modifier
                            .fillMaxSize()
                            .consumeWindowInsets(innerPadding)
                    ) {
                        items(uiState.albums, key = { it.id }) { album ->
                            val favoritesName = stringResource(favorites)
                            val videosName = stringResource(videos)

                            AlbumCard(
                                album = album,
                                onClick = {
                                    val displayName = when (album) {
                                        is Album.Physical -> album.name
                                        is Album.Virtual.Favorites -> favoritesName
                                        is Album.Virtual.Videos -> videosName
                                    }
                                    onEvent(AlbumsUiEvent.AlbumClick(album.id, displayName))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumCard(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(album.getAlbumStyle().gradient),
                contentAlignment = Alignment.Center
            ) {
                if (album.coverUri != null) {
                    AsyncImage(
                        model = album.coverUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = album.getAlbumStyle().icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(MaterialTheme.spacing.large)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.small)
            ) {
                Text(
                    text = album.getDisplayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = album.getCountText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
