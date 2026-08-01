package com.luisfagundes.premium.impl.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.premium.impl.data.PlayBillingSubscriptionProvider
import com.luisfagundes.premium.impl.presentation.navigation.premiumEntries
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
    fun providePremiumEntries(
        subscriptionProvider: PlayBillingSubscriptionProvider,
    ): @JvmSuppressWildcards (EntryProviderScope<NavKey>) -> Unit = { scope ->
        scope.premiumEntries(
            onLaunchPurchase = subscriptionProvider::launchPurchase,
            onOpenSubscriptionManagement = subscriptionProvider::openSubscriptionManagement,
        )
    }
}
