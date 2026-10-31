package com.luisfagundes.library.impl.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
internal object ClockModule {

    @Provides
    fun provideClock(): Clock = Clock.systemUTC()
}
