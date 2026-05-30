package com.luisfagundes.onboarding.impl.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.luisfagundes.designsystem.theme.spacing
import com.luisfagundes.onboarding.impl.R

@Composable
internal fun OnboardingScreen() {
    OnboardingContent(
        onGetStartedClick = {},
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun OnboardingContent(
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.welcome_to),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = stringResource(R.string.honeybee),
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(
            modifier = Modifier.height(MaterialTheme.spacing.default)
        )
        Text(
            text = stringResource(R.string.honeybee_app_description),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onGetStartedClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.get_started)
            )
        }
    }
}