package com.luisfagundes.honeybee.presentation.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

@Composable
fun AppNavDisplay(
    backStack: NavBackStack<NavKey>,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            rememberViewModelStoreNavEntryDecorator<NavKey>(),
        ),
        entryProvider = entryProvider,
        transitionSpec = { slideForward() },
        popTransitionSpec = { slideBackward() },
        predictivePopTransitionSpec = { slideBackward() },
        modifier = modifier
    )
}

private fun slideForward(): ContentTransform =
    slideInHorizontally(initialOffsetX = { it }) togetherWith
            slideOutHorizontally(targetOffsetX = { -it })

private fun slideBackward(): ContentTransform =
    slideInHorizontally(initialOffsetX = { -it }) togetherWith
            slideOutHorizontally(targetOffsetX = { it })