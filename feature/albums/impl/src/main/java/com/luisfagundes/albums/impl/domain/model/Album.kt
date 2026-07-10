package com.luisfagundes.albums.impl.domain.model

import android.net.Uri

internal data class Album(
    val id: String,
    val name: String,
    val count: Int,
    val coverUri: Uri?,
    val isVideo: Boolean
)
