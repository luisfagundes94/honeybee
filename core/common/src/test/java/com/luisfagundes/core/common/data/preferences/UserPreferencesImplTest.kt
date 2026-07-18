package com.luisfagundes.core.common.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Path

@OptIn(ExperimentalCoroutinesApi::class)
internal class UserPreferencesImplTest {

    @TempDir
    lateinit var tempDirectory: Path

    private val dispatcher = UnconfinedTestDispatcher()
    private val dataStoreScope = CoroutineScope(dispatcher + SupervisorJob())

    @AfterEach
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun `notificationsEnabled should default to true and persist updates`() = runTest {
        val preferences = createPreferences()

        preferences.notificationsEnabled().test {
            // Then
            assertEquals(true, awaitItem())

            // When
            preferences.setNotificationsEnabled(false)

            // Then
            assertEquals(false, awaitItem())

            // When
            preferences.setNotificationsEnabled(true)

            // Then
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `notificationsEnabled should default to true when reading fails with IOException`() = runTest {
        // Given
        val dataStore = mockk<DataStore<Preferences>>()
        every { dataStore.data } returns flow { throw IOException() }
        val preferences = UserPreferencesImpl(dataStore, dispatcher)

        preferences.notificationsEnabled().test {
            // When & Then
            assertEquals(true, awaitItem())
            awaitComplete()
        }
    }

    private fun createPreferences(): UserPreferencesImpl {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { tempDirectory.resolve("user_preferences.preferences_pb").toFile() }
        )
        return UserPreferencesImpl(dataStore, dispatcher)
    }
}
