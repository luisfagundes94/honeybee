package com.luisfagundes.honeybee.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import com.luisfagundes.core.common.presentation.navigation.LocalNavBackStack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.luisfagundes.designsystem.theme.HoneybeeTheme
import com.luisfagundes.honeybee.presentation.navigation.AppNavDisplay
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