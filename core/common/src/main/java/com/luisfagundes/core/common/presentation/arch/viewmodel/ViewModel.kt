package com.luisfagundes.core.common.presentation.arch.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.effect.UiEffect
import com.luisfagundes.core.common.presentation.arch.event.UiEvent
import com.luisfagundes.core.common.presentation.arch.state.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class ViewModel<State : UiState, Event : UiEvent, Effect : UiEffect>(
    initialState: State
) : ViewModel() {

    protected val state = MutableStateFlow(initialState)
    val uiState = state.asStateFlow()

    private val _uiEffect = Channel<Effect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    abstract fun dispatchEvent(event: Event)

    protected fun getCurrentState(): State = uiState.value

    protected fun setState(reducer: (State) -> State) {
        state.update(reducer)
    }

    protected inline fun <reified UiStateType : State> setStateOf(
        noinline reducer: (UiStateType) -> State
    ) {
        state.update { current ->
            if (current is UiStateType) reducer(current) else current
        }
    }

    protected fun sendEffect(effect: () -> Effect) = viewModelScope.launch {
        runCatching {
            _uiEffect.send(effect())
        }.onFailure { throwable ->
            Log.w("Failed to send effect: ${throwable.message}", throwable)
        }
    }
}