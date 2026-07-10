package com.luisfagundes.library.impl.presentation.event

import com.luisfagundes.core.common.presentation.arch.event.UiEvent

internal sealed interface MediaDetailsUiEvent : UiEvent {
    data class LoadDetails(val initialMediaId: Long, val albumId: String? = null) : MediaDetailsUiEvent
    data class SwipeUp(val mediaId: Long) : MediaDetailsUiEvent
    data object TrashClick : MediaDetailsUiEvent
    data class ToggleFavorite(val mediaId: Long) : MediaDetailsUiEvent
}
