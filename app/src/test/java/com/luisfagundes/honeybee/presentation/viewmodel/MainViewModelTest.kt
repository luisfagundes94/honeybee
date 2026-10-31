package com.luisfagundes.honeybee.presentation.viewmodel

import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.honeybee.presentation.event.MainUiEvent
import com.luisfagundes.honeybee.presentation.state.MainUiState
import com.luisfagundes.onboarding.api.domain.repository.OnboardingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class MainViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val onboardingStatus = MutableStateFlow(false)
    private val subscriptionProvider: SubscriptionProvider = mockk {
        every { status } returns MutableStateFlow(com.luisfagundes.core.common.provider.SubscriptionStatus.Free)
    }
    private val onboardingRepository: OnboardingRepository = mockk {
        every { getOnboardingStatus() } returns onboardingStatus
    }

    private lateinit var viewModel: MainViewModel

    @BeforeEach
    fun setUp() {
        viewModel = MainViewModel(onboardingRepository, subscriptionProvider)
    }

    @Test
    fun `onboarding status updates main content state`() = runTest {
        // Then
        assertEquals(MainUiState.Content(isOnboardingCompleted = false), viewModel.uiState.value)

        // When
        onboardingStatus.value = true

        // Then
        assertEquals(MainUiState.Content(isOnboardingCompleted = true), viewModel.uiState.value)
    }

    @Test
    fun `onboarding status failure falls back to onboarding content`() = runTest {
        // Given
        every { onboardingRepository.getOnboardingStatus() } returns flow<Boolean> {
            throw IllegalStateException("boom")
        }

        // When
        val failingViewModel = MainViewModel(onboardingRepository, subscriptionProvider)

        // Then
        assertEquals(MainUiState.Content(isOnboardingCompleted = false), failingViewModel.uiState.value)
    }

    @Test
    fun `refresh subscription failure does not crash`() = runTest {
        // Given
        coEvery { subscriptionProvider.refresh() } throws IllegalStateException("boom")

        // When
        viewModel.dispatchEvent(MainUiEvent.RefreshSubscription)

        // Then
        coVerify(exactly = 1) { subscriptionProvider.refresh() }
    }

    @Test
    fun `refresh subscription event delegates to subscription provider`() = runTest {
        // Given
        coEvery { subscriptionProvider.refresh() } returns Unit

        // When
        viewModel.dispatchEvent(MainUiEvent.RefreshSubscription)

        // Then
        coVerify(exactly = 1) { subscriptionProvider.refresh() }
    }
}
