package com.luisfagundes.library.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.library.impl.R.string.failed_to_load_trash_photos
import com.luisfagundes.library.impl.domain.usecase.CreateDeleteRequestUseCase
import com.luisfagundes.library.impl.domain.usecase.GetTrashMediaUseCase
import com.luisfagundes.library.impl.domain.usecase.PermanentlyDeleteUseCase
import com.luisfagundes.library.impl.domain.usecase.RestoreFromTrashUseCase
import com.luisfagundes.library.impl.presentation.effect.TrashUiEffect
import com.luisfagundes.library.impl.presentation.event.TrashUiEvent
import com.luisfagundes.library.impl.presentation.state.TrashUiState
import com.luisfagundes.library.api.domain.model.Media
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TrashViewModel @Inject constructor(
    private val getTrashMediaUseCase: GetTrashMediaUseCase,
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
            is TrashUiEvent.RestoreMedia -> restoreMedia(event.mediaId)
            is TrashUiEvent.ConfirmDeletion -> confirmDeletion()
            is TrashUiEvent.ApproveDeletion -> approveDeletion()
        }
    }

    private fun loadTrash() = viewModelScope.launch {
        setState { TrashUiState.Loading }
        getTrashMediaUseCase().fold(
            onSuccess = { mediaList ->
                setState { TrashUiState.Content(mediaToBeDeleted = mediaList) }
            },
            onFailure = {
                val errorMessage = resourceProvider.getString(failed_to_load_trash_photos)
                setState { TrashUiState.Error(errorMessage) }
            }
        )
    }

    private fun restoreMedia(mediaId: Long) = viewModelScope.launch {
        restoreFromTrashUseCase(listOf(mediaId))
        setStateOf<TrashUiState.Content> { currentState ->
            val updatedList = currentState.mediaToBeDeleted.filterNot { it.id == mediaId }
            currentState.copy(mediaToBeDeleted = updatedList)
        }
    }

    private fun confirmDeletion() {
        runIfStateIs<TrashUiState.Content> { currentState ->
            val mediaList = currentState.mediaToBeDeleted
            if (mediaList.isEmpty()) return@runIfStateIs

            viewModelScope.launch {
                val deleteIds = mediaList.map { it.id }
                val pendingIntent = createDeleteRequestUseCase(deleteIds)
                if (pendingIntent != null) {
                    sendEffect { TrashUiEffect.ShowDeleteConfirmation(pendingIntent.intentSender) }
                } else {
                    deleteMediaPermanently(mediaList)
                }
            }
        }
    }

    private fun approveDeletion() {
        runIfStateIs<TrashUiState.Content> { currentState ->
            val mediaList = currentState.mediaToBeDeleted
            if (mediaList.isNotEmpty()) deleteMediaPermanently(mediaList)
        }
    }

    private fun deleteMediaPermanently(mediaList: List<Media>) = viewModelScope.launch {
        val count = mediaList.size
        val size = mediaList.sumOf { it.size }
        permanentlyDeleteUseCase(mediaList)
        sendEffect { TrashUiEffect.NavigateToCongratulations(count, size) }
    }
}
