package com.luisfagundes.honeybee.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import com.luisfagundes.core.common.presentation.navigation.LocalNavBackStack
import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.core.common.provider.SubscriptionStatus
import com.luisfagundes.core.designsystem.theme.HoneybeeTheme
import com.luisfagundes.core.ads.AdsCoordinator
import com.luisfagundes.albums.api.presentation.navigation.AlbumsRoute
import com.luisfagundes.config.api.presentation.navigation.ConfigRoute
import com.luisfagundes.honeybee.presentation.navigation.AppNavDisplay
import com.luisfagundes.honeybee.presentation.navigation.TopLevelDestination
import com.luisfagundes.honeybee.presentation.navigation.TopLevelNavigationState
import com.luisfagundes.honeybee.presentation.navigation.rememberTopLevelNavigationState
import com.luisfagundes.honeybee.presentation.event.MainUiEvent
import com.luisfagundes.honeybee.presentation.state.MainUiState
import com.luisfagundes.honeybee.presentation.viewmodel.MainViewModel
import com.luisfagundes.library.api.presentation.navigation.LibraryRoute
import com.luisfagundes.onboarding.api.presentation.navigation.OnboardingRoute
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var entryBuilders: @JvmSuppressWildcards Set<(EntryProviderScope<NavKey>) -> Unit>

    @Inject
    lateinit var adsCoordinator: AdsCoordinator

    @Inject
    lateinit var subscriptionProvider: SubscriptionProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainUiState by viewModel.uiState.collectAsStateWithLifecycle()
            val subscriptionStatus by subscriptionProvider.status.collectAsStateWithLifecycle()

            HoneybeeTheme {
                val onboardingCompleted = (mainUiState as? MainUiState.Content)
                    ?.isOnboardingCompleted
                    ?: return@HoneybeeTheme

                LaunchedEffect(subscriptionStatus) {
                    if (subscriptionStatus == SubscriptionStatus.Free) {
                        adsCoordinator.gatherConsent(this@MainActivity)
                    }
                }

                val startRoute = if (onboardingCompleted) LibraryRoute else OnboardingRoute
                val onboardingBackStack = rememberNavBackStack(OnboardingRoute)
                val navigationState = rememberTopLevelNavigationState()

                if (startRoute == OnboardingRoute) {
                    OnboardingContent(
                        backStack = onboardingBackStack,
                        entryBuilders = entryBuilders,
                        onExit = ::finish,
                    )
                } else {
                    MainContent(
                        navigationState = navigationState,
                        entryBuilders = entryBuilders,
                        onExit = ::finish,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.dispatchEvent(MainUiEvent.RefreshSubscription)
    }
}

@Composable
private fun MainContent(
    navigationState: TopLevelNavigationState,
    entryBuilders: Set<(EntryProviderScope<NavKey>) -> Unit>,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val currentRoute = navigationState.currentBackStack.lastOrNull()
        val topLevelRoutes = remember {
            setOf(LibraryRoute, AlbumsRoute, ConfigRoute)
        }
        val shouldShowNavBar = currentRoute in topLevelRoutes

        val adaptiveInfo = currentWindowAdaptiveInfo()
        val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
        val isBottomBar = layoutType == NavigationSuiteType.NavigationBar

        val scrollState = rememberScaffoldScrollState(
            currentRoute = currentRoute,
            isBottomBar = isBottomBar,
        )

        val isScrollVisible = currentRoute != LibraryRoute || scrollState.isScrolledVisible
        val isNavBarVisible = shouldShowNavBar && isScrollVisible
        val scaffoldVisibilityState = rememberNavigationSuiteScaffoldState()

        LaunchedEffect(isNavBarVisible) {
            if (isNavBarVisible) {
                scaffoldVisibilityState.show()
            } else {
                scaffoldVisibilityState.hide()
            }
        }

        MainNavigationSuite(
            navigationState = navigationState,
            entryBuilders = entryBuilders,
            scaffoldVisibilityState = scaffoldVisibilityState,
            scrollState = scrollState,
            onExit = onExit,
        )
    }
}

@Composable
private fun MainNavigationSuite(
    navigationState: TopLevelNavigationState,
    entryBuilders: Set<(EntryProviderScope<NavKey>) -> Unit>,
    scaffoldVisibilityState: androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState,
    scrollState: ScaffoldScrollState,
    onExit: () -> Unit,
) {
    CompositionLocalProvider(LocalNavBackStack provides navigationState.currentBackStack) {
        NavigationSuiteScaffold(
            state = scaffoldVisibilityState,
            navigationSuiteItems = {
                TopLevelDestination.entries.forEach { destination ->
                    item(
                        selected = navigationState.selectedRoute == destination.route,
                        onClick = { navigationState.select(destination.route) },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = stringResource(destination.labelRes)
                            )
                        },
                        label = { Text(text = stringResource(destination.labelRes)) }
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollState.nestedScrollConnection)
        ) {
            AppNavDisplay(
                navigationState = navigationState,
                entryProvider = entryProvider {
                    entryBuilders.forEach { it(this) }
                },
                onExit = onExit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun OnboardingContent(
    backStack: NavBackStack<NavKey>,
    entryBuilders: Set<(EntryProviderScope<NavKey>) -> Unit>,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalNavBackStack provides backStack) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            AppNavDisplay(
                backStack = backStack,
                entryProvider = entryProvider {
                    entryBuilders.forEach { it(this) }
                },
                onBack = {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.lastIndex)
                    } else {
                        onExit()
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Stable
private class ScaffoldScrollState(
    isBottomBar: Boolean,
    currentRoute: NavKey?,
) {
    var isScrolledVisible by mutableStateOf(true)
        private set

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            if (isBottomBar && currentRoute == LibraryRoute) {
                if (available.y < -5f) {
                    isScrolledVisible = false
                } else if (available.y > 5f) {
                    isScrolledVisible = true
                }
            }
            return Offset.Zero
        }
    }
}

@Composable
private fun rememberScaffoldScrollState(
    currentRoute: NavKey?,
    isBottomBar: Boolean,
): ScaffoldScrollState {
    return remember(isBottomBar, currentRoute) {
        ScaffoldScrollState(isBottomBar, currentRoute)
    }
}
