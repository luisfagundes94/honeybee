package com.luisfagundes.library.impl.di

import com.luisfagundes.library.impl.data.repository.LibraryRepositoryImpl
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMediaRepository(
        mediaRepositoryImpl: LibraryRepositoryImpl
    ): LibraryRepository
}
