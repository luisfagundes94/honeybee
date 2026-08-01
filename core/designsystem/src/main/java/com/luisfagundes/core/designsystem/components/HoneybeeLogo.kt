package com.luisfagundes.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import com.luisfagundes.core.designsystem.R
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing

@Composable
fun HoneybeeLogo(
    modifier: Modifier = Modifier,
    size: Dp? = null,
    backgroundColor: Color? = null,
    contentScale: ContentScale = ContentScale.Fit
) {
    val resolvedSize = size ?: MaterialTheme.spacing.logoSize
    val resolvedBackgroundColor = backgroundColor ?: MaterialTheme.colorScheme.surface
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(resolvedSize)
            .dropShadow(
                shape = CircleShape,
                shadow = Shadow(
                    radius = MaterialTheme.spacing.logoShadowRadius,
                    spread = MaterialTheme.spacing.logoShadowSpread,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                    offset = DpOffset(
                        x = MaterialTheme.spacing.logoShadowOffset,
                        y = MaterialTheme.spacing.logoShadowOffset
                    )
                )
            )
            .background(
                color = resolvedBackgroundColor,
                shape = CircleShape
            )
            .padding(MaterialTheme.spacing.default)
    ) {
        Image(
            painter = painterResource(R.drawable.honeybee),
            contentDescription = stringResource(R.string.honeybee_logo),
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@PreviewLightDark
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
fun HoneybeeLogoPreview() {
    HoneybeeLogo()
}
