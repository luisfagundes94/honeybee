package com.luisfagundes.albums.impl.presentation.effect

import com.luisfagundes.core.common.presentation.arch.effect.UiEffect

internal sealed interface AlbumDetailsUiEffect : UiEffect {
    data object NavigateBack : AlbumDetailsUiEffect
    data class NavigateToMediaDetail(val mediaId: Long) : AlbumDetailsUiEffect
}
