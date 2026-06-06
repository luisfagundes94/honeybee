package com.luisfagundes.onboarding.impl.presentation.viewmodel

import app.cash.turbine.test
import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.onboarding.impl.domain.usecase.CompleteOnboardingUseCase
import com.luisfagundes.onboarding.impl.presentation.effect.PermissionUiEffect
import com.luisfagundes.onboarding.impl.presentation.event.PermissionUiEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val completeOnboardingUseCase: CompleteOnboardingUseCase = mockk()
    private val subscriptionProvider: SubscriptionProvider = mockk()
    private lateinit var viewModel: PermissionViewModel

    @BeforeEach
    fun setUp() {
        viewModel = PermissionViewModel(
            completeOnboardingUseCase = completeOnboardingUseCase,
            subscriptionProvider = subscriptionProvider
        )
    }

    @Test
    fun `dispatchEvent PermissionsGranted should complete onboarding and navigate to library`() = runTest {
        // Given
        coEvery { completeOnboardingUseCase() } returns Unit

        // When & Then
        viewModel.uiEffect.test {
            viewModel.dispatchEvent(PermissionUiEvent.PermissionsGranted)

            assertEquals(PermissionUiEffect.NavigateToLibrary, awaitItem())
            coVerify(exactly = 1) { completeOnboardingUseCase() }
        }
    }

    @Test
    fun `dispatchEvent PermissionsDenied with shouldShowRationale true should send ShowDeniedMessage effect`() = runTest {
        // When & Then
        viewModel.uiEffect.test {
            viewModel.dispatchEvent(PermissionUiEvent.PermissionsDenied(shouldShowRationale = true))

            assertEquals(PermissionUiEffect.ShowDeniedMessage, awaitItem())
        }
    }

    @Test
    fun `dispatchEvent PermissionsDenied with shouldShowRationale false should send ShowSettingsDialog effect`() = runTest {
        // When & Then
        viewModel.uiEffect.test {
            viewModel.dispatchEvent(PermissionUiEvent.PermissionsDenied(shouldShowRationale = false))

            assertEquals(PermissionUiEffect.ShowSettingsDialog, awaitItem())
        }
    }

    @Test
    fun `isPremium should return true when subscription provider returns true`() {
        // Given
        every { subscriptionProvider.isPremium() } returns true

        // When
        val result = viewModel.isPremium

        // Then
        assertTrue(result)
    }

    @Test
    fun `isPremium should return false when subscription provider returns false`() {
        // Given
        every { subscriptionProvider.isPremium() } returns false

        // When
        val result = viewModel.isPremium

        // Then
        assertFalse(result)
    }
}
