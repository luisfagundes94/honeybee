package com.luisfagundes.core.ads.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import com.luisfagundes.core.common.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AdsPreferences @Inject constructor(
    @param:ApplicationContext context: Context,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    private val dataStore: DataStore<Preferences> = androidx.datastore.preferences.core.PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(ADS_PREFERENCES_FILE) },
    )

    suspend fun lastInterstitialShownAt(): Long = withContext(dispatcher) {
        dataStore.data.first()[LAST_INTERSTITIAL_SHOWN_AT] ?: NEVER_SHOWN
    }

    suspend fun setLastInterstitialShownAt(timestampMillis: Long) = withContext(dispatcher) {
        dataStore.edit { preferences ->
            preferences[LAST_INTERSTITIAL_SHOWN_AT] = timestampMillis
        }
    }

    private companion object {
        const val ADS_PREFERENCES_FILE = "ads_preferences"
        const val NEVER_SHOWN = 0L
        val LAST_INTERSTITIAL_SHOWN_AT = longPreferencesKey("last_interstitial_shown_at")
    }
}
