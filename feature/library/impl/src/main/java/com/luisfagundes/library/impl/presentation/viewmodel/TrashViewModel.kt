package com.luisfagundes.library.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.core.common.provider.SubscriptionStatus
import com.luisfagundes.library.api.domain.model.DeletionQuota
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import com.luisfagundes.library.impl.presentation.effect.TrashUiEffect
import com.luisfagundes.library.impl.presentation.event.TrashUiEvent
import com.luisfagundes.library.impl.presentation.state.TrashUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TrashViewModel @Inject constructor(
    private val repository: LibraryRepository,
    private val subscriptionProvider: SubscriptionProvider
) : ViewModel<TrashUiState, TrashUiEvent, TrashUiEffect>(
    TrashUiState.Loading
) {
    private var pendingDeletion: List<Media> = emptyList()

    init {
        viewModelScope.launch {
            subscriptionProvider.status.collect { refreshQuota() }
        }
    }

    override fun dispatchEvent(event: TrashUiEvent) {
        when (event) {
            is TrashUiEvent.LoadTrash -> loadTrash()
            is TrashUiEvent.RestoreMedia -> restoreMedia(event.mediaId)
            is TrashUiEvent.ClearTrash -> clearTrash()
            is TrashUiEvent.ConfirmDeletion -> confirmDeletion()
            is TrashUiEvent.DeleteWithinLimit -> deleteWithinLimit()
            is TrashUiEvent.ApproveDeletion -> approveDeletion()
            is TrashUiEvent.QuotaExpired -> refreshQuota()
        }
    }

    private fun loadTrash() = viewModelScope.launch {
        setState { TrashUiState.Loading }
        repository.getTrashMedia().fold(
            onSuccess = { mediaList ->
                resolveQuota().fold(
                    onSuccess = { quota ->
                        setState {
                            TrashUiState.Content(
                                mediaToBeDeleted = mediaList,
                                deletionQuota = quota
                            )
                        }
                    },
                    onFailure = { setState { TrashUiState.Error } }
                )
            },
            onFailure = {
                setState { TrashUiState.Error }
            }
        )
    }

    private suspend fun resolveQuota(): Result<DeletionQuota> =
        if (subscriptionProvider.status.value == SubscriptionStatus.Free) {
            repository.getDeletionQuota()
        } else {
            Result.success(DeletionQuota.Unlimited)
        }

    private fun refreshQuota() = viewModelScope.launch {
        if (getCurrentState() !is TrashUiState.Content) return@launch
        resolveQuota().fold(
            onSuccess = { quota ->
                setStateOf<TrashUiState.Content> { it.copy(deletionQuota = quota) }
            },
            onFailure = { setState { TrashUiState.Error } }
        )
    }

    private fun restoreMedia(mediaId: Long) = viewModelScope.launch {
        repository.restoreFromTrash(listOf(mediaId)).fold(
            onSuccess = {
                setStateOf<TrashUiState.Content> { currentState ->
                    val updatedList = currentState.mediaToBeDeleted.filterNot { it.id == mediaId }
                    currentState.copy(mediaToBeDeleted = updatedList)
                }
            },
            onFailure = { setState { TrashUiState.Error } }
        )
    }

    private fun clearTrash() {
        runIfStateIs<TrashUiState.Content> { currentState ->
            val mediaIds = currentState.mediaToBeDeleted.map { it.id }
            if (mediaIds.isEmpty()) return@runIfStateIs

            viewModelScope.launch {
                repository.restoreFromTrash(mediaIds).fold(
                    onSuccess = {
                        val restoredIds = mediaIds.toSet()
                        setStateOf<TrashUiState.Content> { content ->
                            content.copy(
                                mediaToBeDeleted = content.mediaToBeDeleted
                                    .filterNot { it.id in restoredIds }
                            )
                        }
                    },
                    onFailure = { setState { TrashUiState.Error } }
                )
            }
        }
    }

    private fun confirmDeletion() {
        runIfStateIs<TrashUiState.Content> { currentState ->
            val mediaList = currentState.mediaToBeDeleted
            if (mediaList.isEmpty()) return@runIfStateIs

            when (val quota = currentState.deletionQuota) {
                is DeletionQuota.Unlimited -> requestDeletion(mediaList)
                is DeletionQuota.Available -> {
                    if (mediaList.size <= quota.remaining) {
                        requestDeletion(mediaList)
                    } else {
                        sendEffect { TrashUiEffect.ShowDailyLimitSheet(quota.remaining) }
                    }
                }
                is DeletionQuota.Locked -> sendEffect { TrashUiEffect.NavigateToPremium }
            }
        }
    }

    private fun deleteWithinLimit() {
        runIfStateIs<TrashUiState.Content> { currentState ->
            val mediaList = currentState.withinQuota()
            if (mediaList.isNotEmpty()) requestDeletion(mediaList)
        }
    }

    private fun requestDeletion(mediaList: List<Media>) = viewModelScope.launch {
        pendingDeletion = mediaList
        repository.createDeleteRequest(mediaList.map { it.id }).fold(
            onSuccess = { request ->
                if (request != null) {
                    sendEffect { TrashUiEffect.ShowDeleteConfirmation(request) }
                } else {
                    deleteMediaPermanently(mediaList)
                }
            },
            onFailure = {
                setState { TrashUiState.Error }
            }
        )
    }

    private fun approveDeletion() {
        runIfStateIs<TrashUiState.Content> { currentState ->
            val mediaList = pendingDeletion.ifEmpty { currentState.withinQuota() }
            if (mediaList.isNotEmpty()) deleteMediaPermanently(mediaList)
        }
    }

    private fun deleteMediaPermanently(mediaList: List<Media>) = viewModelScope.launch {
        pendingDeletion = emptyList()
        val count = mediaList.size
        val size = mediaList.sumOf { it.size }
        repository.permanentlyDelete(mediaList).fold(
            onSuccess = { sendEffect { TrashUiEffect.NavigateToCongratulations(count, size) } },
            onFailure = { setState { TrashUiState.Error } }
        )
    }

    private fun TrashUiState.Content.withinQuota(): List<Media> = when (val quota = deletionQuota) {
        is DeletionQuota.Unlimited -> mediaToBeDeleted
        is DeletionQuota.Available -> mediaToBeDeleted.sortedBy { it.dateAdded }.take(quota.remaining)
        is DeletionQuota.Locked -> emptyList()
    }
}
