package com.luisfagundes.albums.impl.presentation.viewmodel

import app.cash.turbine.test
import com.luisfagundes.albums.impl.domain.usecase.GetAlbumsUseCase
import com.luisfagundes.albums.impl.presentation.effect.AlbumsUiEffect
import com.luisfagundes.albums.impl.presentation.event.AlbumsUiEvent
import com.luisfagundes.albums.impl.presentation.state.AlbumsUiState
import com.luisfagundes.albums.impl.tools.fakeAlbum
import com.luisfagundes.core.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class AlbumsViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val getAlbumsUseCase: GetAlbumsUseCase = mockk()

    private lateinit var viewModel: AlbumsViewModel

    @BeforeEach
    fun setUp() {
        viewModel = AlbumsViewModel(
            getAlbumsUseCase = getAlbumsUseCase
        )
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        // When & Then
        assertEquals(AlbumsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `dispatchEvent LoadAlbums success should set Content state`() = runTest {
        // Given
        val albums = listOf(
            fakeAlbum.copy(id = "1"),
            fakeAlbum.copy(id = "2", name = "Screenshots", count = 5)
        )

        coEvery { getAlbumsUseCase() } returns Result.success(albums)

        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(AlbumsUiEvent.LoadAlbums)

            // Then
            assertEquals(AlbumsUiState.Content(albums), awaitItem())

            coVerify(exactly = 1) { getAlbumsUseCase() }
        }
    }

    @Test
    fun `dispatchEvent LoadAlbums failure should set Error state`() = runTest {
        // Given
        val exception = Exception("Query error")

        coEvery { getAlbumsUseCase() } returns Result.failure(exception)

        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(AlbumsUiEvent.LoadAlbums)

            // Then
            assertEquals(AlbumsUiState.Error, awaitItem())

            coVerify(exactly = 1) { getAlbumsUseCase() }
        }
    }

    @Test
    fun `dispatchEvent AlbumClick should emit NavigateToAlbumDetails effect`() = runTest {
        // Given
        val albumId = "camera_id"
        val albumName = "Camera"

        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(AlbumsUiEvent.AlbumClick(albumId, albumName))

            // Then
            assertEquals(AlbumsUiEffect.NavigateToAlbumDetails(albumId, albumName), awaitItem())
        }
    }
}
