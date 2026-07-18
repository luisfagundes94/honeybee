package com.luisfagundes.onboarding.impl.presentation.viewmodel

import app.cash.turbine.test
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.onboarding.impl.domain.repository.OnboardingRepository
import com.luisfagundes.onboarding.impl.presentation.effect.PermissionUiEffect
import com.luisfagundes.onboarding.impl.presentation.event.PermissionUiEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class PermissionViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val repository: OnboardingRepository = mockk()
    private lateinit var viewModel: PermissionViewModel

    @BeforeEach
    fun setUp() {
        viewModel = PermissionViewModel(
            repository = repository
        )
    }

    @Test
    fun `dispatchEvent PermissionsGranted should complete onboarding and navigate to library`() = runTest {
        // Given
        coEvery { repository.completeOnboarding() } returns Unit

        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(PermissionUiEvent.PermissionsGranted)

            // Then
            assertEquals(PermissionUiEffect.NavigateToLibrary, awaitItem())
            coVerify(exactly = 1) { repository.completeOnboarding() }
        }
    }

    @Test
    fun `dispatchEvent PermissionsDenied with shouldShowRationale true should send ShowDeniedMessage effect`() = runTest {
        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(PermissionUiEvent.PermissionsDenied(shouldShowRationale = true))

            // Then
            assertEquals(PermissionUiEffect.ShowDeniedMessage, awaitItem())
        }
    }

    @Test
    fun `dispatchEvent PermissionsDenied with shouldShowRationale false should send ShowSettingsDialog effect`() = runTest {
        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(PermissionUiEvent.PermissionsDenied(shouldShowRationale = false))

            // Then
            assertEquals(PermissionUiEffect.ShowSettingsDialog, awaitItem())
        }
    }
}
