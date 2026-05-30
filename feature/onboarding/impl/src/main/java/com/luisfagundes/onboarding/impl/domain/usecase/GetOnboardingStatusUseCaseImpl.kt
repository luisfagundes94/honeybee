package com.luisfagundes.onboarding.impl.domain.usecase

import com.luisfagundes.onboarding.api.domain.usecase.GetOnboardingStatusUseCase
import com.luisfagundes.onboarding.impl.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class GetOnboardingStatusUseCaseImpl @Inject constructor(
    private val repository: OnboardingRepository
) : GetOnboardingStatusUseCase {
    override fun invoke(): Flow<Boolean> = repository.getOnboardingStatus()
}
