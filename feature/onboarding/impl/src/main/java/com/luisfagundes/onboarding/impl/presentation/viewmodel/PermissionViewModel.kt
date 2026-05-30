package com.luisfagundes.onboarding.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.viewmodel.EffectViewModel
import com.luisfagundes.onboarding.impl.domain.usecase.CompleteOnboardingUseCase
import com.luisfagundes.onboarding.impl.presentation.effect.PermissionUiEffect
import com.luisfagundes.onboarding.impl.presentation.event.PermissionUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PermissionViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : EffectViewModel<PermissionUiEffect, PermissionUiEvent>() {

    override fun dispatchEvent(event: PermissionUiEvent) {
        when (event) {
            is PermissionUiEvent.PermissionsGranted -> completeOnboarding()
            is PermissionUiEvent.PermissionsDenied -> sendEffect { PermissionUiEffect.ShowDeniedMessage }
        }
    }

    private fun completeOnboarding() = viewModelScope.launch {
        completeOnboardingUseCase()
        sendEffect { PermissionUiEffect.NavigateToLibrary }
    }
}
