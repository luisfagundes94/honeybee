package com.luisfagundes.albums.impl.domain.usecase

import com.luisfagundes.albums.impl.domain.mapper.AlbumMapper
import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.domain.model.AlbumMedia
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import javax.inject.Inject

internal class GetAlbumMediaUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val albumMapper: AlbumMapper
) {
    suspend operator fun invoke(albumId: String): Result<List<AlbumMedia>> =
        libraryRepository.getActiveMedia().map { activeMedia ->
            val filteredMedia = when (albumId) {
                Album.Virtual.Favorites.ID -> activeMedia.filter { it.isFavorite }
                Album.Virtual.Videos.ID -> activeMedia.filter { it.isVideo }
                else -> activeMedia.filter { it.bucketId == albumId }
            }

            filteredMedia.map { albumMapper.mapToAlbumMedia(it) }
        }
}
