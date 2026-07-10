package com.luisfagundes.library.impl.presentation.viewmodel

import android.app.PendingIntent
import android.content.IntentSender
import android.net.Uri
import app.cash.turbine.test
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.library.impl.R
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.impl.domain.usecase.CreateDeleteRequestUseCase
import com.luisfagundes.library.impl.domain.usecase.GetTrashMediaUseCase
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

    private val getTrashMediaUseCase: GetTrashMediaUseCase = mockk()
    private val restoreFromTrashUseCase: RestoreFromTrashUseCase = mockk()
    private val permanentlyDeleteUseCase: PermanentlyDeleteUseCase = mockk()
    private val createDeleteRequestUseCase: CreateDeleteRequestUseCase = mockk()
    private val resourceProvider: ResourceProvider = mockk()

    private lateinit var viewModel: TrashViewModel

    @BeforeEach
    fun setUp() {
        viewModel = TrashViewModel(
            getTrashMediaUseCase = getTrashMediaUseCase,
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
        val media = Media(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L, isVideo = false)
        val mediaList = listOf(media)

        coEvery { getTrashMediaUseCase() } returns Result.success(mediaList)

        // When & Then
        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

            val contentState = awaitItem() as TrashUiState.Content
            assertEquals(mediaList, contentState.mediaToBeDeleted)

            coVerify(exactly = 1) { getTrashMediaUseCase() }
        }
    }

    @Test
    fun `dispatchEvent LoadTrash failure should set Error state`() = runTest {
        // Given
        val errorMessage = "Failed to load trash media"
        val exception = Exception("Failed to load trash media")

        coEvery { getTrashMediaUseCase() } returns Result.failure(exception)
        every { resourceProvider.getString(R.string.failed_to_load_trash_photos) } returns errorMessage

        // When & Then
        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

            val errorState = awaitItem() as TrashUiState.Error
            assertEquals(errorMessage, errorState.message)

            coVerify(exactly = 1) { getTrashMediaUseCase() }
            coVerify(exactly = 1) { resourceProvider.getString(R.string.failed_to_load_trash_photos) }
        }
    }

    @Test
    fun `dispatchEvent RestoreMedia should restore media and update Content state`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val media1 = Media(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L, isVideo = false)
        val media2 = Media(id = 2L, uri = mockUri, dateAdded = 1100L, size = 3000L, isVideo = true)
        val mediaList = listOf(media1, media2)

        coEvery { getTrashMediaUseCase() } returns Result.success(mediaList)
        coEvery { restoreFromTrashUseCase(listOf(1L)) } returns Unit

        // When & Then
        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
            val initialContent = awaitItem() as TrashUiState.Content
            assertEquals(mediaList, initialContent.mediaToBeDeleted)

            viewModel.dispatchEvent(TrashUiEvent.RestoreMedia(mediaId = 1L))
            val updatedContent = awaitItem() as TrashUiState.Content
            assertEquals(listOf(media2), updatedContent.mediaToBeDeleted)

            coVerify(exactly = 1) { restoreFromTrashUseCase(listOf(1L)) }
        }
    }

    @Test
    fun `dispatchEvent ConfirmDeletion with empty list should do nothing`() = runTest {
        // Given
        coEvery { getTrashMediaUseCase() } returns Result.success(emptyList())

        // When & Then
        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
            val initialContent = awaitItem() as TrashUiState.Content
            assertEquals(emptyList<Media>(), initialContent.mediaToBeDeleted)

            viewModel.dispatchEvent(TrashUiEvent.ConfirmDeletion)

            coVerify(exactly = 0) { createDeleteRequestUseCase(any()) }
            coVerify(exactly = 0) { permanentlyDeleteUseCase(any()) }
        }
    }

    @Test
    fun `dispatchEvent ConfirmDeletion with pending intent should show delete confirmation effect`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val media1 = Media(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L, isVideo = false)
        val mediaList = listOf(media1)
        val mockPendingIntent = mockk<PendingIntent>()
        val mockIntentSender = mockk<IntentSender>()

        coEvery { getTrashMediaUseCase() } returns Result.success(mediaList)
        coEvery { createDeleteRequestUseCase(listOf(1L)) } returns mockPendingIntent
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
    fun `dispatchEvent ConfirmDeletion without pending intent should permanently delete media and navigate to congratulations`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val media1 = Media(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L, isVideo = false)
        val media2 = Media(id = 2L, uri = mockUri, dateAdded = 1100L, size = 3000L, isVideo = true)
        val mediaList = listOf(media1, media2)

        coEvery { getTrashMediaUseCase() } returns Result.success(mediaList)
        coEvery { createDeleteRequestUseCase(listOf(1L, 2L)) } returns null
        coEvery { permanentlyDeleteUseCase(mediaList) } returns Unit

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
            coVerify(exactly = 1) { permanentlyDeleteUseCase(mediaList) }
        }
    }

    @Test
    fun `dispatchEvent ApproveDeletion should permanently delete media and navigate to congratulations`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val media1 = Media(id = 1L, uri = mockUri, dateAdded = 1000L, size = 2000L, isVideo = false)
        val media2 = Media(id = 2L, uri = mockUri, dateAdded = 1100L, size = 3000L, isVideo = true)
        val mediaList = listOf(media1, media2)

        coEvery { getTrashMediaUseCase() } returns Result.success(mediaList)
        coEvery { permanentlyDeleteUseCase(mediaList) } returns Unit

        // When & Then
        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
            awaitItem() // Skip Content state emissions

            viewModel.uiEffect.test {
                viewModel.dispatchEvent(TrashUiEvent.ApproveDeletion)

                val effect = awaitItem() as TrashUiEffect.NavigateToCongratulations
                assertEquals(2, effect.deletedCount)
                assertEquals(5000L, effect.deletedSize)
            }

            coVerify(exactly = 1) { permanentlyDeleteUseCase(mediaList) }
        }
    }
}
