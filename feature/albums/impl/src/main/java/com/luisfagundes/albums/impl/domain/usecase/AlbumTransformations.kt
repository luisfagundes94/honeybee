package com.luisfagundes.albums.impl.domain.usecase

import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.domain.model.AlbumMedia
import com.luisfagundes.library.api.domain.model.Media

private const val UNKNOWN_ALBUM_NAME = "Unknown"
private const val FAVORITES_ALBUM_NAME = "Favorites"
private const val VIDEOS_ALBUM_NAME = "Videos"

internal fun List<Media>.toAlbums(): List<Album> {
    val physicalAlbums = toPhysicalAlbums()
    val favoritesAlbum = toFavoritesAlbum()
    val videosAlbum = toVideosAlbum()

    return buildList {
        addAll(physicalAlbums)
        favoritesAlbum?.let(::add)
        videosAlbum?.let(::add)
    }.sortedBy { it.sortName().lowercase() }
}

internal fun Media.toAlbumMedia() = AlbumMedia(
    id = id,
    uri = uri,
    dateAdded = dateAdded,
    durationMillis = durationMillis,
    isVideo = isVideo
)

private fun List<Media>.toPhysicalAlbums(): List<Album.Physical> = groupBy { it.bucketId }
    .mapNotNull { (bucketId, items) ->
        if (bucketId == null) return@mapNotNull null
        val first = items.first()
        Album.Physical(
            id = bucketId,
            name = first.bucketDisplayName ?: UNKNOWN_ALBUM_NAME,
            count = items.size,
            coverUri = first.uri,
            isVideo = first.isVideo
        )
    }

private fun List<Media>.toFavoritesAlbum(): Album.Virtual.Favorites? {
    val favoritesItems = filter { it.isFavorite }
    if (favoritesItems.isEmpty()) return null

    val first = favoritesItems.first()
    return Album.Virtual.Favorites(
        count = favoritesItems.size,
        coverUri = first.uri,
        isVideo = first.isVideo
    )
}

private fun List<Media>.toVideosAlbum(): Album.Virtual.Videos? {
    val videoItems = filter { it.isVideo }
    if (videoItems.isEmpty()) return null

    return Album.Virtual.Videos(
        count = videoItems.size,
        coverUri = videoItems.first().uri,
        isVideo = true
    )
}

private fun Album.sortName() = when (this) {
    is Album.Physical -> name
    is Album.Virtual.Favorites -> FAVORITES_ALBUM_NAME
    is Album.Virtual.Videos -> VIDEOS_ALBUM_NAME
}
