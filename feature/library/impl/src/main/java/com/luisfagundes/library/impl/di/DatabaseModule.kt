package com.luisfagundes.library.impl.di

import android.content.Context
import androidx.room.Room
import com.luisfagundes.library.impl.data.database.LibraryDatabase
import com.luisfagundes.library.impl.data.database.dao.StatisticsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "library_database"

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideLibraryDatabase(
        @ApplicationContext context: Context
    ): LibraryDatabase {
        return Room.databaseBuilder(
            context,
            LibraryDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideStatisticsDao(database: LibraryDatabase): StatisticsDao {
        return database.statisticsDao()
    }
}
