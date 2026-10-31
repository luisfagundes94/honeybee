package com.luisfagundes.library.impl.presentation.viewmodel

import app.cash.turbine.test
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.core.common.provider.SubscriptionStatus
import com.luisfagundes.library.api.domain.model.DeletionQuota
import com.luisfagundes.library.api.domain.model.MediaDeleteRequest
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
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val subscriptionStatus = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.Premium)
    private val subscriptionProvider: SubscriptionProvider = mockk {
        every { status } returns subscriptionStatus
    }

    private lateinit var viewModel: TrashViewModel

    private val secondMedia = fakeMedia.copy(
        id = 2L,
        dateAdded = 1_100L,
        size = 3_000L,
        isVideo = true,
    )

    @BeforeEach
    fun setUp() {
        viewModel = TrashViewModel(
            repository = repository,
            subscriptionProvider = subscriptionProvider
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
        val mediaList = listOf(fakeMedia, secondMedia)

        coEvery { repository.getTrashMedia() } returns Result.success(mediaList)
        coEvery { repository.restoreFromTrash(listOf(1L)) } returns Result.success(Unit)

        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

            // Then
            assertEquals(TrashUiState.Content(mediaList), awaitItem())

            // When
            viewModel.dispatchEvent(TrashUiEvent.RestoreMedia(mediaId = 1L))

            // Then
            assertEquals(TrashUiState.Content(listOf(secondMedia)), awaitItem())
            coVerify(exactly = 1) { repository.restoreFromTrash(listOf(1L)) }
        }
    }

    @Test
    fun `clearing trash restores all media without permanently deleting it`() = runTest {
        // Given
        val mediaList = listOf(fakeMedia, secondMedia)
        coEvery { repository.getTrashMedia() } returns Result.success(mediaList)
        coEvery { repository.restoreFromTrash(listOf(1L, 2L)) } returns Result.success(Unit)
        viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

        viewModel.uiState.test {
            assertEquals(TrashUiState.Content(mediaList), awaitItem())

            // When
            viewModel.dispatchEvent(TrashUiEvent.ClearTrash)

            // Then
            assertEquals(TrashUiState.Content(emptyList()), awaitItem())
            coVerify(exactly = 1) { repository.restoreFromTrash(listOf(1L, 2L)) }
            coVerify(exactly = 0) { repository.createDeleteRequest(any()) }
            coVerify(exactly = 0) { repository.permanentlyDelete(any()) }
        }
    }

    @Test
    fun `clearing empty trash does not restore media`() = runTest {
        // Given
        coEvery { repository.getTrashMedia() } returns Result.success(emptyList())
        viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

        // When
        viewModel.dispatchEvent(TrashUiEvent.ClearTrash)

        // Then
        assertEquals(TrashUiState.Content(emptyList()), viewModel.uiState.value)
        coVerify(exactly = 0) { repository.restoreFromTrash(any()) }
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
        val deleteRequest = MediaDeleteRequest(listOf(fakeMedia.uri))

        coEvery { repository.getTrashMedia() } returns Result.success(mediaList)
        coEvery { repository.createDeleteRequest(listOf(1L)) } returns Result.success(deleteRequest)

        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            // When & Then
            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
            awaitItem() // Skip Content state emissions

            viewModel.uiEffect.test {
                // When
                viewModel.dispatchEvent(TrashUiEvent.ConfirmDeletion)

                // Then
                assertEquals(TrashUiEffect.ShowDeleteConfirmation(deleteRequest), awaitItem())
            }

            coVerify(exactly = 1) { repository.createDeleteRequest(listOf(1L)) }
            coVerify(exactly = 0) { repository.permanentlyDelete(any()) }
        }
    }

    @Test
    fun `dispatchEvent ConfirmDeletion failure should set Error state`() = runTest {
        // Given
        val exception = IllegalStateException("Unable to create delete request")
        coEvery { repository.getTrashMedia() } returns Result.success(listOf(fakeMedia))
        coEvery { repository.createDeleteRequest(listOf(1L)) } returns Result.failure(exception)

        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())

            // When
            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

            // Then
            awaitItem()

            // When
            viewModel.dispatchEvent(TrashUiEvent.ConfirmDeletion)

            // Then
            assertEquals(TrashUiState.Error, awaitItem())
        }
    }

    @Test
    fun `deletion without pending intent removes media and shows congratulations`() = runTest {
        // Given
        val mediaList = listOf(fakeMedia, secondMedia)

        coEvery { repository.getTrashMedia() } returns Result.success(mediaList)
        coEvery { repository.createDeleteRequest(listOf(1L, 2L)) } returns Result.success(null)
        coEvery { repository.permanentlyDelete(mediaList) } returns Result.success(Unit)

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
    fun `dispatchEvent ApproveDeletion shows error and does not navigate when deletion fails`() = runTest {
        // Given
        val mediaList = listOf(fakeMedia, secondMedia)

        coEvery { repository.getTrashMedia() } returns Result.success(mediaList)
        coEvery { repository.permanentlyDelete(mediaList) } returns Result.failure(IllegalStateException())

        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())
            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
            awaitItem() // Skip Content state emissions

            // When
            viewModel.dispatchEvent(TrashUiEvent.ApproveDeletion)

            // Then
            assertEquals(TrashUiState.Error, awaitItem())
        }
    }

    @Test
    fun `dispatchEvent RestoreMedia shows error when restore fails`() = runTest {
        // Given
        coEvery { repository.getTrashMedia() } returns Result.success(listOf(fakeMedia))
        coEvery { repository.restoreFromTrash(listOf(1L)) } returns Result.failure(IllegalStateException())

        viewModel.uiState.test {
            assertEquals(TrashUiState.Loading, awaitItem())
            viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
            awaitItem() // Skip Content state emissions

            // When
            viewModel.dispatchEvent(TrashUiEvent.RestoreMedia(1L))

            // Then
            assertEquals(TrashUiState.Error, awaitItem())
        }
    }

    @Test
    fun `dispatchEvent ApproveDeletion should permanently delete media and navigate to congratulations`() = runTest {
        // Given
        val mediaList = listOf(fakeMedia, secondMedia)

        coEvery { repository.getTrashMedia() } returns Result.success(mediaList)
        coEvery { repository.permanentlyDelete(mediaList) } returns Result.success(Unit)

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

    @Test
    fun `premium users get an unlimited quota without querying the repository`() = runTest {
        // Given
        coEvery { repository.getTrashMedia() } returns Result.success(listOf(fakeMedia))

        // When
        viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

        // Then
        assertEquals(
            TrashUiState.Content(listOf(fakeMedia), DeletionQuota.Unlimited),
            viewModel.uiState.value
        )
        coVerify(exactly = 0) { repository.getDeletionQuota() }
    }

    @Test
    fun `free users load the quota from the repository`() = runTest {
        // Given
        subscriptionStatus.value = SubscriptionStatus.Free
        coEvery { repository.getTrashMedia() } returns Result.success(listOf(fakeMedia))
        coEvery { repository.getDeletionQuota() } returns Result.success(DeletionQuota.Available(5))

        // When
        viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

        // Then
        assertEquals(
            TrashUiState.Content(listOf(fakeMedia), DeletionQuota.Available(5)),
            viewModel.uiState.value
        )
    }

    @Test
    fun `LoadTrash sets Error when the quota cannot be loaded`() = runTest {
        // Given
        subscriptionStatus.value = SubscriptionStatus.Free
        coEvery { repository.getTrashMedia() } returns Result.success(listOf(fakeMedia))
        coEvery { repository.getDeletionQuota() } returns Result.failure(IllegalStateException())

        // When
        viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

        // Then
        assertEquals(TrashUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun `ConfirmDeletion within the remaining quota requests the deletion`() = runTest {
        // Given
        subscriptionStatus.value = SubscriptionStatus.Free
        val deleteRequest = MediaDeleteRequest(listOf(fakeMedia.uri))
        coEvery { repository.getTrashMedia() } returns Result.success(listOf(fakeMedia))
        coEvery { repository.getDeletionQuota() } returns Result.success(DeletionQuota.Available(1))
        coEvery { repository.createDeleteRequest(listOf(1L)) } returns Result.success(deleteRequest)
        viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(TrashUiEvent.ConfirmDeletion)

            // Then
            assertEquals(TrashUiEffect.ShowDeleteConfirmation(deleteRequest), awaitItem())
        }
    }

    @Test
    fun `ConfirmDeletion above the remaining quota shows the daily limit sheet`() = runTest {
        // Given
        subscriptionStatus.value = SubscriptionStatus.Free
        coEvery { repository.getTrashMedia() } returns Result.success(listOf(fakeMedia, secondMedia))
        coEvery { repository.getDeletionQuota() } returns Result.success(DeletionQuota.Available(1))
        viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(TrashUiEvent.ConfirmDeletion)

            // Then
            assertEquals(TrashUiEffect.ShowDailyLimitSheet(remaining = 1), awaitItem())
        }
        coVerify(exactly = 0) { repository.createDeleteRequest(any()) }
        coVerify(exactly = 0) { repository.permanentlyDelete(any()) }
    }

    @Test
    fun `ConfirmDeletion while locked navigates to premium`() = runTest {
        // Given
        subscriptionStatus.value = SubscriptionStatus.Free
        coEvery { repository.getTrashMedia() } returns Result.success(listOf(fakeMedia))
        coEvery { repository.getDeletionQuota() } returns Result.success(DeletionQuota.Locked(9_000L))
        viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(TrashUiEvent.ConfirmDeletion)

            // Then
            assertEquals(TrashUiEffect.NavigateToPremium, awaitItem())
        }
        coVerify(exactly = 0) { repository.createDeleteRequest(any()) }
        coVerify(exactly = 0) { repository.permanentlyDelete(any()) }
    }

    @Test
    fun `DeleteWithinLimit deletes only the oldest media that fit the quota`() = runTest {
        // Given
        subscriptionStatus.value = SubscriptionStatus.Free
        coEvery { repository.getTrashMedia() } returns Result.success(listOf(secondMedia, fakeMedia))
        coEvery { repository.getDeletionQuota() } returns Result.success(DeletionQuota.Available(1))
        coEvery { repository.createDeleteRequest(listOf(1L)) } returns Result.success(null)
        coEvery { repository.permanentlyDelete(listOf(fakeMedia)) } returns Result.success(Unit)
        viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(TrashUiEvent.DeleteWithinLimit)

            // Then
            assertEquals(TrashUiEffect.NavigateToCongratulations(1, 2_000L), awaitItem())
        }
        coVerify(exactly = 1) { repository.permanentlyDelete(listOf(fakeMedia)) }
    }

    @Test
    fun `ApproveDeletion deletes only the media chosen by DeleteWithinLimit`() = runTest {
        // Given
        subscriptionStatus.value = SubscriptionStatus.Free
        val deleteRequest = MediaDeleteRequest(listOf(fakeMedia.uri))
        coEvery { repository.getTrashMedia() } returns Result.success(listOf(fakeMedia, secondMedia))
        coEvery { repository.getDeletionQuota() } returns Result.success(DeletionQuota.Available(1))
        coEvery { repository.createDeleteRequest(listOf(1L)) } returns Result.success(deleteRequest)
        coEvery { repository.permanentlyDelete(listOf(fakeMedia)) } returns Result.success(Unit)
        viewModel.dispatchEvent(TrashUiEvent.LoadTrash)

        viewModel.uiEffect.test {
            viewModel.dispatchEvent(TrashUiEvent.DeleteWithinLimit)
            assertEquals(TrashUiEffect.ShowDeleteConfirmation(deleteRequest), awaitItem())

            // When
            viewModel.dispatchEvent(TrashUiEvent.ApproveDeletion)

            // Then
            assertEquals(TrashUiEffect.NavigateToCongratulations(1, 2_000L), awaitItem())
        }
        coVerify(exactly = 1) { repository.permanentlyDelete(listOf(fakeMedia)) }
    }

    @Test
    fun `QuotaExpired reloads the quota`() = runTest {
        // Given
        subscriptionStatus.value = SubscriptionStatus.Free
        coEvery { repository.getTrashMedia() } returns Result.success(listOf(fakeMedia))
        coEvery { repository.getDeletionQuota() } returnsMany listOf(
            Result.success(DeletionQuota.Locked(9_000L)),
            Result.success(DeletionQuota.Available(25))
        )
        viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
        assertEquals(
            TrashUiState.Content(listOf(fakeMedia), DeletionQuota.Locked(9_000L)),
            viewModel.uiState.value
        )

        // When
        viewModel.dispatchEvent(TrashUiEvent.QuotaExpired)

        // Then
        assertEquals(
            TrashUiState.Content(listOf(fakeMedia), DeletionQuota.Available(25)),
            viewModel.uiState.value
        )
    }
}
