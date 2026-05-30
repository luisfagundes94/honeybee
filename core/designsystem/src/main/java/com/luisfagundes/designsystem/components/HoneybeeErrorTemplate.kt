package com.luisfagundes.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.luisfagundes.designsystem.R
import com.luisfagundes.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.designsystem.theme.spacing

@Composable
fun HoneybeeErrorTemplate(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(MaterialTheme.spacing.extraLarge)
        )
        Spacer(
            modifier = Modifier.height(MaterialTheme.spacing.default)
        )
        Text(
            text = message
        )
        Spacer(
            modifier = Modifier.height(MaterialTheme.spacing.default)
        )
        Button(onClick = onRetry) {
            Text(
                text = stringResource(R.string.retry)
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun HoneybeeErrorTemplatePreview() {
    HoneybeeErrorTemplate(
        modifier = Modifier.fillMaxSize(),
        message = "An unexpected error occurred",
        onRetry = {}
    )
}
