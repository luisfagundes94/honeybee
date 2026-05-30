package com.luisfagundes.library.impl.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.library.api.presentation.navigation.LibraryRoute
import com.luisfagundes.impl.presentation.screen.LibraryScreen

internal fun EntryProviderScope<NavKey>.libraryEntries() {
    entry<LibraryRoute> {
        LibraryScreen()
    }
}