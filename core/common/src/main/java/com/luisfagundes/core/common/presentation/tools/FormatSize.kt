package com.luisfagundes.core.common.presentation.tools

import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

/**
 * Formats a size in bytes into a user-friendly Pair representing the value and unit.
 *
 * @param bytes The size in bytes to be formatted.
 * @return A Pair containing the formatted value string and the size unit (B, KB, MB, GB).
 */
fun formatSize(bytes: Long): Pair<String, String> {
    if (bytes <= 0) return Pair("0", "B")
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    val value = bytes / 1024.0.pow(digitGroups.toDouble())
    val formattedValue = if (value % 1.0 == 0.0) {
        String.format(Locale.US, "%.0f", value)
    } else {
        String.format(Locale.US, "%.1f", value)
    }
    return Pair(formattedValue, units[digitGroups])
}
