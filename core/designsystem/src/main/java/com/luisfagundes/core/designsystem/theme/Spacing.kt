package com.luisfagundes.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val verySmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val content: Dp = 12.dp,
    val iconTiny: Dp = 10.dp,
    val iconSmall: Dp = 14.dp,
    val iconMedium: Dp = 20.dp,
    val iconLarge: Dp = 24.dp,
    val default: Dp = 16.dp,
    val large: Dp = 32.dp,
    val veryLarge: Dp = 42.dp,
    val extraLarge: Dp = 52.dp,
    val primaryButtonHeight: Dp = 56.dp,
    val logoSize: Dp = 150.dp,
    val logoShadowRadius: Dp = 1.dp,
    val logoShadowSpread: Dp = 1.dp,
    val logoShadowOffset: Dp = 2.dp,
    val mediaTileMin: Dp = 100.dp,
    val mediaTileWidth: Dp = 108.dp,
    val albumTileMin: Dp = 150.dp,
    val feedbackFieldHeight: Dp = 240.dp,
    val illustrationSize: Dp = 240.dp,
    val largeCorner: Dp = 24.dp
)

val LocalSpacing = compositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
