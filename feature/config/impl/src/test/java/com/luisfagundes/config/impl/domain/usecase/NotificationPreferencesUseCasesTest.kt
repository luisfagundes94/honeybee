package com.luisfagundes.config.impl.domain.usecase

import app.cash.turbine.test
import com.luisfagundes.config.impl.domain.repository.ConfigRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class NotificationPreferencesUseCasesTest {

    private val repository: ConfigRepository = mockk()

    @Test
    fun `observe use case should return repository preference flow`() = runTest {
        // Given
        every { repository.notificationsEnabled() } returns flowOf(false)

        ObserveNotificationsEnabledUseCase(repository)().test {
            // When & Then
            assertEquals(false, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `set use case should return repository result`() = runTest {
        // Given
        coEvery { repository.setNotificationsEnabled(false) } returns Result.success(Unit)

        // When
        val result = SetNotificationsEnabledUseCase(repository)(false)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.setNotificationsEnabled(false) }
    }
}
