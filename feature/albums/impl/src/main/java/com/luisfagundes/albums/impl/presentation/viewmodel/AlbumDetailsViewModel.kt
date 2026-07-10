package com.luisfagundes.albums.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.albums.impl.R
import com.luisfagundes.albums.impl.domain.usecase.GetAlbumMediaUseCase
import com.luisfagundes.albums.impl.presentation.effect.AlbumDetailsUiEffect
import com.luisfagundes.albums.impl.presentation.event.AlbumDetailsUiEvent
import com.luisfagundes.albums.impl.presentation.state.AlbumDetailsUiState
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AlbumDetailsViewModel @Inject constructor(
    private val getAlbumMediaUseCase: GetAlbumMediaUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel<AlbumDetailsUiState, AlbumDetailsUiEvent, AlbumDetailsUiEffect>(AlbumDetailsUiState.Loading) {

    private var currentAlbumId: String? = null

    override fun dispatchEvent(event: AlbumDetailsUiEvent) {
        when (event) {
            is AlbumDetailsUiEvent.LoadMedia -> {
                currentAlbumId = event.albumId
                loadMedia(event.albumId)
            }
            is AlbumDetailsUiEvent.MediaClick -> navigateToMediaDetail(event.mediaId)
            is AlbumDetailsUiEvent.BackClick -> navigateBack()
            is AlbumDetailsUiEvent.Retry -> currentAlbumId?.let { loadMedia(it) }
        }
    }

    private fun loadMedia(albumId: String) = viewModelScope.launch {
        setState { AlbumDetailsUiState.Loading }
        getAlbumMediaUseCase(albumId).fold(
            onSuccess = { mediaList ->
                setState { AlbumDetailsUiState.Content(mediaList) }
            },
            onFailure = {
                val message = resourceProvider.getString(R.string.error_loading_album_media)
                setState { AlbumDetailsUiState.Error(message) }
            }
        )
    }

    private fun navigateToMediaDetail(mediaId: Long) {
        sendEffect { AlbumDetailsUiEffect.NavigateToMediaDetail(mediaId) }
    }

    private fun navigateBack() {
        sendEffect { AlbumDetailsUiEffect.NavigateBack }
    }
}
