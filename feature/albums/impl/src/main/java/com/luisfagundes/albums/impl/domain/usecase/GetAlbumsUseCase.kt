package com.luisfagundes.albums.impl.domain.usecase

import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.domain.repository.AlbumsRepository
import javax.inject.Inject

internal class GetAlbumsUseCase @Inject constructor(
    private val repository: AlbumsRepository
) {
    suspend operator fun invoke(): Result<List<Album>> = repository.getAlbums()
}
