package com.luisfagundes.library.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.library.impl.R.string.error_loading_photos_message
import com.luisfagundes.library.impl.domain.usecase.GetPhotosByMonthUseCase
import com.luisfagundes.library.impl.presentation.effect.LibraryUiEffect
import com.luisfagundes.library.impl.presentation.event.LibraryUiEvent
import com.luisfagundes.library.impl.presentation.state.LibraryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class LibraryViewModel @Inject constructor(
    private val getPhotosByMonthUseCase: GetPhotosByMonthUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel<LibraryUiState, LibraryUiEvent, LibraryUiEffect>(
    LibraryUiState.Loading
) {
    override fun dispatchEvent(event: LibraryUiEvent) {
        when (event) {
            is LibraryUiEvent.LoadPhotos -> loadPhotos()
            is LibraryUiEvent.TrashClick -> Unit
            is LibraryUiEvent.PhotoClick -> navigateToPhotoDetail(event.photoId)
        }
    }

    private fun loadPhotos() = viewModelScope.launch {
        setState { LibraryUiState.Loading }

        getPhotosByMonthUseCase().fold(
            onSuccess = { photoSectionList ->
                setState { LibraryUiState.Content(photoSectionList) }
            },
            onFailure = {
                val message = resourceProvider.getString(error_loading_photos_message)
                setState { LibraryUiState.Error(message = message) }
            }
        )
    }

    private fun navigateToPhotoDetail(photoId: Long) {
        sendEffect { LibraryUiEffect.NavigateToPhotoDetail(photoId) }
    }
}