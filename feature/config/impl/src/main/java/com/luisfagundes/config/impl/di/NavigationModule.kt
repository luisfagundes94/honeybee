package com.luisfagundes.config.impl.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.config.impl.presentation.navigation.configEntries
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal object NavigationModule {
    @IntoSet
    @Provides
    fun provideConfigEntries(): @JvmSuppressWildcards (EntryProviderScope<NavKey>) -> Unit = { scope ->
        scope.configEntries()
    }
}
