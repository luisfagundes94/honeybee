package com.luisfagundes.albums.impl.presentation.viewmodel

import android.net.Uri
import app.cash.turbine.test
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.albums.impl.R
import com.luisfagundes.albums.impl.domain.model.AlbumMedia
import com.luisfagundes.albums.impl.domain.usecase.GetAlbumMediaUseCase
import com.luisfagundes.albums.impl.presentation.effect.AlbumDetailsUiEffect
import com.luisfagundes.albums.impl.presentation.event.AlbumDetailsUiEvent
import com.luisfagundes.albums.impl.presentation.state.AlbumDetailsUiState
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
class AlbumDetailsViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val getAlbumMediaUseCase: GetAlbumMediaUseCase = mockk()
    private val resourceProvider: ResourceProvider = mockk()

    private lateinit var viewModel: AlbumDetailsViewModel

    @BeforeEach
    fun setUp() {
        viewModel = AlbumDetailsViewModel(
            getAlbumMediaUseCase = getAlbumMediaUseCase,
            resourceProvider = resourceProvider
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

            val contentState = awaitItem() as AlbumDetailsUiState.Content
            assertEquals(mediaList, contentState.mediaList)

            coVerify(exactly = 1) { getAlbumMediaUseCase(albumId) }
        }
    }

    @Test
    fun `dispatchEvent LoadMedia failure should set Error state`() = runTest {
        // Given
        val albumId = "camera_id"
        val errorMessage = "Failed to load media"
        val exception = Exception("Query error")

        coEvery { getAlbumMediaUseCase(albumId) } returns Result.failure(exception)
        every { resourceProvider.getString(R.string.error_loading_album_media) } returns errorMessage

        // When & Then
        viewModel.uiState.test {
            assertEquals(AlbumDetailsUiState.Loading, awaitItem())

            viewModel.dispatchEvent(AlbumDetailsUiEvent.LoadMedia(albumId))

            val errorState = awaitItem() as AlbumDetailsUiState.Error
            assertEquals(errorMessage, errorState.message)

            coVerify(exactly = 1) { getAlbumMediaUseCase(albumId) }
            coVerify(exactly = 1) { resourceProvider.getString(R.string.error_loading_album_media) }
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

        coEvery { getAlbumMediaUseCase(albumId) } returns Result.failure(Exception()) andThen Result.success(mediaList)
        every { resourceProvider.getString(R.string.error_loading_album_media) } returns "Error"

        // When & Then
        viewModel.uiState.test {
            assertEquals(AlbumDetailsUiState.Loading, awaitItem())

            viewModel.dispatchEvent(AlbumDetailsUiEvent.LoadMedia(albumId))
            assertEquals("Error", (awaitItem() as AlbumDetailsUiState.Error).message)

            viewModel.dispatchEvent(AlbumDetailsUiEvent.Retry)
            assertEquals(mediaList, (awaitItem() as AlbumDetailsUiState.Content).mediaList)

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

            val effect = awaitItem() as AlbumDetailsUiEffect.NavigateToMediaDetail
            assertEquals(mediaId, effect.mediaId)
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
