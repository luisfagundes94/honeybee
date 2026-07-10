package com.luisfagundes.albums.impl.presentation.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.luisfagundes.albums.impl.presentation.model.AlbumStyle

internal fun getAlbumStyle(name: String): AlbumStyle {
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