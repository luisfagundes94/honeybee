package com.luisfagundes.core.common.domain.preferences

import kotlinx.coroutines.flow.Flow

interface UserPreferences {
    fun notificationsEnabled(): Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)
}
