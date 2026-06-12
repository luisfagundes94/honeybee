package com.luisfagundes.library.impl.data.mapper

import com.luisfagundes.library.api.domain.model.Statistics
import com.luisfagundes.library.impl.data.database.entity.StatisticsEntity
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
}
