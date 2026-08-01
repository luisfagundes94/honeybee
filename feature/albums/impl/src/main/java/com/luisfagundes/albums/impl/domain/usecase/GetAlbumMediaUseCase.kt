package com.luisfagundes.albums.impl.domain.usecase

import com.luisfagundes.albums.impl.domain.model.FavoritesAlbumId
import com.luisfagundes.albums.impl.domain.model.VideosAlbumId
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import javax.inject.Inject

internal class GetAlbumMediaUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(albumId: String) =
        libraryRepository.getActiveMedia().map { activeMedia ->
            val filteredMedia = when (albumId) {
                FavoritesAlbumId -> activeMedia.filter { it.isFavorite }
                VideosAlbumId -> activeMedia.filter { it.isVideo }
                else -> activeMedia.filter { it.bucketId == albumId }
            }

            filteredMedia.map { it.toAlbumMedia() }
        }
}
