package com.luisfagundes.library.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.library.impl.R.string.error_loading_photos_message
import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.library.impl.domain.usecase.GetItemsInTrashCountUseCase
import com.luisfagundes.library.impl.domain.usecase.GetMediaByMonthUseCase
import com.luisfagundes.library.impl.presentation.effect.LibraryUiEffect
import com.luisfagundes.library.impl.presentation.event.LibraryUiEvent
import com.luisfagundes.library.impl.presentation.state.LibraryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class LibraryViewModel @Inject constructor(
    private val getMediaByMonthUseCase: GetMediaByMonthUseCase,
    private val getItemsInTrashCountUseCase: GetItemsInTrashCountUseCase,
    private val subscriptionProvider: SubscriptionProvider,
    private val resourceProvider: ResourceProvider
) : ViewModel<LibraryUiState, LibraryUiEvent, LibraryUiEffect>(
    LibraryUiState.Loading
) {
    val isPremium: Boolean
        get() = subscriptionProvider.isPremium()

    override fun dispatchEvent(event: LibraryUiEvent) {
        when (event) {
            is LibraryUiEvent.LoadMedia -> loadMedia()
            is LibraryUiEvent.TrashClick -> navigateToTrash()
            is LibraryUiEvent.MediaClick -> navigateToMediaDetail(event.mediaId)
        }
    }

    private fun loadMedia() = viewModelScope.launch {
        setState { LibraryUiState.Loading }

        val trashCount = getItemsInTrashCountUseCase()
        getMediaByMonthUseCase().fold(
            onSuccess = { mediaSectionList ->
                setState { LibraryUiState.Content(mediaSectionList, trashCount) }
            },
            onFailure = {
                val message = resourceProvider.getString(error_loading_photos_message)
                setState { LibraryUiState.Error(message = message) }
            }
        )
    }

    private fun navigateToTrash() {
        sendEffect { LibraryUiEffect.NavigateToTrash }
    }

    private fun navigateToMediaDetail(mediaId: Long) {
        sendEffect { LibraryUiEffect.NavigateToMediaDetail(mediaId) }
    }
}