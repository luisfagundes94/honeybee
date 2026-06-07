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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import com.luisfagundes.core.common.presentation.navigation.LocalNavBackStack
import com.luisfagundes.designsystem.theme.HoneybeeTheme
import com.luisfagundes.albums.api.presentation.navigation.AlbumsRoute
import com.luisfagundes.config.api.presentation.navigation.ConfigRoute
import com.luisfagundes.honeybee.presentation.navigation.AppNavDisplay
import com.luisfagundes.honeybee.presentation.navigation.TopLevelDestination
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val onboardingCompleted by viewModel.isOnboardingCompleted()
                .collectAsStateWithLifecycle(initialValue = null)
            
            HoneybeeTheme {
                if (onboardingCompleted == null) return@HoneybeeTheme

                val startRoute = if (onboardingCompleted == true) LibraryRoute else OnboardingRoute
                val backStack = rememberNavBackStack(startRoute)

                CompositionLocalProvider(LocalNavBackStack provides backStack) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val currentRoute = backStack.lastOrNull()
                        val topLevelRoutes = remember {
                            setOf(LibraryRoute, AlbumsRoute, ConfigRoute)
                        }
                        val shouldShowNavBar = currentRoute in topLevelRoutes

                        var isScrolledVisible by remember { mutableStateOf(true) }
                        LaunchedEffect(currentRoute) {
                            isScrolledVisible = true
                        }

                        val adaptiveInfo = currentWindowAdaptiveInfo()
                        val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
                        val isBottomBar = layoutType == NavigationSuiteType.NavigationBar

                        val nestedScrollConnection = remember(isBottomBar, currentRoute) {
                            object : NestedScrollConnection {
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

                        LaunchedEffect(isBottomBar) {
                            if (!isBottomBar) {
                                isScrolledVisible = true
                            }
                        }

                        val isNavBarVisible = shouldShowNavBar && (currentRoute != LibraryRoute || isScrolledVisible)
                        val scaffoldVisibilityState = rememberNavigationSuiteScaffoldState()

                        LaunchedEffect(isNavBarVisible) {
                            if (isNavBarVisible) {
                                scaffoldVisibilityState.show()
                            } else {
                                scaffoldVisibilityState.hide()
                            }
                        }

                        NavigationSuiteScaffold(
                            state = scaffoldVisibilityState,
                            navigationSuiteItems = {
                                TopLevelDestination.entries.forEach { destination ->
                                    item(
                                        selected = currentRoute == destination.route,
                                        onClick = {
                                            if (currentRoute != destination.route) {
                                                backStack.clear()
                                                backStack.add(destination.route)
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = destination.icon,
                                                contentDescription = stringResource(destination.labelRes)
                                            )
                                        },
                                        label = {
                                            Text(text = stringResource(destination.labelRes))
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(nestedScrollConnection)
                        ) {
                            AppNavDisplay(
                                backStack = backStack,
                                entryProvider = entryProvider {
                                    entryBuilders.forEach { it(this) }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}