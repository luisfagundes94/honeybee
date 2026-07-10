package com.luisfagundes.albums.impl.domain.usecase

import com.luisfagundes.albums.impl.domain.model.AlbumMedia
import com.luisfagundes.albums.impl.domain.repository.AlbumsRepository
import javax.inject.Inject

internal class GetAlbumMediaUseCase @Inject constructor(
    private val repository: AlbumsRepository
) {
    suspend operator fun invoke(albumId: String): Result<List<AlbumMedia>> =
        repository.getAlbumMedia(albumId)
}
