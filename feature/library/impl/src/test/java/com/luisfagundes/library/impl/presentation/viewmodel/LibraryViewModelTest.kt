package com.luisfagundes.library.impl.presentation.viewmodel

import android.net.Uri
import app.cash.turbine.test
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.library.impl.R
import com.luisfagundes.library.impl.domain.model.Media
import com.luisfagundes.library.impl.domain.model.MediaSection
import com.luisfagundes.library.impl.domain.usecase.GetItemsInTrashCountUseCase
import com.luisfagundes.library.impl.domain.usecase.GetMediaByMonthUseCase
import com.luisfagundes.library.impl.presentation.effect.LibraryUiEffect
import com.luisfagundes.library.impl.presentation.event.LibraryUiEvent
import com.luisfagundes.library.impl.presentation.state.LibraryUiState
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
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val getMediaByMonthUseCase: GetMediaByMonthUseCase = mockk()
    private val getItemsInTrashCountUseCase: GetItemsInTrashCountUseCase = mockk()
    private val resourceProvider: ResourceProvider = mockk()

    private lateinit var viewModel: LibraryViewModel

    @BeforeEach
    fun setUp() {
        viewModel = LibraryViewModel(
            getMediaByMonthUseCase = getMediaByMonthUseCase,
            getItemsInTrashCountUseCase = getItemsInTrashCountUseCase,
            resourceProvider = resourceProvider
        )
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        // When & Then
        assertEquals(LibraryUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `dispatchEvent LoadMedia success should set Content state`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val media = Media(
            id = 1L,
            uri = mockUri,
            dateAdded = 1000L,
            size = 2000L,
            isVideo = false
        )
        val mediaSections = listOf(
            MediaSection(
                yearMonth = YearMonth.of(2026, 6),
                mediaList = listOf(media)
            )
        )
        val trashCount = 5

        coEvery { getItemsInTrashCountUseCase() } returns trashCount
        coEvery { getMediaByMonthUseCase() } returns Result.success(mediaSections)

        // When & Then
        viewModel.uiState.test {
            assertEquals(LibraryUiState.Loading, awaitItem())

            viewModel.dispatchEvent(LibraryUiEvent.LoadMedia)

            val contentState = awaitItem() as LibraryUiState.Content
            assertEquals(mediaSections, contentState.mediaSectionList)
            assertEquals(trashCount, contentState.itemsInTrash)

            coVerify(exactly = 1) { getItemsInTrashCountUseCase() }
            coVerify(exactly = 1) { getMediaByMonthUseCase() }
        }
    }

    @Test
    fun `dispatchEvent LoadMedia failure should set Error state`() = runTest {
        // Given
        val errorMessage = "Failed to load media"
        val exception = Exception("Network error")

        coEvery { getItemsInTrashCountUseCase() } returns 2
        coEvery { getMediaByMonthUseCase() } returns Result.failure(exception)
        every { resourceProvider.getString(R.string.error_loading_photos_message) } returns errorMessage

        // When & Then
        viewModel.uiState.test {
            assertEquals(LibraryUiState.Loading, awaitItem())

            viewModel.dispatchEvent(LibraryUiEvent.LoadMedia)

            val errorState = awaitItem() as LibraryUiState.Error
            assertEquals(errorMessage, errorState.message)

            coVerify(exactly = 1) { getItemsInTrashCountUseCase() }
            coVerify(exactly = 1) { getMediaByMonthUseCase() }
            coVerify(exactly = 1) { resourceProvider.getString(R.string.error_loading_photos_message) }
        }
    }

    @Test
    fun `dispatchEvent TrashClick should emit NavigateToTrash effect`() = runTest {
        // When & Then
        viewModel.uiEffect.test {
            viewModel.dispatchEvent(LibraryUiEvent.TrashClick)

            assertEquals(LibraryUiEffect.NavigateToTrash, awaitItem())
        }
    }

    @Test
    fun `dispatchEvent MediaClick should emit NavigateToMediaDetail effect`() = runTest {
        // Given
        val mediaId = 123L

        // When & Then
        viewModel.uiEffect.test {
            viewModel.dispatchEvent(LibraryUiEvent.MediaClick(mediaId))

            assertEquals(LibraryUiEffect.NavigateToMediaDetail(mediaId), awaitItem())
        }
    }
}
