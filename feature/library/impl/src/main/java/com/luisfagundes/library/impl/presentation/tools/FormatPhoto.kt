package com.luisfagundes.library.impl.presentation.tools

import com.luisfagundes.core.common.presentation.tools.formatSize
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun formatPhotoDate(epochSeconds: Long): String {
    return runCatching {
        val instant = Instant.ofEpochSecond(epochSeconds)
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy • h:mm a", Locale.ENGLISH)
        instant.atZone(ZoneId.systemDefault()).format(formatter)
    }.getOrDefault("")
}

internal fun formatPhotoSize(bytes: Long): String {
    val (value, unit) = formatSize(bytes)
    return "$value $unit"
}

internal fun getFriendlyFileType(mimeType: String?): String {
    if (mimeType == null) return "??"
    val subtype = mimeType.substringAfter('/')
    return when (subtype.lowercase()) {
        "jpeg", "jpg" -> "JPEG"
        "png" -> "PNG"
        "webp" -> "WEBP"
        "gif" -> "GIF"
        "heic", "heif" -> "HEIC"
        else -> subtype.uppercase()
    }
}
