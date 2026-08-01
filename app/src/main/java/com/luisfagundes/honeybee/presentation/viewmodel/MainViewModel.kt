package com.luisfagundes.honeybee.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.viewmodel.StateViewModel
import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.honeybee.presentation.event.MainUiEvent
import com.luisfagundes.honeybee.presentation.state.MainUiState
import com.luisfagundes.onboarding.api.domain.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val subscriptionProvider: SubscriptionProvider,
) : StateViewModel<MainUiState, MainUiEvent>(MainUiState.Loading) {
    init {
        viewModelScope.launch {
            onboardingRepository.getOnboardingStatus().collect { isCompleted ->
                setState { MainUiState.Content(isCompleted) }
            }
        }
    }

    override fun dispatchEvent(event: MainUiEvent) {
        when (event) {
            MainUiEvent.RefreshSubscription -> refreshSubscription()
        }
    }

    private fun refreshSubscription() {
        viewModelScope.launch { subscriptionProvider.refresh() }
    }
}
