package com.luisfagundes.library.impl.data.model

import android.net.Uri

internal data class PhotoDto(
    val id: Long,
    val uri: Uri,
    val dateAdded: Long
)