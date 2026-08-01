package com.luisfagundes.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing

@ExperimentalMaterial3ExpressiveApi
@Composable
fun HoneybeeLoadingTemplate(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.default),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = MaterialTheme.spacing.default,
            alignment = Alignment.CenterVertically
        )
    ) {
        LoadingIndicator()
        message?.let {
            Text(
                text = it,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@PreviewLightDark
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
fun HoneybeeLoadingTemplatePreview() {
    HoneybeeLoadingTemplate(message = "Loading your media")
}
