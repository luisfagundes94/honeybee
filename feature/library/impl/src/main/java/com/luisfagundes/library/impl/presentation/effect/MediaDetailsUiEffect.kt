package com.luisfagundes.library.impl.presentation.effect

import com.luisfagundes.core.common.presentation.arch.effect.UiEffect

internal sealed interface MediaDetailsUiEffect : UiEffect {
    data object NavigateBack : MediaDetailsUiEffect
    data object NavigateToTrash : MediaDetailsUiEffect
}
