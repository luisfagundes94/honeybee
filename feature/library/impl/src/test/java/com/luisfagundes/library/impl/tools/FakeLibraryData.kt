package com.luisfagundes.library.impl.tools

import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.api.domain.model.Statistics
import com.luisfagundes.library.impl.data.database.entity.StatisticsEntity
import com.luisfagundes.library.impl.data.model.MediaDto
import io.mockk.mockk

internal val fakeMedia = Media(
    id = 1L,
    uri = mockk(),
    dateAdded = 1_000L,
    size = 2_000L,
    isVideo = false
)

internal val fakeMediaDto = MediaDto(
    id = 1L,
    uri = mockk(),
    dateAdded = 1_000L,
    size = 2_000L,
    isVideo = false
)

internal val fakeStatisticsEntity = StatisticsEntity(
    id = 1,
    memoryCleared = 1_024L,
    mediaDeleted = 5,
    photosDeleted = 3,
    videosDeleted = 2
)

internal val fakeStatistics = Statistics(
    memoryCleared = 1_024L,
    mediaDeleted = 5,
    photosDeleted = 3,
    videosDeleted = 2
)
