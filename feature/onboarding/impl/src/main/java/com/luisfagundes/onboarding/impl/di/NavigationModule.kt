package com.luisfagundes.onboarding.impl.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.onboarding.impl.presentation.navigation.onboardingEntries
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object NavigationModule {
    @IntoSet
    @Provides
    fun provideLibraryEntries(): @JvmSuppressWildcards (EntryProviderScope<NavKey>) -> Unit = { scope ->
        scope.onboardingEntries()
    }
}