package com.luisfagundes.library.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.library.impl.R.string.failed_to_load_photo_details
import com.luisfagundes.library.impl.domain.usecase.GetActiveMediaUseCase
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
    private val getActiveMediaUseCase: GetActiveMediaUseCase,
    private val moveToTrashUseCase: MoveToTrashUseCase,
    private val getItemsInTrashCountUseCase: GetItemsInTrashCountUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel<MediaDetailsUiState, MediaDetailsUiEvent, MediaDetailsUiEffect>(
    MediaDetailsUiState.Loading
) {
    override fun dispatchEvent(event: MediaDetailsUiEvent) {
        when (event) {
            is MediaDetailsUiEvent.LoadDetails -> loadDetails(event.initialMediaId)
            is MediaDetailsUiEvent.SwipeUp -> moveToTrash(event.mediaId)
            is MediaDetailsUiEvent.TrashClick -> navigateToTrash()
            is MediaDetailsUiEvent.ToggleFavorite -> toggleFavorite(event.mediaId)
        }
    }

    private fun navigateToTrash() {
        sendEffect { MediaDetailsUiEffect.NavigateToTrash }
    }

    private fun loadDetails(initialMediaId: Long) = viewModelScope.launch {
        setState { MediaDetailsUiState.Loading }
        getActiveMediaUseCase().fold(
            onSuccess = { mediaList ->
                val initialIndex = mediaList.indexOfFirst { it.id == initialMediaId }.coerceAtLeast(0)
                val trashCount = getItemsInTrashCountUseCase()
                setState { MediaDetailsUiState.Content(mediaList, initialIndex, trashCount) }
            },
            onFailure = {
                val errorMessage = resourceProvider.getString(failed_to_load_photo_details)
                setState { MediaDetailsUiState.Error(errorMessage) }
            }
        )
    }

    private fun moveToTrash(mediaId: Long) = viewModelScope.launch {
        moveToTrashUseCase(mediaId)
        runIfStateIs<MediaDetailsUiState.Content> { currentState ->
            val updatedMedia = currentState.mediaList.filterNot { it.id == mediaId }
            if (updatedMedia.isEmpty()) {
                sendEffect { MediaDetailsUiEffect.NavigateBack }
            } else {
                val trashCount = getItemsInTrashCountUseCase()
                setState {
                    currentState.copy(
                        mediaList = updatedMedia,
                        trashCount = trashCount
                    )
                }
            }
        }
    }

    private fun toggleFavorite(mediaId: Long) {
        runIfStateIs<MediaDetailsUiState.Content> { currentState ->
            val favorites = currentState.favoriteMediaIds.toMutableSet()
            if (favorites.contains(mediaId)) favorites.remove(mediaId) else favorites.add(mediaId)
            setState { currentState.copy(favoriteMediaIds = favorites) }
        }
    }
}
