package com.luisfagundes.albums.impl.presentation.event

import com.luisfagundes.core.common.presentation.arch.event.UiEvent

internal sealed interface AlbumsUiEvent : UiEvent {
    data object LoadAlbums : AlbumsUiEvent
    data class AlbumClick(val albumId: String, val albumName: String) : AlbumsUiEvent
}
