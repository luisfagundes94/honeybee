package com.luisfagundes.library.impl.presentation.event

import com.luisfagundes.core.common.presentation.arch.event.UiEvent

internal sealed interface TrashUiEvent : UiEvent {
    data object LoadTrash : TrashUiEvent
    data class RestorePhoto(val photoId: Long) : TrashUiEvent
    data object ConfirmDeletion : TrashUiEvent
}
