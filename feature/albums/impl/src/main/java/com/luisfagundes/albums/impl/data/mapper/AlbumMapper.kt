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

        // 2. Add Virtual Albums
        VirtualAlbum.entries.forEach { virtualAlbum ->
            val items = activeMedia.filter(virtualAlbum.filter)
            if (items.isNotEmpty()) {
                albumList.add(
                    Album(
                        id = virtualAlbum.id,
                        name = virtualAlbum.albumName,
                        count = items.size,
                        coverUri = items.first().uri,
                        isVideo = virtualAlbum.isVideo(items.first())
                    )
                )
            }
        }

        // Sort albums alphabetically by name
        return albumList.sortedBy { it.name.lowercase() }
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

internal enum class VirtualAlbum(
    val id: String,
    val albumName: String,
    val filter: (Media) -> Boolean,
    val isVideo: (Media) -> Boolean
) {
    FAVORITES("favorites", "Favorites", { it.isFavorite }, { it.isVideo }),
    VIDEOS("videos", "Videos", { it.isVideo }, { true });

    companion object {
        fun fromId(id: String): VirtualAlbum? = entries.find { it.id == id }
    }
}
