package com.luisfagundes.honeybee.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.luisfagundes.albums.api.presentation.navigation.AlbumsRoute
import com.luisfagundes.config.api.presentation.navigation.ConfigRoute
import com.luisfagundes.library.api.presentation.navigation.LibraryRoute

@Composable
fun AppNavDisplay(
    backStack: NavBackStack<NavKey>,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { backStack.removeLastOrNull() },
) {
    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider,
        modifier = modifier
    )
}

@Composable
internal fun AppNavDisplay(
    navigationState: TopLevelNavigationState,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val albumsEntries = rememberDecoratedEntries(
        backStack = navigationState.backStacks.getValue(AlbumsRoute),
        entryProvider = entryProvider,
    )
    val libraryEntries = rememberDecoratedEntries(
        backStack = navigationState.backStacks.getValue(LibraryRoute),
        entryProvider = entryProvider,
    )
    val configEntries = rememberDecoratedEntries(
        backStack = navigationState.backStacks.getValue(ConfigRoute),
        entryProvider = entryProvider,
    )
    val currentEntries = when (navigationState.selectedRoute) {
        AlbumsRoute -> albumsEntries
        LibraryRoute -> libraryEntries
        ConfigRoute -> configEntries
        else -> error("Unknown top-level route: ${navigationState.selectedRoute}")
    }

    NavDisplay(
        entries = currentEntries,
        onBack = {
            if (!navigationState.popCurrent()) onExit()
        },
        modifier = modifier,
    )
}

@Composable
private fun rememberDecoratedEntries(
    backStack: NavBackStack<NavKey>,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): List<NavEntry<NavKey>> = rememberDecoratedNavEntries(
    backStack = backStack,
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
    ),
    entryProvider = entryProvider,
)
