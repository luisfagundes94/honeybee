package com.luisfagundes.library.impl.presentation.effect

import com.luisfagundes.core.common.presentation.arch.effect.UiEffect
import com.luisfagundes.library.api.domain.model.MediaDeleteRequest

internal sealed interface TrashUiEffect : UiEffect {
    data object NavigateBack : TrashUiEffect
    data class ShowDeleteConfirmation(val request: MediaDeleteRequest) : TrashUiEffect
    data class NavigateToCongratulations(val deletedCount: Int, val deletedSize: Long) : TrashUiEffect
}
