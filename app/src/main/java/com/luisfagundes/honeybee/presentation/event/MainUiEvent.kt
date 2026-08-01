package com.luisfagundes.honeybee.presentation.event

import com.luisfagundes.core.common.presentation.arch.event.UiEvent

internal sealed interface MainUiEvent : UiEvent {
    data object RefreshSubscription : MainUiEvent
}
