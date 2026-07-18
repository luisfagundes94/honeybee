package com.luisfagundes.core.common.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.luisfagundes.core.common.di.IoDispatcher
import com.luisfagundes.core.common.domain.preferences.UserPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

internal class UserPreferencesImpl @Inject constructor(
    @param:UserPreferencesDataStore private val dataStore: DataStore<Preferences>,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher
) : UserPreferences {

    override fun notificationsEnabled(): Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: DEFAULT_NOTIFICATIONS_ENABLED
        }
        .flowOn(dispatcher)

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        withContext(dispatcher) {
            dataStore.edit { preferences ->
                preferences[NOTIFICATIONS_ENABLED] = enabled
            }
        }
    }

    private companion object {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        const val DEFAULT_NOTIFICATIONS_ENABLED = true
    }
}
