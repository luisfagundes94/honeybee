package com.luisfagundes.impl.presentation.event

import com.luisfagundes.core.common.presentation.arch.event.UiEvent

internal sealed interface LibraryUiEvent : UiEvent {
    data object LoadPhotos : LibraryUiEvent
    data class PhotoClick(val photoId: Long) : LibraryUiEvent
}