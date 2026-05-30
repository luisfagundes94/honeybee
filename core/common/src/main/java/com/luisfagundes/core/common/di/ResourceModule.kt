package com.luisfagundes.core.common.di

import com.luisfagundes.core.common.presentation.tools.ResourceProvider
import com.luisfagundes.core.common.presentation.tools.ResourceProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ResourceModule {

    @Binds
    @Singleton
    abstract fun bindResourceProvider(
        resourceProviderImpl: ResourceProviderImpl
    ): ResourceProvider
}
