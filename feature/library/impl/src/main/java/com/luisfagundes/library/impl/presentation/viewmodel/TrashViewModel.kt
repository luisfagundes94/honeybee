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
import com.luisfagundes.library.impl.domain.model.Photo
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
            is TrashUiEvent.ApproveDeletion -> approveDeletion()
        }
    }

    private fun loadTrash() = viewModelScope.launch {
        setState { TrashUiState.Loading }
        getTrashPhotosUseCase().fold(
            onSuccess = { photos ->
                setState { TrashUiState.Content(photosToBeDeleted = photos) }
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
            val updatedList = currentState.photosToBeDeleted.filterNot { it.id == photoId }
            currentState.copy(photosToBeDeleted = updatedList)
        }
    }

    private fun confirmDeletion() {
        runIfStateIs<TrashUiState.Content> { currentState ->
            val photos = currentState.photosToBeDeleted
            if (photos.isEmpty()) return@runIfStateIs

            val deleteIds = photos.map { it.id }
            val pendingIntent = createDeleteRequestUseCase(deleteIds)
            if (pendingIntent != null) {
                sendEffect { TrashUiEffect.ShowDeleteConfirmation(pendingIntent.intentSender) }
            } else {
                deletePhotosPermanently(photos)
            }
        }
    }

    private fun approveDeletion() {
        runIfStateIs<TrashUiState.Content> { currentState ->
            val photos = currentState.photosToBeDeleted
            if (photos.isNotEmpty()) deletePhotosPermanently(photos)
        }
    }

    private fun deletePhotosPermanently(photos: List<Photo>) = viewModelScope.launch {
        val deleteIds = photos.map { it.id }
        val count = photos.size
        val size = photos.sumOf { it.size }
        permanentlyDeleteUseCase(deleteIds)
        sendEffect { TrashUiEffect.NavigateToCongratulations(count, size) }
    }
}
