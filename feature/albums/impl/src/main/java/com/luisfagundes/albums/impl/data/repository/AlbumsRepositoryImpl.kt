package com.luisfagundes.albums.impl.data.repository

import com.luisfagundes.albums.impl.data.mapper.AlbumMapper
import com.luisfagundes.albums.impl.data.mapper.VirtualAlbum
import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.domain.model.AlbumMedia
import com.luisfagundes.albums.impl.domain.repository.AlbumsRepository
import com.luisfagundes.core.common.di.DefaultDispatcher
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AlbumsRepositoryImpl @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val albumMapper: AlbumMapper,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : AlbumsRepository {

    override suspend fun getAlbums(): Result<List<Album>> = withContext(dispatcher) {
        libraryRepository.getActiveMedia().map { activeMedia ->
            albumMapper.mapToAlbums(activeMedia)
        }
    }

    override suspend fun getAlbumMedia(albumId: String): Result<List<AlbumMedia>> = withContext(dispatcher) {
        libraryRepository.getActiveMedia().map { activeMedia ->
            val virtualAlbum = VirtualAlbum.fromId(albumId)
            val filteredMedia = if (virtualAlbum != null) {
                activeMedia.filter(virtualAlbum.filter)
            } else {
                activeMedia.filter { it.bucketId == albumId }
            }

            filteredMedia.map { albumMapper.mapToAlbumMedia(it) }
        }
    }
}

