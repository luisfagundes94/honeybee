package com.luisfagundes.onboarding.impl.domain.usecase

import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.onboarding.impl.domain.repository.OnboardingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class CompleteOnboardingUseCaseTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val repository: OnboardingRepository = mockk()
    private lateinit var useCase: CompleteOnboardingUseCase

    @BeforeEach
    fun setUp() {
        useCase = CompleteOnboardingUseCase(repository)
    }

    @Test
    fun `invoke should call completeOnboarding in repository`() = runTest {
        // Given
        coEvery { repository.completeOnboarding() } returns Unit

        // When
        useCase()

        // Then
        coVerify(exactly = 1) { repository.completeOnboarding() }
    }
}
