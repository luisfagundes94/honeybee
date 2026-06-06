package com.luisfagundes.library.impl.presentation.effect

import com.luisfagundes.core.common.presentation.arch.effect.UiEffect

internal sealed interface LibraryUiEffect : UiEffect {
    data class NavigateToMediaDetail(val mediaId: Long) : LibraryUiEffect
    data object NavigateToTrash : LibraryUiEffect
}