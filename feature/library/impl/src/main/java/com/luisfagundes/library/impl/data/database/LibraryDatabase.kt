package com.luisfagundes.library.impl.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.luisfagundes.library.impl.data.database.dao.StatisticsDao
import com.luisfagundes.library.impl.data.database.entity.StatisticsEntity

@Database(entities = [StatisticsEntity::class], version = 1, exportSchema = false)
internal abstract class LibraryDatabase : RoomDatabase() {
    abstract fun statisticsDao(): StatisticsDao
}
