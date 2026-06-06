package com.luisfagundes.library.impl.domain.model

import android.net.Uri

internal data class Media(
    val id: Long,
    val uri: Uri,
    val dateAdded: Long,
    val size: Long,
    val mimeType: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val isVideo: Boolean
)
