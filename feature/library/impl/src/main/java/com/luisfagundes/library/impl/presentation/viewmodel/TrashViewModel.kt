package com.luisfagundes.library.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
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
    private val permanentlyDeleteUseCase: PermanentlyDeleteUseCase
) : ViewModel<TrashUiState, TrashUiEvent, TrashUiEffect>(
    TrashUiState.Loading
) {
    override fun dispatchEvent(event: TrashUiEvent) {
        when (event) {
            is TrashUiEvent.LoadTrash -> loadTrash()
            is TrashUiEvent.TogglePhotoSelection -> togglePhotoSelection(event.photoId)
            is TrashUiEvent.ConfirmDeletion -> confirmDeletion()
        }
    }

    private fun loadTrash() = viewModelScope.launch {
        setState { TrashUiState.Loading }
        getTrashPhotosUseCase().fold(
            onSuccess = { photos ->
                setState { TrashUiState.Content(deletePhotos = photos, keepPhotos = emptyList()) }
            },
            onFailure = {
                setState { TrashUiState.Error("Failed to load trash photos") }
            }
        )
    }

    private fun togglePhotoSelection(photoId: Long) {
        val currentState = uiState.value
        if (currentState is TrashUiState.Content) {
            val deleteList = currentState.deletePhotos.toMutableList()
            val keepList = currentState.keepPhotos.toMutableList()

            val photoInDelete = deleteList.find { it.id == photoId }
            if (photoInDelete != null) {
                deleteList.remove(photoInDelete)
                keepList.add(photoInDelete)
            } else {
                val photoInKeep = keepList.find { it.id == photoId }
                if (photoInKeep != null) {
                    keepList.remove(photoInKeep)
                    deleteList.add(photoInKeep)
                }
            }

            setState {
                currentState.copy(
                    deletePhotos = deleteList,
                    keepPhotos = keepList
                )
            }
        }
    }

    private fun confirmDeletion() = viewModelScope.launch {
        val currentState = uiState.value
        if (currentState is TrashUiState.Content) {
            val deleteIds = currentState.deletePhotos.map { it.id }
            val keepIds = currentState.keepPhotos.map { it.id }

            if (deleteIds.isNotEmpty()) {
                permanentlyDeleteUseCase(deleteIds)
            }
            if (keepIds.isNotEmpty()) {
                restoreFromTrashUseCase(keepIds)
            }

            sendEffect { TrashUiEffect.NavigateBack }
        }
    }
}
