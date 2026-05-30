package com.luisfagundes.onboarding.impl.domain.usecase

import com.luisfagundes.onboarding.impl.domain.repository.OnboardingRepository
import javax.inject.Inject

internal class CompleteOnboardingUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke() = repository.completeOnboarding()
}
