package com.luisfagundes.albums.impl.presentation.tools

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import com.luisfagundes.albums.impl.R
import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.presentation.model.AlbumStyle

@Composable
internal fun Album.getDisplayName(): String = when (this) {
    is Album.Physical -> this.name
    is Album.Virtual.Favorites -> stringResource(R.string.favorites)
    is Album.Virtual.Videos -> stringResource(R.string.videos)
}

@Composable
internal fun Album.getCountText(): String = when (this.count) {
    0 -> stringResource(R.string.items_count_zero)
    else -> androidx.compose.ui.res.pluralStringResource(R.plurals.items_count, this.count, this.count)
}

@Composable
internal fun Album.getAlbumStyle(): AlbumStyle {
    val colors = MaterialTheme.colorScheme
    return when (this) {
        is Album.Virtual.Favorites -> AlbumStyle(
            icon = Icons.Default.Favorite,
            gradient = Brush.linearGradient(listOf(colors.tertiaryContainer, colors.tertiary))
        )
        is Album.Virtual.Videos -> AlbumStyle(
            icon = Icons.Default.VideoLibrary,
            gradient = Brush.linearGradient(listOf(colors.primaryContainer, colors.primary))
        )
        is Album.Physical -> {
            val lowerName = this.name.lowercase()
            when {
                lowerName.contains("camera") -> AlbumStyle(
                    icon = Icons.Default.Camera,
                    gradient = Brush.linearGradient(listOf(colors.errorContainer, colors.error))
                )
                lowerName.contains("screenshot") -> AlbumStyle(
                    icon = Icons.Default.Image,
                    gradient = Brush.linearGradient(listOf(colors.secondaryContainer, colors.secondary))
                )
                lowerName.contains("download") -> AlbumStyle(
                    icon = Icons.Default.Folder,
                    gradient = Brush.linearGradient(listOf(colors.tertiaryContainer, colors.tertiary))
                )
                lowerName.contains("whatsapp") -> AlbumStyle(
                    icon = Icons.Default.Folder,
                    gradient = Brush.linearGradient(listOf(colors.primaryContainer, colors.primary))
                )
                else -> AlbumStyle(
                    icon = Icons.Default.Folder,
                    gradient = Brush.linearGradient(listOf(colors.surfaceVariant, colors.onSurfaceVariant))
                )
            }
        }
    }
}
