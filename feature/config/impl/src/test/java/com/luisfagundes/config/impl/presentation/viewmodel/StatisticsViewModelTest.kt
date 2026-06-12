package com.luisfagundes.config.impl.presentation.viewmodel

import app.cash.turbine.test
import com.luisfagundes.config.impl.presentation.event.StatisticsUiEvent
import com.luisfagundes.config.impl.presentation.state.StatisticsUiState
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.library.api.domain.model.Statistics
import com.luisfagundes.library.api.domain.usecase.GetStatisticsUseCase
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

    private val getStatisticsUseCase: GetStatisticsUseCase = mockk()
    private val resourceProvider: ResourceProvider = mockk()
    private lateinit var viewModel: StatisticsViewModel

    @BeforeEach
    fun setUp() {
        viewModel = StatisticsViewModel(
            getStatisticsUseCase = getStatisticsUseCase,
            resourceProvider = resourceProvider
        )
    }

    @Test
    fun `init should load statistics successfully and set Content state`() = runTest {
        // Given
        val mockStats = Statistics(
            memoryCleared = 1024L,
            mediaDeleted = 5,
            photosDeleted = 3,
            videosDeleted = 2
        )
        coEvery { getStatisticsUseCase() } returns Result.success(mockStats)

        // When
        viewModel.dispatchEvent(StatisticsUiEvent.LoadStatistics)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(StatisticsUiState.Content(mockStats), state)
        }
    }

    @Test
    fun `init should fail to load statistics and set Error state`() = runTest {
        // Given
        val errorMessage = "Error loading stats"
        coEvery { getStatisticsUseCase() } returns Result.failure(Exception())
        coEvery { resourceProvider.getString(any()) } returns errorMessage

        // When
        viewModel.dispatchEvent(StatisticsUiEvent.LoadStatistics)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(StatisticsUiState.Error(errorMessage), state)
        }
    }

    @Test
    fun `dispatchEvent LoadStatistics should reload statistics successfully`() = runTest {
        // Given
        val mockStats1 = Statistics(1024L, 5, 3, 2)
        val mockStats2 = Statistics(2048L, 10, 6, 4)
        coEvery { getStatisticsUseCase() } returns Result.success(mockStats1) andThen Result.success(mockStats2)
        viewModel.dispatchEvent(StatisticsUiEvent.LoadStatistics)

        // When & Then
        viewModel.uiState.test {
            // First item from initialization
            assertEquals(StatisticsUiState.Content(mockStats1), awaitItem())

            // Trigger LoadStatistics again
            viewModel.dispatchEvent(StatisticsUiEvent.LoadStatistics)

            // Should eventually emit the updated Content state with mockStats2
            val finalItem = expectMostRecentItem()
            assertEquals(StatisticsUiState.Content(mockStats2), finalItem)
        }
    }
}
