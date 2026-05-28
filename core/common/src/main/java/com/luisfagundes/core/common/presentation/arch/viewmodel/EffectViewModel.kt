package com.luisfagundes.core.common.presentation.arch.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.effect.UiEffect
import com.luisfagundes.core.common.presentation.arch.event.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class EffectViewModel<Effect : UiEffect, Event : UiEvent> : ViewModel() {
    private val _uiEffect = Channel<Effect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    abstract fun dispatchEvent(event: Event)

    protected fun sendEffect(effect: () -> Effect) = viewModelScope.launch {
        runCatching {
            _uiEffect.send(effect())
        }.onFailure { throwable ->
            Log.w("Failed to send effect: ${throwable.message}", throwable)
        }
    }
}