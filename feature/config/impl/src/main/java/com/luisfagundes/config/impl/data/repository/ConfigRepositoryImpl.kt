package com.luisfagundes.config.impl.data.repository

import com.luisfagundes.config.impl.domain.repository.ConfigRepository
import com.luisfagundes.core.common.domain.preferences.UserPreferences
import com.luisfagundes.core.common.tools.safeRunCatching
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class ConfigRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences
) : ConfigRepository {

    override fun notificationsEnabled(): Flow<Boolean> =
        userPreferences.notificationsEnabled()

    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> =
        safeRunCatching { userPreferences.setNotificationsEnabled(enabled) }
}
