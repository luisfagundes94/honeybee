package com.luisfagundes.library.impl.presentation.event

import com.luisfagundes.core.common.presentation.arch.event.UiEvent

internal sealed interface LibraryUiEvent : UiEvent {
    data object LoadMedia : LibraryUiEvent
    data object TrashClick : LibraryUiEvent
    data class MediaClick(val mediaId: Long) : LibraryUiEvent
}