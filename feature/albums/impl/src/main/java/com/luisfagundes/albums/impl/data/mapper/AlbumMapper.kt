package com.luisfagundes.albums.impl.data.mapper

import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.domain.model.AlbumMedia
import com.luisfagundes.library.api.domain.model.Media
import javax.inject.Inject

internal class AlbumMapper @Inject constructor() {

    fun mapToAlbums(activeMedia: List<Media>): List<Album> {
        val albumList = mutableListOf<Album>()

        // 1. Group physical folders
        val physicalGroups = activeMedia.filter { it.bucketId != null }.groupBy { it.bucketId }
        physicalGroups.forEach { (bucketId, items) ->
            if (bucketId != null && items.isNotEmpty()) {
                albumList.add(
                    Album.Physical(
                        id = bucketId,
                        name = items.first().bucketDisplayName ?: "Unknown",
                        count = items.size,
                        coverUri = items.first().uri,
                        isVideo = items.first().isVideo
                    )
                )
            }
        }

        // 2. Add Virtual Albums
        // Favorites
        val favoritesItems = activeMedia.filter { it.isFavorite }
        if (favoritesItems.isNotEmpty()) {
            albumList.add(
                Album.Virtual.Favorites(
                    count = favoritesItems.size,
                    coverUri = favoritesItems.first().uri,
                    isVideo = favoritesItems.first().isVideo
                )
            )
        }

        // Videos
        val videoItems = activeMedia.filter { it.isVideo }
        if (videoItems.isNotEmpty()) {
            albumList.add(
                Album.Virtual.Videos(
                    count = videoItems.size,
                    coverUri = videoItems.first().uri,
                    isVideo = true
                )
            )
        }

        // Sort albums alphabetically by name
        return albumList.sortedBy { album ->
            val sortName = when (album) {
                is Album.Physical -> album.name
                is Album.Virtual.Favorites -> "Favorites"
                is Album.Virtual.Videos -> "Videos"
            }
            sortName.lowercase()
        }
    }

    fun mapToAlbumMedia(media: Media): AlbumMedia {
        return AlbumMedia(
            id = media.id,
            uri = media.uri,
            dateAdded = media.dateAdded,
            isVideo = media.isVideo
        )
    }
}
