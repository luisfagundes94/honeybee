package com.luisfagundes.config.impl.data.repository

import app.cash.turbine.test
import com.luisfagundes.core.common.domain.preferences.UserPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ConfigRepositoryImplTest {

    private val userPreferences: UserPreferences = mockk()
    private val repository = ConfigRepositoryImpl(userPreferences)

    @Test
    fun `notificationsEnabled should expose the user preference`() = runTest {
        // Given
        every { userPreferences.notificationsEnabled() } returns flowOf(false)

        repository.notificationsEnabled().test {
            // When & Then
            assertEquals(false, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `setNotificationsEnabled should return success after persisting`() = runTest {
        // Given
        coEvery { userPreferences.setNotificationsEnabled(false) } returns Unit

        // When
        val result = repository.setNotificationsEnabled(false)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { userPreferences.setNotificationsEnabled(false) }
    }

    @Test
    fun `setNotificationsEnabled should return failure when persistence fails`() = runTest {
        // Given
        coEvery { userPreferences.setNotificationsEnabled(false) } throws IllegalStateException()

        // When
        val result = repository.setNotificationsEnabled(false)

        // Then
        assertTrue(result.isFailure)
    }
}
