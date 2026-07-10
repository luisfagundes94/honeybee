package com.luisfagundes.albums.api.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AlbumsRoute : NavKey

@Serializable
data class AlbumDetailsRoute(
    val albumId: String,
    val albumName: String
) : NavKey

