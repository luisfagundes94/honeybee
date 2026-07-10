package com.luisfagundes.albums.impl.presentation.effect

import com.luisfagundes.core.common.presentation.arch.effect.UiEffect

internal sealed interface AlbumsUiEffect : UiEffect {
    data class NavigateToAlbumDetails(val albumId: String, val albumName: String) : AlbumsUiEffect
}
