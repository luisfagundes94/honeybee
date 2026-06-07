package com.luisfagundes.config.impl.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.config.api.presentation.navigation.ConfigRoute
import com.luisfagundes.config.impl.presentation.screen.ConfigScreen

internal fun EntryProviderScope<NavKey>.configEntries() {
    entry<ConfigRoute> {
        ConfigScreen()
    }
}
