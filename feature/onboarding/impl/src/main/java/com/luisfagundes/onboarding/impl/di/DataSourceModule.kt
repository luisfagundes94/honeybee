package com.luisfagundes.onboarding.impl.di

import com.luisfagundes.onboarding.impl.data.datasource.OnboardingDataSource
import com.luisfagundes.onboarding.impl.data.datasource.OnboardingDataSourceImpl
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
    abstract fun bindOnboardingDataSource(
        onboardingDataSourceImpl: OnboardingDataSourceImpl
    ): OnboardingDataSource
}
