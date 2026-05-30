package com.luisfagundes.impl.presentation.effect

import com.luisfagundes.core.common.presentation.arch.effect.UiEffect

internal sealed interface LibraryUiEffect : UiEffect {
    data class NavigateToPhotoDetail(val photoId: Long) : LibraryUiEffect
}