package com.luisfagundes.library.impl.data.mapper

import com.luisfagundes.library.impl.data.model.MediaDto
import com.luisfagundes.library.api.domain.model.Media
import javax.inject.Inject

internal class MediaMapper @Inject constructor() {
    fun mapToDomain(source: MediaDto): Media {
        return Media(
            id = source.id,
            uri = source.uri,
            dateAdded = source.dateAdded,
            size = source.size,
            mimeType = source.mimeType,
            width = source.width,
            height = source.height,
            durationMillis = source.durationMillis,
            isVideo = source.isVideo,
            bucketId = source.bucketId,
            bucketDisplayName = source.bucketDisplayName,
            isFavorite = source.isFavorite
        )
    }
}
