package com.luisfagundes.library.impl.domain.model

import java.time.YearMonth

internal data class MediaSection(
    val yearMonth: YearMonth,
    val mediaList: List<Media>
)
