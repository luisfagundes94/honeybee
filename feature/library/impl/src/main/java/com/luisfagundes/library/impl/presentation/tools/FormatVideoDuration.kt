package com.luisfagundes.library.impl.presentation.tools

private const val MillisecondsPerSecond = 1_000L
private const val SecondsPerMinute = 60L
private const val MinutesPerHour = 60L

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
