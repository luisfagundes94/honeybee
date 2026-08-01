package com.luisfagundes.config.impl.presentation.viewmodel

import app.cash.turbine.test
import com.luisfagundes.config.impl.domain.repository.ConfigRepository
import com.luisfagundes.config.impl.presentation.effect.ConfigUiEffect
import com.luisfagundes.config.impl.presentation.event.ConfigUiEvent
import com.luisfagundes.config.impl.presentation.state.ConfigUiState
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.core.ads.AdsConfig
import com.luisfagundes.core.ads.AdsCoordinator
import com.luisfagundes.core.ads.AdsState
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
internal class ConfigViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val notificationsEnabled = MutableStateFlow(true)
    private val repository: ConfigRepository = mockk()
    private val adsCoordinator: AdsCoordinator = mockk {
        every { state } returns MutableStateFlow(AdsState())
    }
    private val adsConfig = AdsConfig("banner", "interstitial")
    private lateinit var viewModel: ConfigViewModel

    @BeforeEach
    fun setUp() {
        every { repository.notificationsEnabled() } returns notificationsEnabled
        coEvery { repository.setNotificationsEnabled(any()) } returns Result.success(Unit)
        viewModel = ConfigViewModel(
            repository,
            adsCoordinator,
            adsConfig,
        )
    }

    @Test
    fun `preference changes should update state`() = runTest {
        assertEquals(ConfigUiState.Content(), viewModel.uiState.value)

        // When
        notificationsEnabled.value = false

        // Then
        assertEquals(ConfigUiState.Content(isNotificationsEnabled = false), viewModel.uiState.value)
    }

    @Test
    fun `dispatchEvent NotificationsToggled should persist requested value`() = runTest {
        // When
        viewModel.dispatchEvent(ConfigUiEvent.NotificationsToggled(enabled = false))

        // Then
        coVerify(exactly = 1) { repository.setNotificationsEnabled(false) }
        assertEquals(ConfigUiState.Content(), viewModel.uiState.value)
    }

    @Test
    fun `failed preference update should leave current state unchanged`() = runTest {
        // Given
        coEvery { repository.setNotificationsEnabled(false) } returns Result.failure(IllegalStateException())

        // When
        viewModel.dispatchEvent(ConfigUiEvent.NotificationsToggled(enabled = false))

        // Then
        assertEquals(ConfigUiState.Content(), viewModel.uiState.value)
    }

    @Test
    fun `dispatchEvent StatisticsClick should navigate to statistics`() = runTest {
        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(ConfigUiEvent.StatisticsClick)

            // Then
            assertEquals(ConfigUiEffect.NavigateToStatistics, awaitItem())
        }
    }

    @Test
    fun `dispatchEvent FeedbackClick should navigate to feedback`() = runTest {
        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(ConfigUiEvent.FeedbackClick)

            // Then
            assertEquals(ConfigUiEffect.NavigateToFeedback, awaitItem())
        }
    }
}
