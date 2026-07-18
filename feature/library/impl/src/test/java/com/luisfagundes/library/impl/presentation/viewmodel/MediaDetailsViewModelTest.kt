package com.luisfagundes.library.impl.presentation.viewmodel

import app.cash.turbine.test
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import com.luisfagundes.library.impl.presentation.effect.MediaDetailsUiEffect
import com.luisfagundes.library.impl.presentation.event.MediaDetailsUiEvent
import com.luisfagundes.library.impl.presentation.state.MediaDetailsUiState
import com.luisfagundes.library.impl.tools.fakeMedia
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
internal class MediaDetailsViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val repository: LibraryRepository = mockk()

    private lateinit var viewModel: MediaDetailsViewModel

    @BeforeEach
    fun setUp() {
        viewModel = MediaDetailsViewModel(
            repository = repository
        )
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        // When & Then
        assertEquals(MediaDetailsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `dispatchEvent LoadDetails success should set Content state with correct initialIndex`() = runTest {
        // Given
        val mediaList = listOf(
            fakeMedia,
            fakeMedia.copy(id = 2L, dateAdded = 1_100L, size = 2_100L, isVideo = true),
            fakeMedia.copy(id = 3L, dateAdded = 1_200L, size = 2_200L)
        )
        val trashCount = 2

        coEvery { repository.getActiveMedia() } returns Result.success(mediaList)
        coEvery { repository.getItemsInTrashCount() } returns trashCount

        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialMediaId = 2L))

            // Then
            assertEquals(MediaDetailsUiState.Content(mediaList, 1, trashCount), awaitItem())

            coVerify(exactly = 1) { repository.getActiveMedia() }
            coVerify(exactly = 1) { repository.getItemsInTrashCount() }
        }
    }

    @Test
    fun `dispatchEvent LoadDetails success with non-existent id should set Content state with initialIndex 0`() = runTest {
        // Given
        val mediaList = listOf(fakeMedia)
        val trashCount = 0

        coEvery { repository.getActiveMedia() } returns Result.success(mediaList)
        coEvery { repository.getItemsInTrashCount() } returns trashCount

        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialMediaId = 999L))

            // Then
            assertEquals(MediaDetailsUiState.Content(mediaList, 0, trashCount), awaitItem())

            coVerify(exactly = 1) { repository.getActiveMedia() }
            coVerify(exactly = 1) { repository.getItemsInTrashCount() }
        }
    }

    @Test
    fun `dispatchEvent LoadDetails failure should set Error state`() = runTest {
        // Given
        val exception = Exception("Failed to load media")

        coEvery { repository.getActiveMedia() } returns Result.failure(exception)

        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialMediaId = 1L))

            // Then
            assertEquals(MediaDetailsUiState.Error, awaitItem())

            coVerify(exactly = 1) { repository.getActiveMedia() }
        }
    }

    @Test
    fun `dispatchEvent SwipeUp should move media to trash and update state when other media remain`() = runTest {
        // Given
        val media1 = fakeMedia
        val media2 = fakeMedia.copy(id = 2L, dateAdded = 1_100L, size = 2_100L, isVideo = true)
        val mediaList = listOf(media1, media2)
        val trashCountBefore = 2
        val trashCountAfter = 3

        coEvery { repository.getActiveMedia() } returns Result.success(mediaList)
        coEvery { repository.getItemsInTrashCount() } returnsMany listOf(trashCountBefore, trashCountAfter)
        coEvery { repository.moveToTrash(1L) } returns Unit

        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialMediaId = 1L))

            // Then
            assertEquals(MediaDetailsUiState.Content(mediaList, 0, trashCountBefore), awaitItem())

            // When
            viewModel.dispatchEvent(MediaDetailsUiEvent.SwipeUp(mediaId = 1L))

            // Then
            assertEquals(MediaDetailsUiState.Content(listOf(media2), 0, trashCountAfter), awaitItem())

            coVerify(exactly = 1) { repository.moveToTrash(1L) }
            coVerify(exactly = 2) { repository.getItemsInTrashCount() }
        }
    }

    @Test
    fun `dispatchEvent SwipeUp should move media to trash and send NavigateBack effect when no media remain`() = runTest {
        // Given
        val mediaList = listOf(fakeMedia)
        val trashCount = 2

        coEvery { repository.getActiveMedia() } returns Result.success(mediaList)
        coEvery { repository.getItemsInTrashCount() } returns trashCount
        coEvery { repository.moveToTrash(1L) } returns Unit

        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialMediaId = 1L))

            // Then
            assertEquals(MediaDetailsUiState.Content(mediaList, 0, trashCount), awaitItem())

            viewModel.uiEffect.test {
                // When
                viewModel.dispatchEvent(MediaDetailsUiEvent.SwipeUp(mediaId = 1L))

                // Then
                assertEquals(MediaDetailsUiEffect.NavigateBack, awaitItem())
            }

            coVerify(exactly = 1) { repository.moveToTrash(1L) }
        }
    }

    @Test
    fun `dispatchEvent TrashClick should emit NavigateToTrash effect`() = runTest {
        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(MediaDetailsUiEvent.TrashClick)

            // Then
            assertEquals(MediaDetailsUiEffect.NavigateToTrash, awaitItem())
        }
    }

    @Test
    fun `dispatchEvent ToggleFavorite should add media to favorite set if not favorited`() = runTest {
        // Given
        val mediaList = listOf(fakeMedia)
        val trashCount = 2

        coEvery { repository.getActiveMedia() } returns Result.success(mediaList)
        coEvery { repository.getItemsInTrashCount() } returns trashCount

        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialMediaId = 1L))

            // Then
            assertEquals(MediaDetailsUiState.Content(mediaList, 0, trashCount), awaitItem())

            // When
            viewModel.dispatchEvent(MediaDetailsUiEvent.ToggleFavorite(mediaId = 1L))

            // Then
            assertEquals(MediaDetailsUiState.Content(mediaList, 0, trashCount, setOf(1L)), awaitItem())

            // When
            viewModel.dispatchEvent(MediaDetailsUiEvent.ToggleFavorite(mediaId = 1L))

            // Then
            assertEquals(MediaDetailsUiState.Content(mediaList, 0, trashCount), awaitItem())
        }
    }

    @Test
    fun `dispatchEvent LoadDetails success with favorites albumId should scope to favorited media only`() = runTest {
        // Given
        val media1 = fakeMedia.copy(isFavorite = true)
        val media2 = fakeMedia.copy(id = 2L, dateAdded = 1_100L, size = 2_100L, isVideo = true)
        val mediaList = listOf(media1, media2)
        val trashCount = 1

        coEvery { repository.getActiveMedia() } returns Result.success(mediaList)
        coEvery { repository.getItemsInTrashCount() } returns trashCount

        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(
                MediaDetailsUiEvent.LoadDetails(
                    initialMediaId = 1L,
                    albumId = "favorites"
                )
            )

            // Then
            assertEquals(MediaDetailsUiState.Content(listOf(media1), 0, trashCount), awaitItem())
        }
    }

    @Test
    fun `dispatchEvent LoadDetails success with videos albumId should scope to videos only`() = runTest {
        // Given
        val media1 = fakeMedia
        val media2 = fakeMedia.copy(id = 2L, dateAdded = 1_100L, size = 2_100L, isVideo = true)
        val mediaList = listOf(media1, media2)
        val trashCount = 1

        coEvery { repository.getActiveMedia() } returns Result.success(mediaList)
        coEvery { repository.getItemsInTrashCount() } returns trashCount

        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(
                MediaDetailsUiEvent.LoadDetails(
                    initialMediaId = 2L,
                    albumId = "videos"
                )
            )

            // Then
            assertEquals(MediaDetailsUiState.Content(listOf(media2), 0, trashCount), awaitItem())
        }
    }

    @Test
    fun `dispatchEvent LoadDetails success with physical albumId should scope to that bucketId only`() = runTest {
        // Given
        val media1 = fakeMedia.copy(bucketId = "downloads")
        val media2 = fakeMedia.copy(id = 2L, dateAdded = 1_100L, size = 2_100L, bucketId = "camera")
        val mediaList = listOf(media1, media2)
        val trashCount = 1

        coEvery { repository.getActiveMedia() } returns Result.success(mediaList)
        coEvery { repository.getItemsInTrashCount() } returns trashCount

        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(
                MediaDetailsUiEvent.LoadDetails(
                    initialMediaId = 1L,
                    albumId = "downloads"
                )
            )

            // Then
            assertEquals(MediaDetailsUiState.Content(listOf(media1), 0, trashCount), awaitItem())
        }
    }
}
