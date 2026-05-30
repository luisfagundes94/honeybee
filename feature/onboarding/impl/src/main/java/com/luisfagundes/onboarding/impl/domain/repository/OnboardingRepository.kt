package com.luisfagundes.onboarding.impl.domain.repository

import kotlinx.coroutines.flow.Flow

internal interface OnboardingRepository {
    fun getOnboardingStatus(): Flow<Boolean>
    suspend fun completeOnboarding()
}
