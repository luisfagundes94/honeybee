package com.luisfagundes.honeybee.di

import com.luisfagundes.core.common.provider.SubscriptionProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SubscriptionModule {
    @Binds
    @Singleton
    abstract fun bindSubscriptionProvider(
        impl: PaidSubscriptionProvider
    ): SubscriptionProvider
}

internal class PaidSubscriptionProvider @Inject constructor() : SubscriptionProvider {
    override fun isPremium(): Boolean = true
}
