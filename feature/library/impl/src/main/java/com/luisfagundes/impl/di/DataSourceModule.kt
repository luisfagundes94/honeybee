package com.luisfagundes.impl.di

import com.luisfagundes.impl.data.datasource.LocalLibraryDataSource
import com.luisfagundes.impl.data.datasource.LibraryDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindMediaDataSource(
        localMediaDataSource: LocalLibraryDataSource
    ): LibraryDataSource
}
