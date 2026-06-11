package com.luisfagundes.config.impl.presentation.navigation

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.config.api.presentation.navigation.ConfigRoute
import com.luisfagundes.config.api.presentation.navigation.FeedbackRoute
import com.luisfagundes.config.impl.R
import com.luisfagundes.config.impl.presentation.screen.ConfigScreen
import com.luisfagundes.config.impl.presentation.screen.FeedbackScreen
import com.luisfagundes.core.common.presentation.navigation.LocalNavBackStack

internal fun EntryProviderScope<NavKey>.configEntries() {
    entry<ConfigRoute> {
        val backStack = LocalNavBackStack.current
        ConfigScreen(
            onNavigateToFeedback = {
                backStack?.add(FeedbackRoute)
            }
        )
    }

    entry<FeedbackRoute> {
        val backStack = LocalNavBackStack.current
        val context = LocalContext.current
        FeedbackScreen(
            onNavigateBack = {
                backStack?.removeLastOrNull()
            },
            onSubmitFeedback = { feedbackText ->
                Toast.makeText(
                    context,
                    context.getString(R.string.feedback_submitted_success),
                    Toast.LENGTH_SHORT
                ).show()
                backStack?.removeLastOrNull()
            }
        )
    }
}

