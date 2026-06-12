package com.luisfagundes.config.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.config.impl.presentation.event.StatisticsUiEvent
import com.luisfagundes.config.impl.presentation.state.StatisticsUiState
import com.luisfagundes.core.common.presentation.arch.viewmodel.StateViewModel
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.library.api.domain.usecase.GetStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.luisfagundes.config.impl.R

@HiltViewModel
internal class StatisticsViewModel @Inject constructor(
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val resourceProvider: ResourceProvider
) : StateViewModel<StatisticsUiState, StatisticsUiEvent>(
    StatisticsUiState.Loading
) {
    override fun dispatchEvent(event: StatisticsUiEvent) {
        when (event) {
            is StatisticsUiEvent.LoadStatistics -> loadStatistics()
        }
    }

    private fun loadStatistics() = viewModelScope.launch {
        setState { StatisticsUiState.Loading }
        getStatisticsUseCase().fold(
            onSuccess = { stats ->
                setState { StatisticsUiState.Content(stats) }
            },
            onFailure = {
                val errorMsg = resourceProvider.getString(R.string.failed_to_load_statistics)
                setState { StatisticsUiState.Error(errorMsg) }
            }
        )
    }
}
