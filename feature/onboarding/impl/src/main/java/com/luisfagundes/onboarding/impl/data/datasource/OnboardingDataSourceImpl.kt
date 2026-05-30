package com.luisfagundes.onboarding.impl.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.luisfagundes.core.common.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

internal class OnboardingDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher
) : OnboardingDataSource {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    override fun isOnboardingCompleted(): Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }
        .flowOn(dispatcher)

    override suspend fun setOnboardingCompleted() {
        withContext(dispatcher) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.ONBOARDING_COMPLETED] = true
            }
        }
    }
}
