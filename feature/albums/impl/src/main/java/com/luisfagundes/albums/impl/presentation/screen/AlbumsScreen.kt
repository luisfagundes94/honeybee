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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.luisfagundes.albums.impl.R
import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.presentation.effect.AlbumsUiEffect
import com.luisfagundes.albums.impl.presentation.event.AlbumsUiEvent
import com.luisfagundes.albums.impl.presentation.state.AlbumsUiState
import com.luisfagundes.albums.impl.presentation.viewmodel.AlbumsViewModel
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.designsystem.components.HoneybeeErrorTemplate
import com.luisfagundes.designsystem.components.HoneybeeLoadingTemplate
import com.luisfagundes.designsystem.theme.spacing

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
                        text = stringResource(R.string.albums),
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            is AlbumsUiState.Error -> {
                HoneybeeErrorTemplate(
                    message = uiState.message,
                    onRetry = { onEvent(AlbumsUiEvent.LoadAlbums) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
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
                            text = stringResource(R.string.no_albums),
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
                            AlbumCard(
                                album = album,
                                onClick = {
                                    val displayName = when (album.id) {
                                        "favorites" -> "Favorites"
                                        "videos" -> "Videos"
                                        else -> album.name
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
    val style = getAlbumStyle(album.name)
    val displayName = when (album.id) {
        "favorites" -> stringResource(R.string.favorites)
        "videos" -> stringResource(R.string.videos)
        else -> album.name
    }
    val countText = when (album.count) {
        0 -> stringResource(R.string.items_count_zero)
        1 -> stringResource(R.string.items_count_one)
        else -> stringResource(R.string.items_count_many, album.count)
    }

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
                    .background(style.gradient),
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
                        imageVector = style.icon,
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
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = countText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class AlbumStyle(
    val icon: ImageVector,
    val gradient: Brush
)

private fun getAlbumStyle(name: String): AlbumStyle {
    val lowerName = name.lowercase()
    return when {
        lowerName.contains("camera") -> AlbumStyle(
            icon = Icons.Default.Camera,
            gradient = Brush.linearGradient(listOf(Color(0xFFFF8A80), Color(0xFFFF5252)))
        )
        lowerName.contains("favorite") -> AlbumStyle(
            icon = Icons.Default.Favorite,
            gradient = Brush.linearGradient(listOf(Color(0xFFFF80AB), Color(0xFFFF4081)))
        )
        lowerName.contains("screenshot") -> AlbumStyle(
            icon = Icons.Default.Image,
            gradient = Brush.linearGradient(listOf(Color(0xFF82B1FF), Color(0xFF448AFF)))
        )
        lowerName.contains("video") -> AlbumStyle(
            icon = Icons.Default.VideoLibrary,
            gradient = Brush.linearGradient(listOf(Color(0xFFB388FF), Color(0xFF7C4DFF)))
        )
        lowerName.contains("download") -> AlbumStyle(
            icon = Icons.Default.Folder,
            gradient = Brush.linearGradient(listOf(Color(0xFF84FFFF), Color(0xFF18FFFF)))
        )
        lowerName.contains("whatsapp") -> AlbumStyle(
            icon = Icons.Default.Folder,
            gradient = Brush.linearGradient(listOf(Color(0xFFB9F6CA), Color(0xFF69F0AE)))
        )
        else -> AlbumStyle(
            icon = Icons.Default.Folder,
            gradient = Brush.linearGradient(listOf(Color(0xFFCFD8DC), Color(0xFF90A4AE)))
        )
    }
}
