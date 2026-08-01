package com.luisfagundes.config.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.config.impl.presentation.effect.StatisticsUiEffect
import com.luisfagundes.config.impl.presentation.event.StatisticsUiEvent
import com.luisfagundes.config.impl.presentation.state.StatisticsUiState
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StatisticsViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel<StatisticsUiState, StatisticsUiEvent, StatisticsUiEffect>(
    StatisticsUiState.Loading
) {
    override fun dispatchEvent(event: StatisticsUiEvent) {
        when (event) {
            is StatisticsUiEvent.LoadStatistics -> loadStatistics()
            StatisticsUiEvent.BackClick, StatisticsUiEvent.CancelClick -> navigateBack()
        }
    }

    private fun loadStatistics() = viewModelScope.launch {
        setState { StatisticsUiState.Loading }

        repository.getStatistics().fold(
            onSuccess = { stats ->
                setState { StatisticsUiState.Content(stats) }
            },
            onFailure = {
                setState { StatisticsUiState.Error }
            }
        )
    }

    private fun navigateBack() {
        sendEffect { StatisticsUiEffect.NavigateBack }
    }
}
