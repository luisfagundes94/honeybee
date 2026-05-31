package com.luisfagundes.library.impl.presentation.event

import com.luisfagundes.core.common.presentation.arch.event.UiEvent

internal sealed interface MediaDetailsUiEvent : UiEvent {
    data class LoadDetails(val initialPhotoId: Long) : MediaDetailsUiEvent
    data class SwipeUp(val photoId: Long) : MediaDetailsUiEvent
    data object TrashClick : MediaDetailsUiEvent
    data class ToggleFavorite(val photoId: Long) : MediaDetailsUiEvent
}
