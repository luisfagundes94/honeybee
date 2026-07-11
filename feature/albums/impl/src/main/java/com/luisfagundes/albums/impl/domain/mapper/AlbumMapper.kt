package com.luisfagundes.albums.impl.domain.mapper

import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.domain.model.AlbumMedia
import com.luisfagundes.library.api.domain.model.Media
import javax.inject.Inject

internal class AlbumMapper @Inject constructor() {

    fun mapToAlbums(activeMedia: List<Media>): List<Album> {
        return buildList {
            addAll(getPhysicalAlbums(activeMedia))
            getFavoritesAlbum(activeMedia)?.let { add(it) }
            getVideosAlbum(activeMedia)?.let { add(it) }
        }.sortedBy { getAlbumSortName(it).lowercase() }
    }

    fun mapToAlbumMedia(media: Media): AlbumMedia {
        return AlbumMedia(
            id = media.id,
            uri = media.uri,
            dateAdded = media.dateAdded,
            isVideo = media.isVideo
        )
    }

    private fun getPhysicalAlbums(activeMedia: List<Media>): List<Album.Physical> {
        return activeMedia
            .groupBy { it.bucketId }
            .mapNotNull { (bucketId, items) ->
                if (bucketId == null) return@mapNotNull null
                val first = items.first()
                Album.Physical(
                    id = bucketId,
                    name = first.bucketDisplayName ?: "Unknown",
                    count = items.size,
                    coverUri = first.uri,
                    isVideo = first.isVideo
                )
            }
    }

    private fun getFavoritesAlbum(activeMedia: List<Media>): Album.Virtual.Favorites? {
        val favoritesItems = activeMedia.filter { it.isFavorite }
        if (favoritesItems.isEmpty()) return null

        val first = favoritesItems.first()
        return Album.Virtual.Favorites(
            count = favoritesItems.size,
            coverUri = first.uri,
            isVideo = first.isVideo
        )
    }

    private fun getVideosAlbum(activeMedia: List<Media>): Album.Virtual.Videos? {
        val videoItems = activeMedia.filter { it.isVideo }
        if (videoItems.isEmpty()) return null

        return Album.Virtual.Videos(
            count = videoItems.size,
            coverUri = videoItems.first().uri,
            isVideo = true
        )
    }

    private fun getAlbumSortName(album: Album): String {
        return when (album) {
            is Album.Physical -> album.name
            is Album.Virtual.Favorites -> "Favorites"
            is Album.Virtual.Videos -> "Videos"
        }
    }
}
