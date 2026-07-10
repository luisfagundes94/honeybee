package com.luisfagundes.albums.impl.di

import com.luisfagundes.albums.impl.data.repository.AlbumsRepositoryImpl
import com.luisfagundes.albums.impl.domain.repository.AlbumsRepository
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
    abstract fun bindAlbumsRepository(
        impl: AlbumsRepositoryImpl
    ): AlbumsRepository
}
