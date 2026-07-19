package com.luisfagundes.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing

@Composable
fun HoneybeeErrorTemplate(
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String,
    primaryButtonLabel: String,
    onPrimaryButtonClick: () -> Unit,
    secondaryButtonLabel: String? = null,
    onSecondaryButtonClick: () -> Unit = { }
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.default)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(MaterialTheme.spacing.extraLarge)
            )
            title?.let {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Text(
                text = description
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.default)
        ) {
            Button(
                onClick = onPrimaryButtonClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = primaryButtonLabel
                )
            }
            secondaryButtonLabel?.let {
                OutlinedButton(
                    onClick = onSecondaryButtonClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = secondaryButtonLabel
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun HoneybeeErrorTemplatePreview() {
    HoneybeeErrorTemplate(
        title = "Something went wrong",
        description = "An unexpected error occurred",
        primaryButtonLabel = "Retry",
        onPrimaryButtonClick = {},
        secondaryButtonLabel = "Cancel",
        onSecondaryButtonClick = {}
    )
}
