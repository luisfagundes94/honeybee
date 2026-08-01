package com.luisfagundes.honeybee.presentation.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.albums.api.presentation.navigation.AlbumDetailsRoute
import com.luisfagundes.albums.api.presentation.navigation.AlbumsRoute
import com.luisfagundes.config.api.presentation.navigation.ConfigRoute
import com.luisfagundes.library.api.presentation.navigation.LibraryRoute
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class TopLevelNavigationStateTest {

    @Test
    fun `selecting a top-level route preserves each stack`() {
        // Given
        val albumsBackStack = NavBackStack<NavKey>(AlbumsRoute)
        val libraryBackStack = NavBackStack<NavKey>(LibraryRoute)
        val configBackStack = NavBackStack<NavKey>(ConfigRoute)
        val state = TopLevelNavigationState(
            selectedRouteState = mutableStateOf(LibraryRoute),
            backStacks = mapOf(
                AlbumsRoute to albumsBackStack,
                LibraryRoute to libraryBackStack,
                ConfigRoute to configBackStack,
            ),
        )
        libraryBackStack.add(AlbumDetailsRoute("library", "Library"))

        // When
        state.select(AlbumsRoute)
        albumsBackStack.add(AlbumDetailsRoute("albums", "Albums"))
        state.select(LibraryRoute)

        // Then
        assertEquals(LibraryRoute, state.selectedRoute)
        assertEquals(
            listOf(LibraryRoute, AlbumDetailsRoute("library", "Library")),
            state.currentBackStack,
        )
        assertEquals(
            listOf(AlbumsRoute, AlbumDetailsRoute("albums", "Albums")),
            albumsBackStack,
        )
        assertEquals(listOf(ConfigRoute), configBackStack)
    }

    @Test
    fun `selecting the current route does not reset its stack`() {
        // Given
        val libraryBackStack = NavBackStack<NavKey>(LibraryRoute)
        libraryBackStack.add(AlbumDetailsRoute("library", "Library"))
        val state = TopLevelNavigationState(
            selectedRouteState = mutableStateOf(LibraryRoute),
            backStacks = mapOf(LibraryRoute to libraryBackStack),
        )

        // When
        state.select(LibraryRoute)

        // Then
        assertSame(libraryBackStack, state.currentBackStack)
        assertEquals(2, state.currentBackStack.size)
    }

    @Test
    fun `popCurrent removes nested routes but not the top-level root`() {
        // Given
        val libraryBackStack = NavBackStack<NavKey>(LibraryRoute)
        val state = TopLevelNavigationState(
            selectedRouteState = mutableStateOf(LibraryRoute),
            backStacks = mapOf(LibraryRoute to libraryBackStack),
        )

        // When & Then
        assertFalse(state.popCurrent())
        assertEquals(listOf(LibraryRoute), libraryBackStack)

        libraryBackStack.add(AlbumDetailsRoute("library", "Library"))
        assertTrue(state.popCurrent())
        assertEquals(listOf(LibraryRoute), libraryBackStack)
    }
}
