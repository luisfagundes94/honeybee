package com.luisfagundes.core.ads.di

import com.luisfagundes.core.ads.AdsCoordinator
import com.luisfagundes.core.ads.data.AdsClock
import com.luisfagundes.core.ads.data.AdsCoordinatorImpl
import com.luisfagundes.core.ads.data.SystemAdsClock
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AdsModule {
    @Binds
    @Singleton
    abstract fun bindAdsCoordinator(impl: AdsCoordinatorImpl): AdsCoordinator

    @Binds
    abstract fun bindAdsClock(impl: SystemAdsClock): AdsClock
}
