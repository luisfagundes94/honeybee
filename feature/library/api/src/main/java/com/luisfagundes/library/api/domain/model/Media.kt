package com.luisfagundes.library.api.domain.model

class Media(
    val id: Long,
    val uri: String,
    val dateAdded: Long,
    val size: Long,
    val mimeType: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val durationMillis: Long = 0L,
    val isVideo: Boolean,
    val bucketId: String? = null,
    val bucketDisplayName: String? = null,
    val isFavorite: Boolean = false
)
