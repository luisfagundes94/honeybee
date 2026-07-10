package com.luisfagundes.albums.impl.presentation.viewmodel

import android.net.Uri
import app.cash.turbine.test
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.albums.impl.R
import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.domain.usecase.GetAlbumsUseCase
import com.luisfagundes.albums.impl.presentation.effect.AlbumsUiEffect
import com.luisfagundes.albums.impl.presentation.event.AlbumsUiEvent
import com.luisfagundes.albums.impl.presentation.state.AlbumsUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val getAlbumsUseCase: GetAlbumsUseCase = mockk()
    private val resourceProvider: ResourceProvider = mockk()

    private lateinit var viewModel: AlbumsViewModel

    @BeforeEach
    fun setUp() {
        viewModel = AlbumsViewModel(
            getAlbumsUseCase = getAlbumsUseCase,
            resourceProvider = resourceProvider
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
        val mockUri = mockk<Uri>()
        val albums = listOf(
            Album.Physical(id = "1", name = "Camera", count = 10, coverUri = mockUri, isVideo = false),
            Album.Physical(id = "2", name = "Screenshots", count = 5, coverUri = mockUri, isVideo = false)
        )

        coEvery { getAlbumsUseCase() } returns Result.success(albums)

        // When & Then
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Loading, awaitItem())

            viewModel.dispatchEvent(AlbumsUiEvent.LoadAlbums)

            val contentState = awaitItem() as AlbumsUiState.Content
            assertEquals(albums, contentState.albums)

            coVerify(exactly = 1) { getAlbumsUseCase() }
        }
    }

    @Test
    fun `dispatchEvent LoadAlbums failure should set Error state`() = runTest {
        // Given
        val errorMessage = "Failed to load albums"
        val exception = Exception("Query error")

        coEvery { getAlbumsUseCase() } returns Result.failure(exception)
        every { resourceProvider.getString(R.string.error_loading_albums) } returns errorMessage

        // When & Then
        viewModel.uiState.test {
            assertEquals(AlbumsUiState.Loading, awaitItem())

            viewModel.dispatchEvent(AlbumsUiEvent.LoadAlbums)

            val errorState = awaitItem() as AlbumsUiState.Error
            assertEquals(errorMessage, errorState.message)

            coVerify(exactly = 1) { getAlbumsUseCase() }
            coVerify(exactly = 1) { resourceProvider.getString(R.string.error_loading_albums) }
        }
    }

    @Test
    fun `dispatchEvent AlbumClick should emit NavigateToAlbumDetails effect`() = runTest {
        // Given
        val albumId = "camera_id"
        val albumName = "Camera"

        // When & Then
        viewModel.uiEffect.test {
            viewModel.dispatchEvent(AlbumsUiEvent.AlbumClick(albumId, albumName))

            val effect = awaitItem() as AlbumsUiEffect.NavigateToAlbumDetails
            assertEquals(albumId, effect.albumId)
            assertEquals(albumName, effect.albumName)
        }
    }
}
