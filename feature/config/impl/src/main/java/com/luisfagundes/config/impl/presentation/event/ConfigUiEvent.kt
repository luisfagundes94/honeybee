package com.luisfagundes.config.impl.presentation.event

import com.luisfagundes.core.common.presentation.arch.event.UiEvent

internal sealed interface ConfigUiEvent : UiEvent {
    data class NotificationsToggled(val enabled: Boolean) : ConfigUiEvent
    data object StatisticsClick : ConfigUiEvent
    data object FeedbackClick : ConfigUiEvent
}
