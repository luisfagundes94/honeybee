package com.luisfagundes.albums.impl.presentation.event

import com.luisfagundes.core.common.presentation.arch.event.UiEvent

internal sealed interface AlbumDetailsUiEvent : UiEvent {
    data class LoadMedia(val albumId: String) : AlbumDetailsUiEvent
    data class MediaClick(val mediaId: Long) : AlbumDetailsUiEvent
    data object BackClick : AlbumDetailsUiEvent
    data object CancelClick : AlbumDetailsUiEvent
    data object Retry : AlbumDetailsUiEvent
}
