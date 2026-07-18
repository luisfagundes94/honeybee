package com.luisfagundes.onboarding.impl.data.repository

import app.cash.turbine.test
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.onboarding.impl.data.datasource.OnboardingDataSource
import io.mockk.coEvery
import io.mockk.coVerify
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
internal class OnboardingRepositoryImplTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val dataSource: OnboardingDataSource = mockk()
    private lateinit var repository: OnboardingRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = OnboardingRepositoryImpl(
            dataSource = dataSource,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `getOnboardingStatus should flow status from dataSource`() = runTest {
        // Given
        val expectedStatus = true
        every { dataSource.isOnboardingCompleted() } returns flowOf(expectedStatus)

        // When & Then
        repository.getOnboardingStatus().test {
            assertEquals(expectedStatus, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `completeOnboarding should call dataSource setOnboardingCompleted`() = runTest {
        // Given
        coEvery { dataSource.setOnboardingCompleted() } returns Unit

        // When
        repository.completeOnboarding()

        // Then
        coVerify(exactly = 1) { dataSource.setOnboardingCompleted() }
    }
}
