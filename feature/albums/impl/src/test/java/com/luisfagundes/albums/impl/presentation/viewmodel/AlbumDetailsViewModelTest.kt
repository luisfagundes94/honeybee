package com.luisfagundes.albums.impl.presentation.viewmodel

import android.net.Uri
import app.cash.turbine.test
import com.luisfagundes.albums.impl.domain.model.AlbumMedia
import com.luisfagundes.albums.impl.domain.usecase.GetAlbumMediaUseCase
import com.luisfagundes.albums.impl.presentation.effect.AlbumDetailsUiEffect
import com.luisfagundes.albums.impl.presentation.event.AlbumDetailsUiEvent
import com.luisfagundes.albums.impl.presentation.state.AlbumDetailsUiState
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
class AlbumDetailsViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val getAlbumMediaUseCase: GetAlbumMediaUseCase = mockk()

    private lateinit var viewModel: AlbumDetailsViewModel

    @BeforeEach
    fun setUp() {
        viewModel = AlbumDetailsViewModel(
            getAlbumMediaUseCase = getAlbumMediaUseCase
        )
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        // When & Then
        assertEquals(AlbumDetailsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `dispatchEvent LoadMedia success should set Content state`() = runTest {
        // Given
        val albumId = "camera_id"
        val mockUri = mockk<Uri>()
        val mediaList = listOf(
            AlbumMedia(id = 1L, uri = mockUri, dateAdded = 1000L, isVideo = false),
            AlbumMedia(id = 2L, uri = mockUri, dateAdded = 2000L, isVideo = true)
        )

        coEvery { getAlbumMediaUseCase(albumId) } returns Result.success(mediaList)

        // When & Then
        viewModel.uiState.test {
            assertEquals(AlbumDetailsUiState.Loading, awaitItem())

            viewModel.dispatchEvent(AlbumDetailsUiEvent.LoadMedia(albumId))

            assertEquals(AlbumDetailsUiState.Content(mediaList), awaitItem())

            coVerify(exactly = 1) { getAlbumMediaUseCase(albumId) }
        }
    }

    @Test
    fun `dispatchEvent LoadMedia failure should set Error state`() = runTest {
        // Given
        val albumId = "camera_id"
        val exception = Exception("Query error")

        coEvery { getAlbumMediaUseCase(albumId) } returns Result.failure(exception)

        // When & Then
        viewModel.uiState.test {
            assertEquals(AlbumDetailsUiState.Loading, awaitItem())

            viewModel.dispatchEvent(AlbumDetailsUiEvent.LoadMedia(albumId))

            assertEquals(AlbumDetailsUiState.Error, awaitItem())

            coVerify(exactly = 1) { getAlbumMediaUseCase(albumId) }
        }
    }

    @Test
    fun `dispatchEvent Retry success should load media again`() = runTest {
        // Given
        val albumId = "camera_id"
        val mockUri = mockk<Uri>()
        val mediaList = listOf(
            AlbumMedia(id = 1L, uri = mockUri, dateAdded = 1000L, isVideo = false)
        )

        coEvery { getAlbumMediaUseCase(albumId) } returns Result.failure(Exception()) andThen
                Result.success(mediaList)

        // When & Then
        viewModel.uiState.test {
            assertEquals(AlbumDetailsUiState.Loading, awaitItem())

            viewModel.dispatchEvent(AlbumDetailsUiEvent.LoadMedia(albumId))
            assertEquals(AlbumDetailsUiState.Error, awaitItem())

            viewModel.dispatchEvent(AlbumDetailsUiEvent.Retry)
            assertEquals(AlbumDetailsUiState.Content(mediaList), awaitItem())

            coVerify(exactly = 2) { getAlbumMediaUseCase(albumId) }
        }
    }

    @Test
    fun `dispatchEvent MediaClick should emit NavigateToMediaDetail effect`() = runTest {
        // Given
        val mediaId = 123L

        // When & Then
        viewModel.uiEffect.test {
            viewModel.dispatchEvent(AlbumDetailsUiEvent.MediaClick(mediaId))

            assertEquals(AlbumDetailsUiEffect.NavigateToMediaDetail(mediaId), awaitItem())
        }
    }

    @Test
    fun `dispatchEvent BackClick should emit NavigateBack effect`() = runTest {
        // When & Then
        viewModel.uiEffect.test {
            viewModel.dispatchEvent(AlbumDetailsUiEvent.BackClick)

            assertEquals(AlbumDetailsUiEffect.NavigateBack, awaitItem())
        }
    }
}
