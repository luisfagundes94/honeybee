package com.luisfagundes.honeybee.domain.repository

import com.luisfagundes.honeybee.domain.model.HoneybeeNotification

internal interface NotificationRepository {
    suspend fun registerDeviceToken(token: String)
    suspend fun processIncomingPayload(rawData: Map<String, String>): HoneybeeNotification
}