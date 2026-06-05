package com.luisfagundes.library.impl.presentation.viewmodel

import android.app.PendingIntent
import android.content.IntentSender
import android.net.Uri
import app.cash.turbine.test
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.library.impl.R
import com.luisfagundes.library.impl.domain.model.Photo
import com.luisfagundes.library.impl.domain.usecase.CreateDeleteRequestUseCase
import com.luisfagundes.library.impl.domain.usecase.GetTrashPhotosUseCase
import com.luisfagundes.library.impl.domain.usecase.PermanentlyDeleteUseCase
import com.luisfagundes.library.impl.domain.usecase.RestoreFromTrashUseCase
import com.luisfagundes.library.impl.presentation.effect.TrashUiEffect
import com.luisfagundes.library.impl.presentation.event.TrashUiEvent
import com.luisfagundes.library.impl.presentation.state.TrashUiState
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
class TrashViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val getTrashPhotosUseCase: GetTrashPhotosUseCase = mockk()
    private val restoreFromTrashUseCase: RestoreFromTrashUseCase = mockk()
    private val permanentlyDeleteUseCase: PermanentlyDeleteUseCase = mockk()
    private val createDeleteRequestUseCase: CreateDeleteRequestUseCase = mockk()
    private val resourceProvider: ResourceProvider = mockk()

    private lateinit var viewModel: TrashViewModel

    @BeforeEach
    fun setUp() {
        viewModel = TrashViewModel(
            getTrashPhotosUseCase = getTrashPhotosUseCase,
            restoreFromTrashUseCase = restoreFromTrashUseCase,
            permanentlyDeleteUseCase = permanentlyDeleteUseCase,
            createDeleteRequestUseCase = createDeleteRequestUseCase,
            resourceProvider = resourceProvider
        )
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        // When & Then
        assertEquals(TrashUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `dispatchEvent LoadTrash success should set Content state`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val photo = Photo(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L)
        val photos = listOf(photo)

        coEvery { getTrashPhotosUseCase() } returns Result.success(photos)

        // When & Then
        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

            val contentState = awaitItem() as TrashUiState.Content
            assertEquals(photos, contentState.deletePhotos)

            coVerify(exactly = 1) { getTrashPhotosUseCase() }
        }
    }

    @Test
    fun `dispatchEvent LoadTrash failure should set Error state`() = runTest {
        // Given
        val errorMessage = "Failed to load trash photos"
        val exception = Exception("Failed to load trash photos")

        coEvery { getTrashPhotosUseCase() } returns Result.failure(exception)
        every { resourceProvider.getString(R.string.failed_to_load_trash_photos) } returns errorMessage

        // When & Then
        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

            val errorState = awaitItem() as TrashUiState.Error
            assertEquals(errorMessage, errorState.message)

            coVerify(exactly = 1) { getTrashPhotosUseCase() }
            coVerify(exactly = 1) { resourceProvider.getString(R.string.failed_to_load_trash_photos) }
        }
    }

    @Test
    fun `dispatchEvent RestorePhoto should restore photo and update Content state`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val photo1 = Photo(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L)
        val photo2 = Photo(id = 2L, uri = mockUri, dateAdded = 1100L, size = 3000L)
        val photos = listOf(photo1, photo2)

        coEvery { getTrashPhotosUseCase() } returns Result.success(photos)
        coEvery { restoreFromTrashUseCase(listOf(1L)) } returns Unit

        // When & Then
        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
            val initialContent = awaitItem() as TrashUiState.Content
            assertEquals(photos, initialContent.deletePhotos)

            viewModel.dispatchEvent(TrashUiEvent.RestorePhoto(photoId = 1L))
            val updatedContent = awaitItem() as TrashUiState.Content
            assertEquals(listOf(photo2), updatedContent.deletePhotos)

            coVerify(exactly = 1) { restoreFromTrashUseCase(listOf(1L)) }
        }
    }

    @Test
    fun `dispatchEvent ConfirmDeletion with empty list should do nothing`() = runTest {
        // Given
        coEvery { getTrashPhotosUseCase() } returns Result.success(emptyList())

        // When & Then
        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
            val initialContent = awaitItem() as TrashUiState.Content
            assertEquals(emptyList<Photo>(), initialContent.deletePhotos)

            viewModel.dispatchEvent(TrashUiEvent.ConfirmDeletion)

            coVerify(exactly = 0) { createDeleteRequestUseCase(any()) }
            coVerify(exactly = 0) { permanentlyDeleteUseCase(any()) }
        }
    }

    @Test
    fun `dispatchEvent ConfirmDeletion with pending intent should show delete confirmation effect`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val photo1 = Photo(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L)
        val photos = listOf(photo1)
        val mockPendingIntent = mockk<PendingIntent>()
        val mockIntentSender = mockk<IntentSender>()

        coEvery { getTrashPhotosUseCase() } returns Result.success(photos)
        every { createDeleteRequestUseCase(listOf(1L)) } returns mockPendingIntent
        every { mockPendingIntent.intentSender } returns mockIntentSender

        // When & Then
        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
            awaitItem() // Skip Content state emissions

            viewModel.uiEffect.test {
                viewModel.dispatchEvent(TrashUiEvent.ConfirmDeletion)

                val effect = awaitItem() as TrashUiEffect.ShowDeleteConfirmation
                assertEquals(mockIntentSender, effect.intentSender)
            }

            coVerify(exactly = 1) { createDeleteRequestUseCase(listOf(1L)) }
            coVerify(exactly = 0) { permanentlyDeleteUseCase(any()) }
        }
    }

    @Test
    fun `dispatchEvent ConfirmDeletion without pending intent should permanently delete photos and navigate to congratulations`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val photo1 = Photo(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L)
        val photo2 = Photo(id = 2L, uri = mockUri, dateAdded = 1100L, size = 3000L)
        val photos = listOf(photo1, photo2)

        coEvery { getTrashPhotosUseCase() } returns Result.success(photos)
        every { createDeleteRequestUseCase(listOf(1L, 2L)) } returns null
        coEvery { permanentlyDeleteUseCase(listOf(1L, 2L)) } returns Unit

        // When & Then
        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
            awaitItem() // Skip Content state emissions

            viewModel.uiEffect.test {
                viewModel.dispatchEvent(TrashUiEvent.ConfirmDeletion)

                val effect = awaitItem() as TrashUiEffect.NavigateToCongratulations
                assertEquals(2, effect.deletedCount)
                assertEquals(5000L, effect.deletedSize)
            }

            coVerify(exactly = 1) { createDeleteRequestUseCase(listOf(1L, 2L)) }
            coVerify(exactly = 1) { permanentlyDeleteUseCase(listOf(1L, 2L)) }
        }
    }

    @Test
    fun `dispatchEvent OnDeleteApproved should permanently delete photos and navigate to congratulations`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val photo1 = Photo(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L)
        val photo2 = Photo(id = 2L, uri = mockUri, dateAdded = 1100L, size = 3000L)
        val photos = listOf(photo1, photo2)

        coEvery { getTrashPhotosUseCase() } returns Result.success(photos)
        coEvery { permanentlyDeleteUseCase(listOf(1L, 2L)) } returns Unit

        // When & Then
        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
            awaitItem() // Skip Content state emissions

            viewModel.uiEffect.test {
                viewModel.dispatchEvent(TrashUiEvent.OnDeleteApproved)

                val effect = awaitItem() as TrashUiEffect.NavigateToCongratulations
                assertEquals(2, effect.deletedCount)
                assertEquals(5000L, effect.deletedSize)
            }

            coVerify(exactly = 1) { permanentlyDeleteUseCase(listOf(1L, 2L)) }
        }
    }
}
