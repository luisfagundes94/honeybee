package com.luisfagundes.onboarding.impl.presentation.viewmodel

import app.cash.turbine.test
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.onboarding.impl.domain.usecase.CompleteOnboardingUseCase
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
class PermissionViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val completeOnboardingUseCase: CompleteOnboardingUseCase = mockk()
    private lateinit var viewModel: PermissionViewModel

    @BeforeEach
    fun setUp() {
        viewModel = PermissionViewModel(completeOnboardingUseCase)
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
    fun `dispatchEvent PermissionsDenied should send ShowDeniedMessage effect`() = runTest {
        // When & Then
        viewModel.uiEffect.test {
            viewModel.dispatchEvent(PermissionUiEvent.PermissionsDenied)

            assertEquals(PermissionUiEffect.ShowDeniedMessage, awaitItem())
        }
    }
}
