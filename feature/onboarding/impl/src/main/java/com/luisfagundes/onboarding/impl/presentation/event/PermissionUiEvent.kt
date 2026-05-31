package com.luisfagundes.onboarding.impl.presentation.event

import com.luisfagundes.core.common.presentation.arch.event.UiEvent

internal sealed interface PermissionUiEvent : UiEvent {
    data object PermissionsGranted : PermissionUiEvent
    data class PermissionsDenied(val shouldShowRationale: Boolean) : PermissionUiEvent
}
