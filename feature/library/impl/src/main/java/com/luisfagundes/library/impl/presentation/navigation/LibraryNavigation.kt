package com.luisfagundes.library.impl.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.library.api.presentation.navigation.LibraryRoute
import com.luisfagundes.library.api.presentation.navigation.MediaDetailsRoute
import com.luisfagundes.library.api.presentation.navigation.TrashRoute
import com.luisfagundes.library.impl.presentation.screen.LibraryScreen
import com.luisfagundes.library.impl.presentation.screen.MediaDetailsScreen
import com.luisfagundes.library.impl.presentation.screen.TrashScreen

internal fun EntryProviderScope<NavKey>.libraryEntries() {
    entry<LibraryRoute> {
        LibraryScreen()
    }
    entry<MediaDetailsRoute> { route ->
        MediaDetailsScreen(initialPhotoId = route.initialPhotoId)
    }
    entry<TrashRoute> {
        TrashScreen()
    }
}