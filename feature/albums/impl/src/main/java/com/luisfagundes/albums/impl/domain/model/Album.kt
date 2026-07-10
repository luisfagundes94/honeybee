package com.luisfagundes.albums.impl.domain.model

import android.net.Uri

internal sealed class Album {
    abstract val id: String
    abstract val count: Int
    abstract val coverUri: Uri?
    abstract val isVideo: Boolean

    data class Physical(
        override val id: String,
        val name: String,
        override val count: Int,
        override val coverUri: Uri?,
        override val isVideo: Boolean
    ) : Album()

    sealed class Virtual : Album() {
        data class Favorites(
            override val count: Int,
            override val coverUri: Uri?,
            override val isVideo: Boolean
        ) : Virtual() {
            override val id: String get() = ID

            companion object {
                const val ID = "favorites"
            }
        }

        data class Videos(
            override val count: Int,
            override val coverUri: Uri?,
            override val isVideo: Boolean
        ) : Virtual() {
            override val id: String get() = ID

            companion object {
                const val ID = "videos"
            }
        }
    }
}
