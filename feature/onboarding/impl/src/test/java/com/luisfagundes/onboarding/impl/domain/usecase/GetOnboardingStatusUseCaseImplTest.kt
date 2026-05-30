package com.luisfagundes.onboarding.impl.domain.usecase

import app.cash.turbine.test
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.onboarding.impl.domain.repository.OnboardingRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class GetOnboardingStatusUseCaseImplTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val repository: OnboardingRepository = mockk()
    private lateinit var useCase: GetOnboardingStatusUseCaseImpl

    @BeforeEach
    fun setUp() {
        useCase = GetOnboardingStatusUseCaseImpl(repository)
    }

    @Test
    fun `invoke should flow status from repository`() = runTest {
        // Given
        val expectedStatus = true
        every { repository.getOnboardingStatus() } returns flowOf(expectedStatus)

        // When & Then
        useCase().test {
            assertEquals(expectedStatus, awaitItem())
            awaitComplete()
        }
    }
}
