package com.luisfagundes.library.impl.presentation.viewmodel

import android.app.PendingIntent
import android.content.IntentSender
import app.cash.turbine.test
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import com.luisfagundes.library.impl.presentation.effect.TrashUiEffect
import com.luisfagundes.library.impl.presentation.event.TrashUiEvent
import com.luisfagundes.library.impl.presentation.state.TrashUiState
import com.luisfagundes.library.impl.tools.fakeMedia
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
internal class TrashViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val repository: LibraryRepository = mockk()

    private lateinit var viewModel: TrashViewModel

    @BeforeEach
    fun setUp() {
        viewModel = TrashViewModel(
            repository = repository
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
        val mediaList = listOf(fakeMedia)

        coEvery { repository.getTrashMedia() } returns Result.success(mediaList)

        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

            // Then
            assertEquals(TrashUiState.Content(mediaList), awaitItem())

            coVerify(exactly = 1) { repository.getTrashMedia() }
        }
    }

    @Test
    fun `dispatchEvent LoadTrash failure should set Error state`() = runTest {
        // Given
        val exception = Exception("Failed to load trash media")

        coEvery { repository.getTrashMedia() } returns Result.failure(exception)

        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

            // Then
            assertEquals(TrashUiState.Error, awaitItem() )

            coVerify(exactly = 1) { repository.getTrashMedia() }
        }
    }

    @Test
    fun `dispatchEvent RestoreMedia should restore media and update Content state`() = runTest {
        // Given
        val media1 = fakeMedia
        val media2 = fakeMedia.copy(id = 2L, dateAdded = 1_100L, size = 3_000L, isVideo = true)
        val mediaList = listOf(media1, media2)

        coEvery { repository.getTrashMedia() } returns Result.success(mediaList)
        coEvery { repository.restoreFromTrash(listOf(1L)) } returns Unit

        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

            // Then
            assertEquals(TrashUiState.Content(mediaList), awaitItem())

            // When
            viewModel.dispatchEvent(TrashUiEvent.RestoreMedia(mediaId = 1L))

            // Then
            assertEquals(TrashUiState.Content(listOf(media2)), awaitItem())
            coVerify(exactly = 1) { repository.restoreFromTrash(listOf(1L)) }
        }
    }

    @Test
    fun `dispatchEvent ConfirmDeletion with empty list should do nothing`() = runTest {
        // Given
        coEvery { repository.getTrashMedia() } returns Result.success(emptyList())

        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

            // Then
            assertEquals(TrashUiState.Content(emptyList()), awaitItem())

            // When
            viewModel.dispatchEvent(TrashUiEvent.ConfirmDeletion)

            // Then
            coVerify(exactly = 0) { repository.createDeleteRequest(any()) }
            coVerify(exactly = 0) { repository.permanentlyDelete(any()) }
        }
    }

    @Test
    fun `dispatchEvent ConfirmDeletion with pending intent should show delete confirmation effect`() = runTest {
        // Given
        val mediaList = listOf(fakeMedia)
        val mockPendingIntent = mockk<PendingIntent>()
        val mockIntentSender = mockk<IntentSender>()

        coEvery { repository.getTrashMedia() } returns Result.success(mediaList)
        coEvery { repository.createDeleteRequest(listOf(1L)) } returns mockPendingIntent
        every { mockPendingIntent.intentSender } returns mockIntentSender

        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            // When & Then
            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
            awaitItem() // Skip Content state emissions

            viewModel.uiEffect.test {
                // When
                viewModel.dispatchEvent(TrashUiEvent.ConfirmDeletion)

                // Then
                assertEquals(TrashUiEffect.ShowDeleteConfirmation(mockIntentSender), awaitItem())
            }

            coVerify(exactly = 1) { repository.createDeleteRequest(listOf(1L)) }
            coVerify(exactly = 0) { repository.permanentlyDelete(any()) }
        }
    }

    @Test
    fun `dispatchEvent ConfirmDeletion without pending intent should permanently delete media and navigate to congratulations`() = runTest {
        // Given
        val media1 = fakeMedia
        val media2 = fakeMedia.copy(id = 2L, dateAdded = 1_100L, size = 3_000L, isVideo = true)
        val mediaList = listOf(media1, media2)

        coEvery { repository.getTrashMedia() } returns Result.success(mediaList)
        coEvery { repository.createDeleteRequest(listOf(1L, 2L)) } returns null
        coEvery { repository.permanentlyDelete(mediaList) } returns Unit

        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            // When & Then
            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
            awaitItem() // Skip Content state emissions

            viewModel.uiEffect.test {
                // When
                viewModel.dispatchEvent(TrashUiEvent.ConfirmDeletion)

                // Then
                assertEquals(TrashUiEffect.NavigateToCongratulations(2, 5_000L), awaitItem())
            }

            coVerify(exactly = 1) { repository.createDeleteRequest(listOf(1L, 2L)) }
            coVerify(exactly = 1) { repository.permanentlyDelete(mediaList) }
        }
    }

    @Test
    fun `dispatchEvent ApproveDeletion should permanently delete media and navigate to congratulations`() = runTest {
        // Given
        val media1 = fakeMedia
        val media2 = fakeMedia.copy(id = 2L, dateAdded = 1_100L, size = 3_000L, isVideo = true)
        val mediaList = listOf(media1, media2)

        coEvery { repository.getTrashMedia() } returns Result.success(mediaList)
        coEvery { repository.permanentlyDelete(mediaList) } returns Unit

        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            // When & Then
            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
            awaitItem() // Skip Content state emissions

            viewModel.uiEffect.test {
                // When
                viewModel.dispatchEvent(TrashUiEvent.ApproveDeletion)

                // Then
                assertEquals(TrashUiEffect.NavigateToCongratulations(2, 5_000L), awaitItem())
            }

            coVerify(exactly = 1) { repository.permanentlyDelete(mediaList) }
        }
    }
}
