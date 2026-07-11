package com.luisfagundes.albums.impl.domain.usecase

import com.luisfagundes.albums.impl.domain.mapper.AlbumMapper
import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import javax.inject.Inject

internal class GetAlbumsUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val albumMapper: AlbumMapper
) {
    suspend operator fun invoke(): Result<List<Album>> =
        libraryRepository.getActiveMedia().map { activeMedia ->
            albumMapper.mapToAlbums(activeMedia)
        }
}
