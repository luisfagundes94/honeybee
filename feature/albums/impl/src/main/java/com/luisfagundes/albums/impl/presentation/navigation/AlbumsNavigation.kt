package com.luisfagundes.albums.impl.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.albums.api.presentation.navigation.AlbumsRoute
import com.luisfagundes.albums.api.presentation.navigation.AlbumDetailsRoute
import com.luisfagundes.library.api.presentation.navigation.MediaDetailsRoute
import com.luisfagundes.albums.impl.presentation.screen.AlbumsScreen
import com.luisfagundes.albums.impl.presentation.screen.AlbumDetailsScreen
import com.luisfagundes.core.common.presentation.navigation.LocalNavBackStack

internal fun EntryProviderScope<NavKey>.albumsEntries() {
    entry<AlbumsRoute> {
        val backStack = LocalNavBackStack.current
        AlbumsScreen(
            onNavigateToAlbumDetails = { albumId, albumName ->
                backStack?.add(
                    AlbumDetailsRoute(
                        albumId = albumId,
                        albumName = albumName
                    )
                )
            }
        )
    }
    entry<AlbumDetailsRoute> { route ->
        val backStack = LocalNavBackStack.current
        AlbumDetailsScreen(
            albumId = route.albumId,
            albumName = route.albumName,
            onNavigateBack = {
                backStack?.removeLastOrNull()
            },
            onNavigateToMediaDetail = { mediaId ->
                backStack?.add(
                    MediaDetailsRoute(
                        initialPhotoId = mediaId,
                        albumId = route.albumId
                    )
                )
            }
        )
    }
}
