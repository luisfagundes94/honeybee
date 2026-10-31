package com.luisfagundes.onboarding.impl.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.luisfagundes.core.common.di.IoDispatcher
import com.luisfagundes.core.common.tools.safeRunCatching
import com.luisfagundes.onboarding.api.domain.repository.OnboardingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

internal class OnboardingRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher
) : OnboardingRepository {

    override fun getOnboardingStatus(): Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences -> preferences[ONBOARDING_COMPLETED] ?: false }
        .flowOn(dispatcher)

    override suspend fun completeOnboarding(): Result<Unit> = withContext(dispatcher) {
        safeRunCatching {
            dataStore.edit { preferences -> preferences[ONBOARDING_COMPLETED] = true }
            Unit
        }
    }
}
