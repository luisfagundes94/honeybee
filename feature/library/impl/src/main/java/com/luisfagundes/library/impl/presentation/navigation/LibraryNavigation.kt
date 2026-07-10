package com.luisfagundes.library.impl.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.core.common.presentation.navigation.LocalNavBackStack
import com.luisfagundes.library.api.presentation.navigation.CongratulationsRoute
import com.luisfagundes.library.api.presentation.navigation.LibraryRoute
import com.luisfagundes.library.api.presentation.navigation.MediaDetailsRoute
import com.luisfagundes.library.api.presentation.navigation.TrashRoute
import com.luisfagundes.library.impl.presentation.screen.CongratulationsScreen
import com.luisfagundes.library.impl.presentation.screen.LibraryScreen
import com.luisfagundes.library.impl.presentation.screen.MediaDetailsScreen
import com.luisfagundes.library.impl.presentation.screen.TrashScreen

internal fun EntryProviderScope<NavKey>.libraryEntries() {
    entry<LibraryRoute> {
        val backStack = LocalNavBackStack.current
        LibraryScreen(
            onNavigateToMediaDetail = { mediaId ->
                backStack?.add(MediaDetailsRoute(mediaId))
            },
            onNavigateToTrash = {
                backStack?.add(TrashRoute)
            }
        )
    }
    entry<MediaDetailsRoute> { route ->
        val backStack = LocalNavBackStack.current
        MediaDetailsScreen(
            initialMediaId = route.initialPhotoId,
            albumId = route.albumId,
            onNavigateBack = {
                backStack?.removeLastOrNull()
            },
            onNavigateToTrash = {
                backStack?.add(TrashRoute)
            }
        )
    }
    entry<TrashRoute> {
        val backStack = LocalNavBackStack.current
        TrashScreen(
            onNavigateBack = {
                backStack?.removeLastOrNull()
            },
            onNavigateToCongratulations = { count, size ->
                backStack?.add(CongratulationsRoute(count, size))
            }
        )
    }
    entry<CongratulationsRoute> { route ->
        val backStack = LocalNavBackStack.current
        CongratulationsScreen(
            deletedCount = route.deletedCount,
            deletedSize = route.deletedSize,
            onDoneClick = {
                backStack?.clear()
                backStack?.add(LibraryRoute)
            }
        )
    }
}