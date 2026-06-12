package com.luisfagundes.config.impl.presentation.event

import com.luisfagundes.core.common.presentation.arch.event.UiEvent

internal sealed interface StatisticsUiEvent : UiEvent {
    data object LoadStatistics : StatisticsUiEvent
}
