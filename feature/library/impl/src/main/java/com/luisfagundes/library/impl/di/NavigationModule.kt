package com.luisfagundes.library.impl.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.library.impl.presentation.navigation.libraryEntries
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
    fun provideLibraryEntries(): @JvmSuppressWildcards (EntryProviderScope<NavKey>) -> Unit = { scope ->
        scope.libraryEntries()
    }
}