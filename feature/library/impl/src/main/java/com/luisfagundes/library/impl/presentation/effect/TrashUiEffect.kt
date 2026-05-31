package com.luisfagundes.library.impl.presentation.effect

import com.luisfagundes.core.common.presentation.arch.effect.UiEffect

internal sealed interface TrashUiEffect : UiEffect {
    data object NavigateBack : TrashUiEffect
}
