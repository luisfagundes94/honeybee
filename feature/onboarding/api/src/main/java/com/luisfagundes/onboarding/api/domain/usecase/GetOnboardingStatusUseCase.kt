package com.luisfagundes.onboarding.api.domain.usecase

import kotlinx.coroutines.flow.Flow

interface GetOnboardingStatusUseCase {
    operator fun invoke(): Flow<Boolean>
}
