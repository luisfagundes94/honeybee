package com.luisfagundes.library.api.domain.model

data class Statistics(
    val memoryCleared: Long,
    val mediaDeleted: Int,
    val photosDeleted: Int,
    val videosDeleted: Int
)
