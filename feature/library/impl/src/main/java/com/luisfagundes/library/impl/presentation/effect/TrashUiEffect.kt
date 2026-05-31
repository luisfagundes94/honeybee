package com.luisfagundes.library.impl.presentation.effect

import android.content.IntentSender
import com.luisfagundes.core.common.presentation.arch.effect.UiEffect

internal sealed interface TrashUiEffect : UiEffect {
    data object NavigateBack : TrashUiEffect
    data class ShowDeleteConfirmation(val intentSender: IntentSender) : TrashUiEffect
}
