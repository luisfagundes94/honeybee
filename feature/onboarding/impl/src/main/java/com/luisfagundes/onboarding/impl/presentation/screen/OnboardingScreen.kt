package com.luisfagundes.onboarding.impl.presentation.screen

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.luisfagundes.core.designsystem.components.HoneybeeLogo
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.onboarding.impl.R

@Composable
internal fun OnboardingScreen(
    onGetStartedClick: () -> Unit
) {
    OnboardingContent(
        onGetStartedClick = onGetStartedClick,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun OnboardingContent(
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = {
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(MaterialTheme.spacing.default)
            ) {
                Text(
                    text = stringResource(R.string.get_started)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = MaterialTheme.spacing.default)
        ) {
            HoneybeeLogo()
            Spacer(
                modifier = Modifier.height(MaterialTheme.spacing.default)
            )
            Text(
                text = stringResource(R.string.welcome_to),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(R.string.honeybee),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(
                modifier = Modifier.height(MaterialTheme.spacing.default)
            )
            Text(
                text = stringResource(R.string.honeybee_app_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview(uiMode = UI_MODE_NIGHT_NO)
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun OnboardingPreview() {
    OnboardingContent(
        onGetStartedClick = {},
        modifier = Modifier.fillMaxSize()
    )
}