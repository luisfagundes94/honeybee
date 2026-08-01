package com.luisfagundes.albums.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.albums.impl.domain.usecase.GetAlbumsUseCase
import com.luisfagundes.albums.impl.presentation.effect.AlbumsUiEffect
import com.luisfagundes.albums.impl.presentation.event.AlbumsUiEvent
import com.luisfagundes.albums.impl.presentation.state.AlbumsUiState
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.core.common.provider.SubscriptionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import javax.inject.Inject

@HiltViewModel
internal class AlbumsViewModel @Inject constructor(
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val subscriptionProvider: SubscriptionProvider,
) : ViewModel<AlbumsUiState, AlbumsUiEvent, AlbumsUiEffect>(
    initialState = AlbumsUiState.Loading
) {
    init {
        observeSubscriptionStatus()
    }

    override fun dispatchEvent(event: AlbumsUiEvent) {
        when (event) {
            is AlbumsUiEvent.LoadAlbums -> loadAlbums()
            is AlbumsUiEvent.AlbumClick -> navigateToAlbumDetails(
                albumId = event.albumId,
                albumName = event.albumName
            )
        }
    }

    private fun observeSubscriptionStatus() {
        viewModelScope.launch {
            subscriptionProvider.status
                .filter { it != SubscriptionStatus.Loading }
                .distinctUntilChanged()
                .collect { loadAlbums() }
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
