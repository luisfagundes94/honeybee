package com.luisfagundes.library.impl.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.luisfagundes.library.impl.data.database.entity.StatisticsEntity

@Dao
internal interface StatisticsDao {
    @Query("SELECT * FROM statistics WHERE id = 1 LIMIT 1")
    fun getStatistics(): StatisticsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(statistics: StatisticsEntity): Long
}
