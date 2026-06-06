package com.luisfagundes.library.impl.presentation.viewmodel

import android.net.Uri
import app.cash.turbine.test
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.library.impl.R
import com.luisfagundes.library.impl.domain.model.Media
import com.luisfagundes.library.impl.domain.usecase.GetActiveMediaUseCase
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

    private val getActiveMediaUseCase: GetActiveMediaUseCase = mockk()
    private val moveToTrashUseCase: MoveToTrashUseCase = mockk()
    private val getItemsInTrashCountUseCase: GetItemsInTrashCountUseCase = mockk()
    private val resourceProvider: ResourceProvider = mockk()

    private lateinit var viewModel: MediaDetailsViewModel

    @BeforeEach
    fun setUp() {
        viewModel = MediaDetailsViewModel(
            getActiveMediaUseCase = getActiveMediaUseCase,
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
        val media1 = Media(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L, isVideo = false)
        val media2 = Media(id = 2L, uri = mockUri, dateAdded = 1100L, size = 2100L, isVideo = true)
        val media3 = Media(id = 3L, uri = mockUri, dateAdded = 1200L, size = 2200L, isVideo = false)
        val mediaList = listOf(media1, media2, media3)
        val trashCount = 2

        coEvery { getActiveMediaUseCase() } returns Result.success(mediaList)
        coEvery { getItemsInTrashCountUseCase() } returns trashCount

        // When & Then
        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialMediaId = 2L))

            val contentState = awaitItem() as MediaDetailsUiState.Content
            assertEquals(mediaList, contentState.mediaList)
            assertEquals(1, contentState.initialIndex)
            assertEquals(trashCount, contentState.trashCount)

            coVerify(exactly = 1) { getActiveMediaUseCase() }
            coVerify(exactly = 1) { getItemsInTrashCountUseCase() }
        }
    }

    @Test
    fun `dispatchEvent LoadDetails success with non-existent id should set Content state with initialIndex 0`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val media1 = Media(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L, isVideo = false)
        val mediaList = listOf(media1)
        val trashCount = 0

        coEvery { getActiveMediaUseCase() } returns Result.success(mediaList)
        coEvery { getItemsInTrashCountUseCase() } returns trashCount

        // When & Then
        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialMediaId = 999L))

            val contentState = awaitItem() as MediaDetailsUiState.Content
            assertEquals(mediaList, contentState.mediaList)
            assertEquals(0, contentState.initialIndex)
            assertEquals(trashCount, contentState.trashCount)

            coVerify(exactly = 1) { getActiveMediaUseCase() }
            coVerify(exactly = 1) { getItemsInTrashCountUseCase() }
        }
    }

    @Test
    fun `dispatchEvent LoadDetails failure should set Error state`() = runTest {
        // Given
        val errorMessage = "Failed to load media details"
        val exception = Exception("Failed to load media")

        coEvery { getActiveMediaUseCase() } returns Result.failure(exception)
        every { resourceProvider.getString(R.string.failed_to_load_photo_details) } returns errorMessage

        // When & Then
        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialMediaId = 1L))

            val errorState = awaitItem() as MediaDetailsUiState.Error
            assertEquals(errorMessage, errorState.message)

            coVerify(exactly = 1) { getActiveMediaUseCase() }
            coVerify(exactly = 1) { resourceProvider.getString(R.string.failed_to_load_photo_details) }
        }
    }

    @Test
    fun `dispatchEvent SwipeUp should move media to trash and update state when other media remain`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val media1 = Media(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L, isVideo = false)
        val media2 = Media(id = 2L, uri = mockUri, dateAdded = 1100L, size = 2100L, isVideo = true)
        val mediaList = listOf(media1, media2)
        val trashCountBefore = 2
        val trashCountAfter = 3

        coEvery { getActiveMediaUseCase() } returns Result.success(mediaList)
        coEvery { getItemsInTrashCountUseCase() } returnsMany listOf(trashCountBefore, trashCountAfter)
        coEvery { moveToTrashUseCase(mediaId = 1L) } returns Unit

        // When & Then
        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // Initial load
            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialMediaId = 1L))
            val initialContent = awaitItem() as MediaDetailsUiState.Content
            assertEquals(mediaList, initialContent.mediaList)
            assertEquals(trashCountBefore, initialContent.trashCount)

            // Swipe up/Move to trash
            viewModel.dispatchEvent(MediaDetailsUiEvent.SwipeUp(mediaId = 1L))

            val updatedContent = awaitItem() as MediaDetailsUiState.Content
            assertEquals(listOf(media2), updatedContent.mediaList)
            assertEquals(trashCountAfter, updatedContent.trashCount)

            coVerify(exactly = 1) { moveToTrashUseCase(mediaId = 1L) }
            coVerify(exactly = 2) { getItemsInTrashCountUseCase() }
        }
    }

    @Test
    fun `dispatchEvent SwipeUp should move media to trash and send NavigateBack effect when no media remain`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val media1 = Media(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L, isVideo = false)
        val mediaList = listOf(media1)
        val trashCount = 2

        coEvery { getActiveMediaUseCase() } returns Result.success(mediaList)
        coEvery { getItemsInTrashCountUseCase() } returns trashCount
        coEvery { moveToTrashUseCase(mediaId = 1L) } returns Unit

        // When & Then
        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // Initial load
            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialMediaId = 1L))
            val initialContent = awaitItem() as MediaDetailsUiState.Content
            assertEquals(mediaList, initialContent.mediaList)

            viewModel.uiEffect.test {
                viewModel.dispatchEvent(MediaDetailsUiEvent.SwipeUp(mediaId = 1L))

                assertEquals(MediaDetailsUiEffect.NavigateBack, awaitItem())
            }

            coVerify(exactly = 1) { moveToTrashUseCase(mediaId = 1L) }
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
    fun `dispatchEvent ToggleFavorite should add media to favorite set if not favorited`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val media1 = Media(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L, isVideo = false)
        val mediaList = listOf(media1)
        val trashCount = 2

        coEvery { getActiveMediaUseCase() } returns Result.success(mediaList)
        coEvery { getItemsInTrashCountUseCase() } returns trashCount

        // When & Then
        viewModel.uiState.test {
            assertEquals(MediaDetailsUiState.Loading, awaitItem())

            // Initial load
            viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialMediaId = 1L))
            val initialContent = awaitItem() as MediaDetailsUiState.Content
            assertEquals(emptySet<Long>(), initialContent.favoriteMediaIds)

            // Toggle favorite ON
            viewModel.dispatchEvent(MediaDetailsUiEvent.ToggleFavorite(mediaId = 1L))
            val favoritedContent = awaitItem() as MediaDetailsUiState.Content
            assertEquals(setOf(1L), favoritedContent.favoriteMediaIds)

            // Toggle favorite OFF
            viewModel.dispatchEvent(MediaDetailsUiEvent.ToggleFavorite(mediaId = 1L))
            val unfavoritedContent = awaitItem() as MediaDetailsUiState.Content
            assertEquals(emptySet<Long>(), unfavoritedContent.favoriteMediaIds)
        }
    }
}
