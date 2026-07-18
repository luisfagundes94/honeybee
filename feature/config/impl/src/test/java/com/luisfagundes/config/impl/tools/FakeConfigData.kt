package com.luisfagundes.config.impl.tools

import com.luisfagundes.library.api.domain.model.Statistics

internal val fakeStatistics = Statistics(
    memoryCleared = 1_024L,
    mediaDeleted = 5,
    photosDeleted = 3,
    videosDeleted = 2
)
