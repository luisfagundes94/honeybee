package com.luisfagundes.honeybee.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.luisfagundes.onboarding.api.domain.usecase.GetOnboardingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase
) : ViewModel() {
    fun isOnboardingCompleted() = getOnboardingStatusUseCase.invoke()
}