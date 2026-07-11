package com.luisfagundes.onboarding.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.viewmodel.EffectViewModel
import com.luisfagundes.onboarding.impl.domain.repository.OnboardingRepository
import com.luisfagundes.onboarding.impl.presentation.effect.PermissionUiEffect
import com.luisfagundes.onboarding.impl.presentation.event.PermissionUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PermissionViewModel @Inject constructor(
    private val repository: OnboardingRepository
) : EffectViewModel<PermissionUiEffect, PermissionUiEvent>() {
    override fun dispatchEvent(event: PermissionUiEvent) {
        when (event) {
            is PermissionUiEvent.PermissionsGranted -> completeOnboarding()
            is PermissionUiEvent.PermissionsDenied -> handlePermissionsDenied(event)
        }
    }

    private fun completeOnboarding() = viewModelScope.launch {
        repository.completeOnboarding()
        sendEffect { PermissionUiEffect.NavigateToLibrary }
    }

    private fun handlePermissionsDenied(event: PermissionUiEvent.PermissionsDenied) {
        if (event.shouldShowRationale) {
            sendEffect { PermissionUiEffect.ShowDeniedMessage }
        } else {
            sendEffect { PermissionUiEffect.ShowSettingsDialog }
        }
    }
}
