package com.luisfagundes.honeybee.data.repository

import android.util.Log
import com.luisfagundes.honeybee.domain.model.HoneybeeNotification
import com.luisfagundes.honeybee.domain.model.NotificationType
import com.luisfagundes.honeybee.domain.repository.NotificationRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {

    override suspend fun registerDeviceToken(token: String) {
        Log.d(TAG, "Device token registered: $token")
    }

    override suspend fun processIncomingPayload(rawData: Map<String, String>): HoneybeeNotification {
        Log.d(TAG, "Processing payload: $rawData")
        val id = rawData["id"] ?: UUID.randomUUID().toString()
        val title = rawData["title"].orEmpty()
        val body = rawData["body"].orEmpty()
        val typeStr = rawData["type"]
        val type = try {
            if (typeStr != null) {
                NotificationType.valueOf(typeStr)
            } else {
                NotificationType.SYSTEM_ALERT
            }
        } catch (e: IllegalArgumentException) {
            NotificationType.SYSTEM_ALERT
        }
        val deepLinkUrl = rawData["deepLinkUrl"]

        return HoneybeeNotification(
            id = id,
            title = title,
            body = body,
            type = type,
            deepLinkUrl = deepLinkUrl
        )
    }

    companion object {
        private const val TAG = "NotificationRepo"
    }
}