package com.luisfagundes.config.impl.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.config.api.presentation.navigation.ConfigRoute
import com.luisfagundes.config.api.presentation.navigation.FeedbackRoute
import com.luisfagundes.config.api.presentation.navigation.StatisticsRoute
import com.luisfagundes.config.impl.presentation.screen.ConfigScreen
import com.luisfagundes.config.impl.presentation.screen.FeedbackScreen
import com.luisfagundes.config.impl.presentation.screen.StatisticsScreen
import com.luisfagundes.core.common.presentation.navigation.LocalNavBackStack

internal fun EntryProviderScope<NavKey>.configEntries() {
    entry<ConfigRoute> {
        val backStack = LocalNavBackStack.current
        ConfigScreen(
            onNavigateToFeedback = {
                backStack?.add(FeedbackRoute)
            },
            onNavigateToStatistics = {
                backStack?.add(StatisticsRoute)
            }
        )
    }

    entry<FeedbackRoute> {
        val backStack = LocalNavBackStack.current
        FeedbackScreen(
            onNavigateBack = {
                backStack?.removeLastOrNull()
            }
        )
    }

    entry<StatisticsRoute> {
        val backStack = LocalNavBackStack.current
        StatisticsScreen(
            onNavigateBack = {
                backStack?.removeLastOrNull()
            }
        )
    }
}

