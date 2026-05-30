package com.luisfagundes.onboarding.impl.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.core.common.presentation.navigation.LocalNavBackStack
import com.luisfagundes.onboarding.api.presentation.navigation.OnboardingRoute
import com.luisfagundes.onboarding.impl.presentation.navigation.routes.PermissionRoute
import com.luisfagundes.onboarding.impl.presentation.screen.OnboardingScreen
import com.luisfagundes.onboarding.impl.presentation.screen.PermissionScreen

internal fun EntryProviderScope<NavKey>.onboardingEntries() {
    entry<OnboardingRoute> {
        val backStack = LocalNavBackStack.current
        OnboardingScreen(
            onGetStartedClick = {
                backStack?.add(PermissionRoute)
            }
        )
    }
    entry<PermissionRoute> {
        PermissionScreen(
            onAllowAccessClick = {
                // Handle permission and navigate
            }
        )
    }
}