package com.luisfagundes.library.impl.di

import com.luisfagundes.library.api.domain.usecase.GetStatisticsUseCase
import com.luisfagundes.library.impl.domain.usecase.GetStatisticsUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UseCaseModule {

    @Binds
    @Singleton
    abstract fun bindGetStatisticsUseCase(
        impl: GetStatisticsUseCaseImpl
    ): GetStatisticsUseCase
}
