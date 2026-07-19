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
import androidx.compose.ui.unit.dp
import com.luisfagundes.core.designsystem.R
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing

@Composable
fun HoneybeeLogo(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    backgroundColor: Color = Color.White,
    contentScale: ContentScale = ContentScale.Fit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .dropShadow(
                shape = CircleShape,
                shadow = Shadow(
                    radius = 1.dp,
                    spread = 1.dp,
                    color = Color(0x40000000),
                    offset = DpOffset(x = 2.dp, y = 2.dp)
                )
            )
            .background(
                color = backgroundColor,
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
