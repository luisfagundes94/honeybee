package com.luisfagundes.library.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.library.impl.R.string.failed_to_load_photo_details
import com.luisfagundes.library.impl.domain.usecase.GetActivePhotosUseCase
import com.luisfagundes.library.impl.domain.usecase.GetItemsInTrashCountUseCase
import com.luisfagundes.library.impl.domain.usecase.MoveToTrashUseCase
import com.luisfagundes.library.impl.presentation.effect.MediaDetailsUiEffect
import com.luisfagundes.library.impl.presentation.event.MediaDetailsUiEvent
import com.luisfagundes.library.impl.presentation.state.MediaDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MediaDetailsViewModel @Inject constructor(
    private val getActivePhotosUseCase: GetActivePhotosUseCase,
    private val moveToTrashUseCase: MoveToTrashUseCase,
    private val getItemsInTrashCountUseCase: GetItemsInTrashCountUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel<MediaDetailsUiState, MediaDetailsUiEvent, MediaDetailsUiEffect>(
    MediaDetailsUiState.Loading
) {
    override fun dispatchEvent(event: MediaDetailsUiEvent) {
        when (event) {
            is MediaDetailsUiEvent.LoadDetails -> loadDetails(event.initialPhotoId)
            is MediaDetailsUiEvent.SwipeUp -> moveToTrash(event.photoId)
            is MediaDetailsUiEvent.TrashClick -> navigateToTrash()
            is MediaDetailsUiEvent.ToggleFavorite -> toggleFavorite(event.photoId)
        }
    }

    private fun navigateToTrash() {
        sendEffect { MediaDetailsUiEffect.NavigateToTrash }
    }

    private fun loadDetails(initialPhotoId: Long) = viewModelScope.launch {
        setState { MediaDetailsUiState.Loading }
        getActivePhotosUseCase().fold(
            onSuccess = { photos ->
                val initialIndex = photos.indexOfFirst { it.id == initialPhotoId }.coerceAtLeast(0)
                val trashCount = getItemsInTrashCountUseCase()
                setState { MediaDetailsUiState.Content(photos, initialIndex, trashCount) }
            },
            onFailure = {
                val errorMessage = resourceProvider.getString(failed_to_load_photo_details)
                setState { MediaDetailsUiState.Error(errorMessage) }
            }
        )
    }

    private fun moveToTrash(photoId: Long) = viewModelScope.launch {
        moveToTrashUseCase(photoId)
        val currentState = uiState.value
        if (currentState is MediaDetailsUiState.Content) {
            val updatedPhotos = currentState.photos.filterNot { it.id == photoId }
            if (updatedPhotos.isEmpty()) {
                sendEffect { MediaDetailsUiEffect.NavigateBack }
            } else {
                val trashCount = getItemsInTrashCountUseCase()
                setState {
                    currentState.copy(
                        photos = updatedPhotos,
                        trashCount = trashCount
                    )
                }
            }
        }
    }

    private fun toggleFavorite(photoId: Long) {
        val currentState = uiState.value
        if (currentState is MediaDetailsUiState.Content) {
            val favorites = currentState.favoritePhotoIds.toMutableSet()
            if (favorites.contains(photoId)) {
                favorites.remove(photoId)
            } else {
                favorites.add(photoId)
            }
            setState { currentState.copy(favoritePhotoIds = favorites) }
        }
    }
}
