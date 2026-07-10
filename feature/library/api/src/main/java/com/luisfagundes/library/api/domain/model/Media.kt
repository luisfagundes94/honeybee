package com.luisfagundes.library.api.domain.model

import android.net.Uri

data class Media(
    val id: Long,
    val uri: Uri,
    val dateAdded: Long,
    val size: Long,
    val mimeType: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val isVideo: Boolean,
    val bucketId: String? = null,
    val bucketDisplayName: String? = null,
    val isFavorite: Boolean = false
)
