package com.luisfagundes.albums.impl.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.albums.api.presentation.navigation.AlbumsRoute
import com.luisfagundes.albums.impl.presentation.screen.AlbumsScreen

internal fun EntryProviderScope<NavKey>.albumsEntries() {
    entry<AlbumsRoute> {
        AlbumsScreen()
    }
}
