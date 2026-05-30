package com.luisfagundes.impl.domain.model

import android.net.Uri

internal data class Photo(
    val id: Long,
    val uri: Uri,
    val dateAdded: Long
)
