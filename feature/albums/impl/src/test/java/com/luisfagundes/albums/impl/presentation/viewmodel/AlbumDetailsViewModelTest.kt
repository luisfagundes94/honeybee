package com.luisfagundes.albums.impl.presentation.viewmodel

import app.cash.turbine.test
import com.luisfagundes.albums.impl.domain.usecase.GetAlbumMediaUseCase
import com.luisfagundes.albums.impl.presentation.effect.AlbumDetailsUiEffect
import com.luisfagundes.albums.impl.presentation.event.AlbumDetailsUiEvent
import com.luisfagundes.albums.impl.presentation.state.AlbumDetailsUiState
import com.luisfagundes.albums.impl.tools.fakeAlbumMedia
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
internal class AlbumDetailsViewModelTest {

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
        val mediaList = listOf(
            fakeAlbumMedia,
            fakeAlbumMedia.copy(id = 2L, dateAdded = 2_000L, isVideo = true)
        )

        coEvery { getAlbumMediaUseCase(albumId) } returns Result.success(mediaList)

        viewModel.uiState.test {
            assertEquals(AlbumDetailsUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(AlbumDetailsUiEvent.LoadMedia(albumId))

            // Then
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

        viewModel.uiState.test {
            assertEquals(AlbumDetailsUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(AlbumDetailsUiEvent.LoadMedia(albumId))

            // Then
            assertEquals(AlbumDetailsUiState.Error, awaitItem())

            coVerify(exactly = 1) { getAlbumMediaUseCase(albumId) }
        }
    }

    @Test
    fun `dispatchEvent Retry success should load media again`() = runTest {
        // Given
        val albumId = "camera_id"
        val mediaList = listOf(fakeAlbumMedia)

        coEvery { getAlbumMediaUseCase(albumId) } returns Result.failure(Exception()) andThen
                Result.success(mediaList)

        viewModel.uiState.test {
            assertEquals(AlbumDetailsUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(AlbumDetailsUiEvent.LoadMedia(albumId))

            // Then
            assertEquals(AlbumDetailsUiState.Error, awaitItem())

            // When
            viewModel.dispatchEvent(AlbumDetailsUiEvent.Retry)

            // Then
            assertEquals(AlbumDetailsUiState.Content(mediaList), awaitItem())

            coVerify(exactly = 2) { getAlbumMediaUseCase(albumId) }
        }
    }

    @Test
    fun `dispatchEvent MediaClick should emit NavigateToMediaDetail effect`() = runTest {
        // Given
        val mediaId = 123L

        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(AlbumDetailsUiEvent.MediaClick(mediaId))

            // Then
            assertEquals(AlbumDetailsUiEffect.NavigateToMediaDetail(mediaId), awaitItem())
        }
    }

    @Test
    fun `dispatchEvent BackClick should emit NavigateBack effect`() = runTest {
        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(AlbumDetailsUiEvent.BackClick)

            // Then
            assertEquals(AlbumDetailsUiEffect.NavigateBack, awaitItem())
        }
    }
}
