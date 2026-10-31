package com.luisfagundes.onboarding.impl.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.luisfagundes.core.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
internal class OnboardingRepositoryImplTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @TempDir
    lateinit var tempDir: File

    private fun repository(dataStore: DataStore<Preferences>) = OnboardingRepositoryImpl(
        dataStore = dataStore,
        dispatcher = dispatcherRule.testDispatcher
    )

    private fun realDataStore() = PreferenceDataStoreFactory.create(
        produceFile = { File(tempDir, "onboarding.preferences_pb") }
    )

    @Test
    fun `getOnboardingStatus defaults to false`() = runTest {
        // Given
        val repository = repository(realDataStore())

        // When & Then
        assertFalse(repository.getOnboardingStatus().first())
    }

    @Test
    fun `completeOnboarding persists completed status`() = runTest {
        // Given
        val repository = repository(realDataStore())

        // When
        val result = repository.completeOnboarding()

        // Then
        assertTrue(result.isSuccess)
        assertTrue(repository.getOnboardingStatus().first())
    }

    @Test
    fun `getOnboardingStatus falls back to false on IOException`() = runTest {
        // Given
        val dataStore: DataStore<Preferences> = mockk {
            every { data } returns flow { throw IOException("boom") }
        }

        // When & Then
        repository(dataStore).getOnboardingStatus().test {
            assertEquals(false, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `completeOnboarding returns failure when dataStore fails`() = runTest {
        // Given
        val dataStore: DataStore<Preferences> = mockk {
            every { data } returns flow { throw IOException("boom") }
        }
        coEvery { dataStore.updateData(any()) } throws IOException("boom")

        // When
        val result = repository(dataStore).completeOnboarding()

        // Then
        assertTrue(result.isFailure)
    }
}
