package com.luisfagundes.albums.impl.domain.model

internal data class AlbumMedia(
    val id: Long,
    val uri: String,
    val dateAdded: Long,
    val durationMillis: Long = 0L,
    val isVideo: Boolean
)
