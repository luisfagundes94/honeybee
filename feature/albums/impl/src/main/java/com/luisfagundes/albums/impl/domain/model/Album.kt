package com.luisfagundes.albums.impl.domain.model

import com.luisfagundes.library.api.domain.model.MediaFilter

internal sealed class Album {
    abstract val id: String
    abstract val count: Int
    abstract val coverUri: String?

    data class Physical(
        override val id: String,
        val name: String,
        override val count: Int,
        override val coverUri: String?
    ) : Album()

    sealed class Virtual : Album() {
        data class Favorites(
            override val count: Int,
            override val coverUri: String?
        ) : Virtual() {
            override val id: String get() = MediaFilter.Favorites.id.orEmpty()
        }

        data class Videos(
            override val count: Int,
            override val coverUri: String?
        ) : Virtual() {
            override val id: String get() = MediaFilter.Videos.id.orEmpty()
        }
    }
}
