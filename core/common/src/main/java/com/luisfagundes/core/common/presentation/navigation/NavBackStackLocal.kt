package com.luisfagundes.core.common.presentation.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

val LocalNavBackStack = staticCompositionLocalOf<NavBackStack<NavKey>?> {
    null
}
