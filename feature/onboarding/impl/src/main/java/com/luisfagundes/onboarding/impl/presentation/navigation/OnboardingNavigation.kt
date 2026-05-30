package com.luisfagundes.onboarding.impl.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.onboarding.api.presentation.navigation.OnboardingRoute
import com.luisfagundes.onboarding.impl.presentation.screen.OnboardingScreen

internal fun EntryProviderScope<NavKey>.onboardingEntries() {
    entry<OnboardingRoute> {
        OnboardingScreen()
    }
}