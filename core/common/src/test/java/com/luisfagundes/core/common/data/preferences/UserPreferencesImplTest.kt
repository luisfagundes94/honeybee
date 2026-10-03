package com.luisfagundes.core.common.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
internal class UserPreferencesImplTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Test
    fun `notificationsEnabled should default to true and emit updated values`() = runTest {
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

    private fun createPreferences(): UserPreferencesImpl = UserPreferencesImpl(
        dataStore = InMemoryPreferencesDataStore(),
        dispatcher = dispatcher,
    )

    private class InMemoryPreferencesDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = preferences

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences,
        ): Preferences {
            return transform(preferences.value).also { preferences.value = it }
        }
    }
}
