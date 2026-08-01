package com.luisfagundes.premium.impl.presentation.navigation

import android.app.Activity
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.core.common.presentation.navigation.LocalNavBackStack
import com.luisfagundes.premium.api.presentation.navigation.PremiumRoute
import com.luisfagundes.premium.impl.presentation.screen.PremiumScreen

internal fun EntryProviderScope<NavKey>.premiumEntries(
    onLaunchPurchase: (Activity, String) -> Unit,
    onOpenSubscriptionManagement: (Activity) -> Unit,
) {
    entry<PremiumRoute> {
        val backStack = LocalNavBackStack.current
        PremiumScreen(
            onNavigateBack = { backStack?.removeLastOrNull() },
            onLaunchPurchase = onLaunchPurchase,
            onOpenSubscriptionManagement = onOpenSubscriptionManagement,
        )
    }
}
