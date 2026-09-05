package com.luisfagundes.honeybee.di

import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.core.common.provider.SubscriptionStatus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Module
@InstallIn(SingletonComponent::class)
internal object SubscriptionProviderModule {
    @Provides
    @Singleton
    fun provideSubscriptionProvider(): SubscriptionProvider = object : SubscriptionProvider {
        private val _status = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.Free)

        override val status = _status.asStateFlow()

        override suspend fun refresh() = Unit
    }
}
