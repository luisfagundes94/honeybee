package com.luisfagundes.albums.impl.domain.model

internal sealed class Album {
    abstract val id: String
    abstract val count: Int
    abstract val coverUri: String?
    abstract val isVideo: Boolean

    class Physical(
        override val id: String,
        val name: String,
        override val count: Int,
        override val coverUri: String?,
        override val isVideo: Boolean
    ) : Album()

    sealed class Virtual : Album() {
        class Favorites(
            override val count: Int,
            override val coverUri: String?,
            override val isVideo: Boolean
        ) : Virtual() {
            override val id: String get() = FavoritesAlbumId
        }

        class Videos(
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
