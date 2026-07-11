package com.luisfagundes.albums.impl.presentation.tools

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

internal fun Album.getAlbumStyle(): AlbumStyle {
    return when (this) {
        is Album.Virtual.Favorites -> AlbumStyle(
            icon = Icons.Default.Favorite,
            gradient = Brush.linearGradient(listOf(Color(0xFFFF80AB), Color(0xFFFF4081)))
        )
        is Album.Virtual.Videos -> AlbumStyle(
            icon = Icons.Default.VideoLibrary,
            gradient = Brush.linearGradient(listOf(Color(0xFFB388FF), Color(0xFF7C4DFF)))
        )
        is Album.Physical -> {
            val lowerName = this.name.lowercase()
            when {
                lowerName.contains("camera") -> AlbumStyle(
                    icon = Icons.Default.Camera,
                    gradient = Brush.linearGradient(listOf(Color(0xFFFF8A80), Color(0xFFFF5252)))
                )
                lowerName.contains("screenshot") -> AlbumStyle(
                    icon = Icons.Default.Image,
                    gradient = Brush.linearGradient(listOf(Color(0xFF82B1FF), Color(0xFF448AFF)))
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
    }
}