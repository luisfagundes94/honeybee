package com.luisfagundes.albums.impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luisfagundes.designsystem.theme.spacing
import com.luisfagundes.albums.impl.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlbumsScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
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
        val albums = listOf(
            AlbumItem(
                "Camera",
                1240,
                Icons.Default.Camera,
                Brush.linearGradient(listOf(Color(0xFFFF8A80), Color(0xFFFF5252)))
            ),
            AlbumItem(
                "Favorites",
                245,
                Icons.Default.Favorite,
                Brush.linearGradient(listOf(Color(0xFFFF80AB), Color(0xFFFF4081)))
            ),
            AlbumItem(
                "Screenshots",
                87,
                Icons.Default.Image,
                Brush.linearGradient(listOf(Color(0xFF82B1FF), Color(0xFF448AFF)))
            ),
            AlbumItem(
                "Videos",
                312,
                Icons.Default.VideoLibrary,
                Brush.linearGradient(listOf(Color(0xFFB388FF), Color(0xFF7C4DFF)))
            ),
            AlbumItem(
                "Downloads",
                143,
                Icons.Default.Folder,
                Brush.linearGradient(listOf(Color(0xFF84FFFF), Color(0xFF18FFFF)))
            ),
            AlbumItem(
                "WhatsApp",
                622,
                Icons.Default.Folder,
                Brush.linearGradient(listOf(Color(0xFFB9F6CA), Color(0xFF69F0AE)))
            )
        )

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
            modifier = Modifier.fillMaxSize()
        ) {
            items(albums) { album ->
                AlbumCard(album = album)
            }
        }
    }
}

@Composable
private fun AlbumCard(
    album: AlbumItem,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
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
                    .background(album.gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = album.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(MaterialTheme.spacing.large)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.small)
            ) {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${album.count} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class AlbumItem(
    val name: String,
    val count: Int,
    val icon: ImageVector,
    val gradient: Brush
)
