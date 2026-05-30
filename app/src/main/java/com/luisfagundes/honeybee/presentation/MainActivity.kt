package com.luisfagundes.honeybee.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import com.luisfagundes.core.common.presentation.navigation.LocalNavBackStack
import com.luisfagundes.designsystem.theme.HoneybeeTheme
import com.luisfagundes.honeybee.presentation.navigation.AppNavDisplay
import com.luisfagundes.onboarding.api.presentation.navigation.OnboardingRoute
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var entryBuilders: @JvmSuppressWildcards Set<(EntryProviderScope<NavKey>) -> Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HoneybeeTheme {
                val backStack = rememberNavBackStack(OnboardingRoute)

                CompositionLocalProvider(LocalNavBackStack provides backStack) {
                    Scaffold { innerPadding ->
                        AppNavDisplay(
                            backStack = backStack,
                            entryProvider = entryProvider {
                                entryBuilders.forEach { it(this) }
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}