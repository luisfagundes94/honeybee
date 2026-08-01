package com.luisfagundes.honeybee.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import com.luisfagundes.albums.api.presentation.navigation.AlbumsRoute
import com.luisfagundes.config.api.presentation.navigation.ConfigRoute
import com.luisfagundes.library.api.presentation.navigation.LibraryRoute

internal class TopLevelNavigationState(
    private val selectedRouteState: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    var selectedRoute: NavKey by selectedRouteState
        private set

    val currentBackStack: NavBackStack<NavKey>
        get() = backStacks.getValue(selectedRoute)

    fun select(route: NavKey) {
        check(route in backStacks) { "Unknown top-level route: $route" }
        selectedRoute = route
    }

    fun popCurrent(): Boolean {
        if (currentBackStack.size <= 1) return false

        currentBackStack.removeAt(currentBackStack.lastIndex)
        return true
    }
}

@Composable
internal fun rememberTopLevelNavigationState(
    startRoute: NavKey = LibraryRoute,
): TopLevelNavigationState {
    val selectedRouteState = rememberSerializable(
        startRoute,
        serializer = MutableStateSerializer(NavKeySerializer())
    ) {
        mutableStateOf(startRoute)
    }

    val albumsBackStack = rememberNavBackStack(AlbumsRoute)
    val libraryBackStack = rememberNavBackStack(LibraryRoute)
    val configBackStack = rememberNavBackStack(ConfigRoute)
    val backStacks = remember(albumsBackStack, libraryBackStack, configBackStack) {
        mapOf<NavKey, NavBackStack<NavKey>>(
            AlbumsRoute to albumsBackStack,
            LibraryRoute to libraryBackStack,
            ConfigRoute to configBackStack,
        )
    }

    return remember(selectedRouteState, backStacks) {
        TopLevelNavigationState(
            selectedRouteState = selectedRouteState,
            backStacks = backStacks,
        )
    }
}
