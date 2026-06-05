package com.luisfagundes.library.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.library.impl.R.string.failed_to_load_trash_photos
import com.luisfagundes.library.impl.domain.usecase.CreateDeleteRequestUseCase
import com.luisfagundes.library.impl.domain.usecase.GetTrashPhotosUseCase
import com.luisfagundes.library.impl.domain.usecase.PermanentlyDeleteUseCase
import com.luisfagundes.library.impl.domain.usecase.RestoreFromTrashUseCase
import com.luisfagundes.library.impl.presentation.effect.TrashUiEffect
import com.luisfagundes.library.impl.presentation.event.TrashUiEvent
import com.luisfagundes.library.impl.presentation.state.TrashUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TrashViewModel @Inject constructor(
    private val getTrashPhotosUseCase: GetTrashPhotosUseCase,
    private val restoreFromTrashUseCase: RestoreFromTrashUseCase,
    private val permanentlyDeleteUseCase: PermanentlyDeleteUseCase,
    private val createDeleteRequestUseCase: CreateDeleteRequestUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel<TrashUiState, TrashUiEvent, TrashUiEffect>(
    TrashUiState.Loading
) {
    override fun dispatchEvent(event: TrashUiEvent) {
        when (event) {
            is TrashUiEvent.LoadTrash -> loadTrash()
            is TrashUiEvent.RestorePhoto -> restorePhoto(event.photoId)
            is TrashUiEvent.ConfirmDeletion -> confirmDeletion()
            is TrashUiEvent.OnDeleteApproved -> onDeleteApproved()
        }
    }

    private fun loadTrash() = viewModelScope.launch {
        setState { TrashUiState.Loading }
        getTrashPhotosUseCase().fold(
            onSuccess = { photos ->
                setState { TrashUiState.Content(deletePhotos = photos) }
            },
            onFailure = {
                val errorMessage = resourceProvider.getString(failed_to_load_trash_photos)
                setState { TrashUiState.Error(errorMessage) }
            }
        )
    }

    private fun restorePhoto(photoId: Long) = viewModelScope.launch {
        restoreFromTrashUseCase(listOf(photoId))
        setStateOf<TrashUiState.Content> { currentState ->
            val updatedList = currentState.deletePhotos.filterNot { it.id == photoId }
            currentState.copy(deletePhotos = updatedList)
        }
    }

    private fun confirmDeletion() = runIfStateIs<TrashUiState.Content> { currentState ->
        val deleteIds = currentState.deletePhotos.map { it.id }
        if (deleteIds.isNotEmpty()) {
            val pendingIntent = createDeleteRequestUseCase(deleteIds)
            if (pendingIntent != null) {
                sendEffect { TrashUiEffect.ShowDeleteConfirmation(pendingIntent.intentSender) }
            } else {
                viewModelScope.launch {
                    val count = currentState.deletePhotos.size
                    val size = currentState.deletePhotos.sumOf { it.size }
                    permanentlyDeleteUseCase(deleteIds)
                    sendEffect { TrashUiEffect.NavigateToCongratulations(count, size) }
                }
            }
        }
    }

    private fun onDeleteApproved() = runIfStateIs<TrashUiState.Content> { currentState ->
        val deleteIds = currentState.deletePhotos.map { it.id }
        if (deleteIds.isNotEmpty()) {
            viewModelScope.launch {
                val count = currentState.deletePhotos.size
                val size = currentState.deletePhotos.sumOf { it.size }
                permanentlyDeleteUseCase(deleteIds)
                sendEffect { TrashUiEffect.NavigateToCongratulations(count, size) }
            }
        }
    }
}
