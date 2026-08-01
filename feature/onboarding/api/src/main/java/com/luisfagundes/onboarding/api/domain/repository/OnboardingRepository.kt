package com.luisfagundes.onboarding.api.domain.repository

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun getOnboardingStatus(): Flow<Boolean>
    suspend fun completeOnboarding()
}
