package com.luisfagundes.library.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import com.luisfagundes.library.impl.presentation.effect.MediaDetailsUiEffect
import com.luisfagundes.library.impl.presentation.event.MediaDetailsUiEvent
import com.luisfagundes.library.impl.presentation.state.MediaDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MediaDetailsViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel<MediaDetailsUiState, MediaDetailsUiEvent, MediaDetailsUiEffect>(
    MediaDetailsUiState.Loading
) {
    override fun dispatchEvent(event: MediaDetailsUiEvent) {
        when (event) {
            is MediaDetailsUiEvent.LoadDetails -> loadDetails(event.initialMediaId, event.albumId)
            is MediaDetailsUiEvent.SwipeUp -> moveToTrash(event.mediaId)
            is MediaDetailsUiEvent.ToggleFavorite -> toggleFavorite(event.mediaId)
            MediaDetailsUiEvent.TrashClick -> navigateToTrash()
            MediaDetailsUiEvent.BackClick, MediaDetailsUiEvent.CancelClick -> navigateBack()
        }
    }

    private fun loadDetails(initialMediaId: Long, albumId: String?) = viewModelScope.launch {
        setState { MediaDetailsUiState.Loading }
        repository.getActiveMedia().fold(
            onSuccess = { mediaList ->
                val filteredList = when (albumId) {
                    null -> mediaList
                    "favorites" -> mediaList.filter { it.isFavorite }
                    "videos" -> mediaList.filter { it.isVideo }
                    else -> mediaList.filter { it.bucketId == albumId }
                }
                val initialIndex = filteredList.indexOfFirst { it.id == initialMediaId }.coerceAtLeast(0)
                val trashCount = repository.getItemsInTrashCount()
                setState { MediaDetailsUiState.Content(filteredList, initialIndex, trashCount) }
            },
            onFailure = {
                setState { MediaDetailsUiState.Error }
            }
        )
    }

    private fun moveToTrash(mediaId: Long) = viewModelScope.launch {
        repository.moveToTrash(mediaId)
        runIfStateIs<MediaDetailsUiState.Content> { currentState ->
            val updatedMedia = currentState.mediaList.filterNot { it.id == mediaId }
            if (updatedMedia.isEmpty()) {
                sendEffect { MediaDetailsUiEffect.NavigateBack }
            } else {
                val trashCount = repository.getItemsInTrashCount()
                setState {
                    currentState.copy(
                        mediaList = updatedMedia,
                        trashCount = trashCount
                    )
                }
            }
        }
    }

    private fun navigateToTrash() {
        sendEffect { MediaDetailsUiEffect.NavigateToTrash }
    }

    private fun navigateBack() {
        sendEffect { MediaDetailsUiEffect.NavigateBack }
    }

    private fun toggleFavorite(mediaId: Long) {
        runIfStateIs<MediaDetailsUiState.Content> { currentState ->
            val favorites = currentState.favoriteMediaIds.toMutableSet()
            if (favorites.contains(mediaId)) favorites.remove(mediaId) else favorites.add(mediaId)
            setState { currentState.copy(favoriteMediaIds = favorites) }
        }
    }
}
