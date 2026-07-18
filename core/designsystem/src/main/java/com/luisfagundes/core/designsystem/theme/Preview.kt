package com.luisfagundes.core.designsystem.theme

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider

class HoneybeeThemeWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(
        content: @Composable (() -> Unit)
    ) {
        HoneybeeTheme {
            Surface {
                content()
            }
        }
    }
}