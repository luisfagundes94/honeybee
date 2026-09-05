package com.luisfagundes.library.api.domain.model

sealed interface MediaFilter {
    val id: String?

    data object All : MediaFilter {
        override val id: String? = null
    }

    data object Favorites : MediaFilter {
        override val id: String = "favorites"
    }

    data object Videos : MediaFilter {
        override val id: String = "videos"
    }

    data class Bucket(val bucketId: String) : MediaFilter {
        override val id: String = bucketId
    }
}

fun String?.toMediaFilter(): MediaFilter = when (this) {
    null -> MediaFilter.All
    "favorites" -> MediaFilter.Favorites
    "videos" -> MediaFilter.Videos
    else -> MediaFilter.Bucket(this)
}

fun List<Media>.filterBy(mediaFilter: MediaFilter): List<Media> = when (mediaFilter) {
    MediaFilter.All -> this
    MediaFilter.Favorites -> filter { it.isFavorite }
    MediaFilter.Videos -> filter { it.isVideo }
    is MediaFilter.Bucket -> filter { it.bucketId == mediaFilter.bucketId }
}
