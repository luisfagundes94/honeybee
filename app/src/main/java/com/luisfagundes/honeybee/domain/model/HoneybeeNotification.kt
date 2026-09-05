package com.luisfagundes.honeybee.domain.model

internal data class HoneybeeNotification(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val deepLinkUrl: String?
)

internal enum class NotificationType {
    NEW_MESSAGE, SYSTEM_ALERT, PROMOTION
}
