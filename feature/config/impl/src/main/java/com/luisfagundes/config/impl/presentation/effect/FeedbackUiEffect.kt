package com.luisfagundes.config.impl.presentation.effect

import com.luisfagundes.core.common.presentation.arch.effect.UiEffect

internal sealed interface FeedbackUiEffect : UiEffect {
    data object NavigateBack : FeedbackUiEffect
    data class ShowToast(val message: String) : FeedbackUiEffect
}
