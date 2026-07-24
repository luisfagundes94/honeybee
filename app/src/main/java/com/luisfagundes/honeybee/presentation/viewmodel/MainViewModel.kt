package com.luisfagundes.honeybee.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.onboarding.api.domain.usecase.GetOnboardingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    private val subscriptionProvider: SubscriptionProvider,
) : ViewModel() {
    fun isOnboardingCompleted() = getOnboardingStatusUseCase.invoke()

    fun refreshSubscription() {
        viewModelScope.launch { subscriptionProvider.refresh() }
    }
}
