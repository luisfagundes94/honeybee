package com.luisfagundes.config.impl.di

import com.luisfagundes.config.impl.data.repository.ConfigRepositoryImpl
import com.luisfagundes.config.impl.domain.repository.ConfigRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ConfigRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindConfigRepository(
        configRepositoryImpl: ConfigRepositoryImpl
    ): ConfigRepository
}
