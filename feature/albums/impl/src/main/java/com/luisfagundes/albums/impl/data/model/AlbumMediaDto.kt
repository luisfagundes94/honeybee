package com.luisfagundes.albums.impl.data.model

import android.net.Uri

internal data class AlbumMediaDto(
    val id: Long,
    val uri: Uri,
    val dateAdded: Long,
    val isVideo: Boolean,
    val bucketDisplayName: String?,
    val bucketId: String?,
    val isFavorite: Boolean
)
