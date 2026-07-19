package com.luisfagundes.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.luisfagundes.core.designsystem.R
import com.luisfagundes.core.designsystem.theme.spacing

private const val ContainerAlpha = 0.5f
private const val MillisecondsPerSecond = 1_000L
private const val SecondsPerMinute = 60L
private const val MinutesPerHour = 60L

@Composable
fun VideoDurationBadge(
    durationMillis: Long,
    modifier: Modifier = Modifier
) {
    val formattedDuration = durationMillis.formatVideoDuration()
    val durationContentDescription = stringResource(
        R.string.video_duration_content_description,
        formattedDuration
    )

    Text(
        text = formattedDuration,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = ContainerAlpha),
                shape = RoundedCornerShape(MaterialTheme.spacing.verySmall)
            )
            .padding(MaterialTheme.spacing.verySmall)
            .semantics {
                contentDescription = durationContentDescription
            }
    )
}

internal fun Long.formatVideoDuration(): String {
    val totalSeconds = coerceAtLeast(0L) / MillisecondsPerSecond
    val seconds = totalSeconds % SecondsPerMinute
    val totalMinutes = totalSeconds / SecondsPerMinute
    val minutes = totalMinutes % MinutesPerHour
    val hours = totalMinutes / MinutesPerHour

    return if (hours > 0L) {
        "$hours:${minutes.twoDigits()}:${seconds.twoDigits()}"
    } else {
        "$minutes:${seconds.twoDigits()}"
    }
}

private fun Long.twoDigits(): String = toString().padStart(length = 2, padChar = '0')
