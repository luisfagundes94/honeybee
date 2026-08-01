package com.luisfagundes.config.impl.presentation.viewmodel

import app.cash.turbine.test
import com.luisfagundes.config.impl.presentation.event.StatisticsUiEvent
import com.luisfagundes.config.impl.presentation.state.StatisticsUiState
import com.luisfagundes.config.impl.tools.fakeStatistics
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.library.api.domain.model.Statistics
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class StatisticsViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val repository: LibraryRepository = mockk()

    private lateinit var viewModel: StatisticsViewModel

    @BeforeEach
    fun setUp() {
        viewModel = StatisticsViewModel(
            repository = repository
        )
    }

    @Test
    fun `init should load statistics successfully and set Content state`() = runTest {
        // Given
        val mockStats = fakeStatistics

        coEvery { repository.getStatistics() } returns Result.success(mockStats)

        // When
        viewModel.dispatchEvent(StatisticsUiEvent.LoadStatistics)

        // Then
        viewModel.uiState.test {
            assertEquals(StatisticsUiState.Content(mockStats), awaitItem())
        }
    }

    @Test
    fun `init should fail to load statistics and set Error state`() = runTest {
        // Given
        coEvery { repository.getStatistics() } returns Result.failure(Exception())

        // When
        viewModel.dispatchEvent(StatisticsUiEvent.LoadStatistics)

        // Then
        viewModel.uiState.test {
            assertEquals(StatisticsUiState.Error, awaitItem())
        }
    }

    @Test
    fun `dispatchEvent LoadStatistics should reload statistics successfully`() = runTest {
        // Given
        val mockStats1 = fakeStatistics
        val mockStats2 = Statistics(
            memoryCleared = 2_048L,
            mediaDeleted = 10,
            photosDeleted = 6,
            videosDeleted = 4
        )

        coEvery { repository.getStatistics() } returns Result.success(mockStats1) andThen Result.success(mockStats2)
        viewModel.dispatchEvent(StatisticsUiEvent.LoadStatistics)

        viewModel.uiState.test {
            assertEquals(StatisticsUiState.Content(mockStats1), awaitItem())

            // When
            viewModel.dispatchEvent(StatisticsUiEvent.LoadStatistics)

            // Then
            assertEquals(StatisticsUiState.Content(mockStats2), expectMostRecentItem())
        }
    }
}
