package com.luisfagundes.onboarding.impl.presentation.effect

import com.luisfagundes.core.common.presentation.arch.effect.UiEffect

internal sealed interface PermissionUiEffect : UiEffect {
    data object NavigateToLibrary : PermissionUiEffect
    data object ShowDeniedMessage : PermissionUiEffect
}
