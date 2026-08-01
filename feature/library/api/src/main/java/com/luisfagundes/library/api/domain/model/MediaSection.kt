package com.luisfagundes.library.api.domain.model

import java.time.YearMonth

class MediaSection(
    val yearMonth: YearMonth,
    val mediaList: List<Media>
)
