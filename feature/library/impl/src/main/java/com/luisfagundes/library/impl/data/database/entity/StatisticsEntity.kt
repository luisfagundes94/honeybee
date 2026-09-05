package com.luisfagundes.library.impl.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistics")
internal data class StatisticsEntity(
    @PrimaryKey val id: Int = 1,
    val memoryCleared: Long,
    val mediaDeleted: Int,
    val photosDeleted: Int,
    val videosDeleted: Int
)
