package com.luisfagundes.premium.impl.di

import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.premium.impl.data.PlayBillingSubscriptionProvider
import com.luisfagundes.premium.impl.domain.repository.PremiumSubscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SubscriptionModule {
    @Binds
    @Singleton
    abstract fun bindSubscriptionProvider(
        impl: PlayBillingSubscriptionProvider,
    ): SubscriptionProvider

    @Binds
    @Singleton
    abstract fun bindPremiumSubscriptionRepository(
        impl: PlayBillingSubscriptionProvider,
    ): PremiumSubscriptionRepository
}
