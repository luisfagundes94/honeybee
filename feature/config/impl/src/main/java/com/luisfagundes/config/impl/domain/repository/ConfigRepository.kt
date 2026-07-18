package com.luisfagundes.config.impl.domain.repository

import kotlinx.coroutines.flow.Flow

internal interface ConfigRepository {
    fun notificationsEnabled(): Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit>
}
