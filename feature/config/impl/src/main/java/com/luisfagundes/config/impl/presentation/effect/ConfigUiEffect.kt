package com.luisfagundes.config.impl.presentation.effect

import com.luisfagundes.core.common.presentation.arch.effect.UiEffect

internal sealed interface ConfigUiEffect : UiEffect {
    data object NavigateToStatistics : ConfigUiEffect
    data object NavigateToFeedback : ConfigUiEffect
    data object NavigateToPremium : ConfigUiEffect
    data object ShowPrivacyOptions : ConfigUiEffect
}
