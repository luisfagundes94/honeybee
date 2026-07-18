package com.luisfagundes.config.impl.presentation.effect

import com.luisfagundes.core.common.presentation.arch.effect.UiEffect

internal sealed interface StatisticsUiEffect : UiEffect {
    data object NavigateBack : StatisticsUiEffect
}