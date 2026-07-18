package com.luisfagundes.albums.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.albums.impl.domain.usecase.GetAlbumsUseCase
import com.luisfagundes.albums.impl.presentation.effect.AlbumsUiEffect
import com.luisfagundes.albums.impl.presentation.event.AlbumsUiEvent
import com.luisfagundes.albums.impl.presentation.state.AlbumsUiState
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AlbumsViewModel @Inject constructor(
    private val getAlbumsUseCase: GetAlbumsUseCase
) : ViewModel<AlbumsUiState, AlbumsUiEvent, AlbumsUiEffect>(AlbumsUiState.Loading) {

    override fun dispatchEvent(event: AlbumsUiEvent) {
        when (event) {
            is AlbumsUiEvent.LoadAlbums -> loadAlbums()
            is AlbumsUiEvent.AlbumClick -> navigateToAlbumDetails(event.albumId, event.albumName)
        }
    }

    private fun loadAlbums() = viewModelScope.launch {
        setState { AlbumsUiState.Loading }
        getAlbumsUseCase().fold(
            onSuccess = { albums ->
                setState { AlbumsUiState.Content(albums) }
            },
            onFailure = {
                setState { AlbumsUiState.Error }
            }
        )
    }

    private fun navigateToAlbumDetails(albumId: String, albumName: String) {
        sendEffect { AlbumsUiEffect.NavigateToAlbumDetails(albumId, albumName) }
    }
}
