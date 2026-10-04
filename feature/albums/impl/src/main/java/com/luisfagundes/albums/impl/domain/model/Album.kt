package com.luisfagundes.albums.impl.domain.model

import com.luisfagundes.library.api.domain.model.MediaFilter

internal sealed interface Album {
    val id: String
    val count: Int
    val coverUri: String?

    data class Physical(
        override val id: String,
        val name: String,
        override val count: Int,
        override val coverUri: String?
    ) : Album

    sealed interface Virtual : Album {
        data class Favorites(
            override val count: Int,
            override val coverUri: String?
        ) : Virtual {
            override val id: String get() = MediaFilter.Favorites.id
        }

        data class Videos(
            override val count: Int,
            override val coverUri: String?
        ) : Virtual {
            override val id: String get() = MediaFilter.Videos.id
        }
    }
}
