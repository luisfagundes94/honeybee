package com.luisfagundes.core.billing.di

import com.luisfagundes.core.billing.PremiumBillingCoordinator
import com.luisfagundes.core.billing.data.PlayBillingCoordinator
import com.luisfagundes.core.common.provider.SubscriptionProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface BillingModule {
    @Binds
    @Singleton
    fun bindPremiumBillingCoordinator(impl: PlayBillingCoordinator): PremiumBillingCoordinator

    @Binds
    @Singleton
    fun bindSubscriptionProvider(impl: PlayBillingCoordinator): SubscriptionProvider
}
