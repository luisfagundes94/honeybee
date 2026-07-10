package com.luisfagundes.albums.impl.domain.model

import android.net.Uri

internal data class AlbumMedia(
    val id: Long,
    val uri: Uri,
    val dateAdded: Long,
    val isVideo: Boolean
)
