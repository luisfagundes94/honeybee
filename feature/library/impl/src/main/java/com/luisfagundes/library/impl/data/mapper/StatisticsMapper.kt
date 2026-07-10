package com.luisfagundes.library.impl.data.mapper

import com.luisfagundes.library.api.domain.model.Statistics
import com.luisfagundes.library.impl.data.database.entity.StatisticsEntity
import com.luisfagundes.library.api.domain.model.Media
import javax.inject.Inject

internal class StatisticsMapper @Inject constructor() {
    fun mapToDomain(source: StatisticsEntity?): Statistics {
        return Statistics(
            memoryCleared = source?.memoryCleared ?: 0L,
            mediaDeleted = source?.mediaDeleted ?: 0,
            photosDeleted = source?.photosDeleted ?: 0,
            videosDeleted = source?.videosDeleted ?: 0
        )
    }

    fun mapToUpdatedEntity(
        currentEntity: StatisticsEntity?,
        deletedMedia: List<Media>
    ): StatisticsEntity {
        val current = currentEntity ?: StatisticsEntity(
            memoryCleared = 0L,
            mediaDeleted = 0,
            photosDeleted = 0,
            videosDeleted = 0
        )
        return current.copy(
            memoryCleared = current.memoryCleared + deletedMedia.sumOf { it.size },
            mediaDeleted = current.mediaDeleted + deletedMedia.size,
            photosDeleted = current.photosDeleted + deletedMedia.count { !it.isVideo },
            videosDeleted = current.videosDeleted + deletedMedia.count { it.isVideo }
        )
    }
}
