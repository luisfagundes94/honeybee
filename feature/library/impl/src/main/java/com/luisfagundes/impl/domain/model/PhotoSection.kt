package com.luisfagundes.impl.domain.model

import java.time.YearMonth

internal data class PhotoSection(
    val yearMonth: YearMonth,
    val photos: List<Photo>
)
