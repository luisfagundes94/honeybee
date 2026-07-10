package com.luisfagundes.albums.impl.data.repository

import android.content.Context
import com.luisfagundes.albums.impl.data.datasource.LocalAlbumsDataSource
import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.domain.model.AlbumMedia
import com.luisfagundes.albums.impl.domain.repository.AlbumsRepository
import com.luisfagundes.core.common.di.DefaultDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AlbumsRepositoryImpl @Inject constructor(
    private val dataSource: LocalAlbumsDataSource,
    @param:ApplicationContext private val context: Context,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : AlbumsRepository {

    private val prefs = context.getSharedPreferences("library_prefs", Context.MODE_PRIVATE)

    private fun getTrashedPhotoIds(): Set<Long> {
        return prefs.getStringSet("trashed_photo_ids", emptySet())
            ?.mapNotNull { it.toLongOrNull() }
            ?.toSet() ?: emptySet()
    }

    private fun getDeletedPhotoIds(): Set<Long> {
        return prefs.getStringSet("deleted_photo_ids", emptySet())
            ?.mapNotNull { it.toLongOrNull() }
            ?.toSet() ?: emptySet()
    }

    override suspend fun getAlbums(): Result<List<Album>> = withContext(dispatcher) {
        dataSource.fetchMediaList().map { mediaList ->
            val trashedIds = getTrashedPhotoIds()
            val deletedIds = getDeletedPhotoIds()
            
            // Filter active media files
            val activeMedia = mediaList.filter { it.id !in trashedIds && it.id !in deletedIds }

            val albumList = mutableListOf<Album>()

            // 1. Group physical folders
            val physicalGroups = activeMedia.filter { it.bucketId != null }.groupBy { it.bucketId }
            physicalGroups.forEach { (bucketId, items) ->
                if (bucketId != null && items.isNotEmpty()) {
                    albumList.add(
                        Album(
                            id = bucketId,
                            name = items.first().bucketDisplayName ?: "Unknown",
                            count = items.size,
                            coverUri = items.first().uri,
                            isVideo = items.first().isVideo
                        )
                    )
                }
            }

            // 2. Add "Favorites" virtual album if it has items
            val favoritesList = activeMedia.filter { it.isFavorite }
            if (favoritesList.isNotEmpty()) {
                albumList.add(
                    Album(
                        id = "favorites",
                        name = "Favorites",
                        count = favoritesList.size,
                        coverUri = favoritesList.first().uri,
                        isVideo = favoritesList.first().isVideo
                    )
                )
            }

            // 3. Add "Videos" virtual album if it has items
            val videosList = activeMedia.filter { it.isVideo }
            if (videosList.isNotEmpty()) {
                albumList.add(
                    Album(
                        id = "videos",
                        name = "Videos",
                        count = videosList.size,
                        coverUri = videosList.first().uri,
                        isVideo = true
                    )
                )
            }

            // Sort albums alphabetically by name
            albumList.sortedBy { it.name.lowercase() }
        }
    }

    override suspend fun getAlbumMedia(albumId: String): Result<List<AlbumMedia>> = withContext(dispatcher) {
        dataSource.fetchMediaList().map { mediaList ->
            val trashedIds = getTrashedPhotoIds()
            val deletedIds = getDeletedPhotoIds()
            
            // Filter active media files
            val activeMedia = mediaList.filter { it.id !in trashedIds && it.id !in deletedIds }

            val filteredMedia = when (albumId) {
                "favorites" -> activeMedia.filter { it.isFavorite }
                "videos" -> activeMedia.filter { it.isVideo }
                else -> activeMedia.filter { it.bucketId == albumId }
            }

            filteredMedia.map {
                AlbumMedia(
                    id = it.id,
                    uri = it.uri,
                    dateAdded = it.dateAdded,
                    isVideo = it.isVideo
                )
            }
        }
    }
}
