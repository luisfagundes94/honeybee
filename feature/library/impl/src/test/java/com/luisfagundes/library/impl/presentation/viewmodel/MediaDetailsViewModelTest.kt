package com.luisfagundes.library.impl.presentation.viewmodel

import android.net.Uri
import app.cash.turbine.test
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.library.impl.R
import com.luisfagundes.library.impl.domain.model.Photo
import com.luisfagundes.library.impl.domain.usecase.GetActivePhotosUseCase
import com.luisfagundes.library.impl.domain.usecase.GetItemsInTrashCountUseCase
import com.luisfagundes.library.impl.domain.usecase.MoveToTrashUseCase
import com.luisfagundes.library.impl.presentation.effect.MediaDetailsUiEffect
import com.luisfagundes.library.impl.presentation.event.MediaDetailsUiEvent
import com.luisfagundes.library.impl.presentation.state.MediaDetailsUiState
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
class MediaDetailsViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val getActivePhotosUseCase: GetActivePhotosUseCase = mockk()
    private val moveToTrashUseCase: MoveToTrashUseCase = mockk()
    private val getItemsInTrashCountUseCase: GetItemsInTrashCountUseCase = mockk()
    private val resourceProvider: ResourceProvider = mockk()

    private lateinit var viewModel: MediaDetailsViewModel

    @BeforeEach
    fun setUp() {
        viewModel = MediaDetailsViewModel(
            getActivePhotosUseCase = getActivePhotosUseCase,
            moveToTrashUseCase = moveToTrashUseCase,
            getItemsInTrashCountUseCase = getItemsInTrashCountUseCase,
            resourceProvider = resourceProvider
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
        val mockUri = mockk<Uri>()
        val photo1 = Photo(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L)
        val photo2 = Photo(id = 2L, uri = mockUri, dateAdded = 1100L, size = 2100L)
        val photo3 = Photo(id = 3L, uri = mockUri, dateAdded = 1200L, size = 2200L)
        val photos = listOf(photo1, photo2, photo3)
        val trashCount = 2

        coEvery { getActivePhotosUseCase() } returns Result.success(photos)
        coEvery { getItemsInTrashCountUseCase() } returns trashCount

        // When & Then
        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialPhotoId = 2L))

            val contentState = awaitItem() as MediaDetailsUiState.Content
            assertEquals(photos, contentState.photos)
            assertEquals(1, contentState.initialIndex)
            assertEquals(trashCount, contentState.trashCount)

            coVerify(exactly = 1) { getActivePhotosUseCase() }
            coVerify(exactly = 1) { getItemsInTrashCountUseCase() }
        }
    }

    @Test
    fun `dispatchEvent LoadDetails success with non-existent id should set Content state with initialIndex 0`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val photo1 = Photo(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L)
        val photos = listOf(photo1)
        val trashCount = 0

        coEvery { getActivePhotosUseCase() } returns Result.success(photos)
        coEvery { getItemsInTrashCountUseCase() } returns trashCount

        // When & Then
        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialPhotoId = 999L))

            val contentState = awaitItem() as MediaDetailsUiState.Content
            assertEquals(photos, contentState.photos)
            assertEquals(0, contentState.initialIndex)
            assertEquals(trashCount, contentState.trashCount)

            coVerify(exactly = 1) { getActivePhotosUseCase() }
            coVerify(exactly = 1) { getItemsInTrashCountUseCase() }
        }
    }

    @Test
    fun `dispatchEvent LoadDetails failure should set Error state`() = runTest {
        // Given
        val errorMessage = "Failed to load photo details"
        val exception = Exception("Failed to load photos")

        coEvery { getActivePhotosUseCase() } returns Result.failure(exception)
        every { resourceProvider.getString(R.string.failed_to_load_photo_details) } returns errorMessage

        // When & Then
        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialPhotoId = 1L))

            val errorState = awaitItem() as MediaDetailsUiState.Error
            assertEquals(errorMessage, errorState.message)

            coVerify(exactly = 1) { getActivePhotosUseCase() }
            coVerify(exactly = 1) { resourceProvider.getString(R.string.failed_to_load_photo_details) }
        }
    }

    @Test
    fun `dispatchEvent SwipeUp should move photo to trash and update state when other photos remain`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val photo1 = Photo(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L)
        val photo2 = Photo(id = 2L, uri = mockUri, dateAdded = 1100L, size = 2100L)
        val photos = listOf(photo1, photo2)
        val trashCountBefore = 2
        val trashCountAfter = 3

        coEvery { getActivePhotosUseCase() } returns Result.success(photos)
        coEvery { getItemsInTrashCountUseCase() } returnsMany listOf(trashCountBefore, trashCountAfter)
        coEvery { moveToTrashUseCase(photoId = 1L) } returns Unit

        // When & Then
        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // Initial load
            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialPhotoId = 1L))
            val initialContent = awaitItem() as MediaDetailsUiState.Content
            assertEquals(photos, initialContent.photos)
            assertEquals(trashCountBefore, initialContent.trashCount)

            // Swipe up/Move to trash
            viewModel.dispatchEvent(MediaDetailsUiEvent.SwipeUp(photoId = 1L))

            val updatedContent = awaitItem() as MediaDetailsUiState.Content
            assertEquals(listOf(photo2), updatedContent.photos)
            assertEquals(trashCountAfter, updatedContent.trashCount)

            coVerify(exactly = 1) { moveToTrashUseCase(photoId = 1L) }
            coVerify(exactly = 2) { getItemsInTrashCountUseCase() }
        }
    }

    @Test
    fun `dispatchEvent SwipeUp should move photo to trash and send NavigateBack effect when no photos remain`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val photo1 = Photo(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L)
        val photos = listOf(photo1)
        val trashCount = 2

        coEvery { getActivePhotosUseCase() } returns Result.success(photos)
        coEvery { getItemsInTrashCountUseCase() } returns trashCount
        coEvery { moveToTrashUseCase(photoId = 1L) } returns Unit

        // When & Then
        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // Initial load
            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialPhotoId = 1L))
            val initialContent = awaitItem() as MediaDetailsUiState.Content
            assertEquals(photos, initialContent.photos)

            viewModel.uiEffect.test {
                viewModel.dispatchEvent(MediaDetailsUiEvent.SwipeUp(photoId = 1L))

                assertEquals(MediaDetailsUiEffect.NavigateBack, awaitItem())
            }

            coVerify(exactly = 1) { moveToTrashUseCase(photoId = 1L) }
        }
    }

    @Test
    fun `dispatchEvent TrashClick should emit NavigateToTrash effect`() = runTest {
        // When & Then
        viewModel.uiEffect.test {
            viewModel.dispatchEvent(MediaDetailsUiEvent.TrashClick)

            assertEquals(MediaDetailsUiEffect.NavigateToTrash, awaitItem())
        }
    }

    @Test
    fun `dispatchEvent ToggleFavorite should add photo to favorite set if not favorited`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val photo1 = Photo(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L)
        val photos = listOf(photo1)
        val trashCount = 2

        coEvery { getActivePhotosUseCase() } returns Result.success(photos)
        coEvery { getItemsInTrashCountUseCase() } returns trashCount

        // When & Then
        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // Initial load
            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialPhotoId = 1L))
            val initialContent = awaitItem() as MediaDetailsUiState.Content
            assertEquals(emptySet<Long>(), initialContent.favoritePhotoIds)

            // Toggle favorite ON
            viewModel.dispatchEvent(MediaDetailsUiEvent.ToggleFavorite(photoId = 1L))
            val favoritedContent = awaitItem() as MediaDetailsUiState.Content
            assertEquals(setOf(1L), favoritedContent.favoritePhotoIds)

            // Toggle favorite OFF
            viewModel.dispatchEvent(MediaDetailsUiEvent.ToggleFavorite(photoId = 1L))
            val unfavoritedContent = awaitItem() as MediaDetailsUiState.Content
            assertEquals(emptySet<Long>(), unfavoritedContent.favoritePhotoIds)
        }
    }
}
