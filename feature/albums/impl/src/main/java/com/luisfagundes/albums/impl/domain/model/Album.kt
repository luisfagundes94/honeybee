package com.luisfagundes.albums.impl.domain.model

internal sealed class Album {
    abstract val id: String
    abstract val count: Int
    abstract val coverUri: String?
    abstract val isVideo: Boolean

    data class Physical(
        override val id: String,
        val name: String,
        override val count: Int,
        override val coverUri: String?,
        override val isVideo: Boolean
    ) : Album()

    sealed class Virtual : Album() {
        data class Favorites(
            override val count: Int,
            override val coverUri: String?,
            override val isVideo: Boolean
        ) : Virtual() {
            override val id: String get() = FavoritesAlbumId
        }

        data class Videos(
            override val count: Int,
            override val coverUri: String?,
            override val isVideo: Boolean
        ) : Virtual() {
            override val id: String get() = VideosAlbumId
        }
    }
}

internal const val FavoritesAlbumId = "favorites"
internal const val VideosAlbumId = "videos"
