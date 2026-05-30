package com.luisfagundes.onboarding.impl.data.datasource

import kotlinx.coroutines.flow.Flow

internal interface OnboardingDataSource {
    fun isOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted()
}
