package com.luisfagundes.honeybee.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.luisfagundes.core.common.di.IoDispatcher
import com.luisfagundes.core.common.domain.preferences.UserPreferences
import com.luisfagundes.honeybee.domain.model.HoneybeeNotification
import com.luisfagundes.honeybee.domain.model.NotificationType
import com.luisfagundes.honeybee.domain.repository.NotificationRepository
import com.luisfagundes.honeybee.presentation.activity.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HoneybeeFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    internal lateinit var repository: NotificationRepository

    @Inject
    @IoDispatcher
    internal lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    internal lateinit var userPreferences: UserPreferences

    private val serviceScope by lazy {
        CoroutineScope(SupervisorJob() + ioDispatcher)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            repository.registerDeviceToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        serviceScope.launch {
            handleMessage(remoteMessage)
        }
    }

    internal suspend fun handleMessage(
        remoteMessage: RemoteMessage,
        postNotification: (HoneybeeNotification) -> Unit = ::showNotification
    ) {
        if (!userPreferences.notificationsEnabled().first()) {
            return
        }

        val data = remoteMessage.data.toMutableMap()
        remoteMessage.notification?.let { notification ->
            if (!data.containsKey("title")) {
                data["title"] = notification.title.orEmpty()
            }
            if (!data.containsKey("body")) {
                data["body"] = notification.body.orEmpty()
            }
        }

        val notification = repository.processIncomingPayload(data)
        postNotification(notification)
    }

    private fun showNotification(notification: HoneybeeNotification) {
        createNotificationChannels()

        val channelId = getChannelId(notification.type)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            notification.deepLinkUrl?.let { url ->
                if (url.isNotBlank()) {
                    putExtra("deepLinkUrl", url)
                    data = url.toUri()
                }
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.luisfagundes.honeybee.R.mipmap.ic_launcher)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                when (notification.type) {
                    NotificationType.SYSTEM_ALERT -> NotificationCompat.PRIORITY_HIGH
                    NotificationType.NEW_MESSAGE -> NotificationCompat.PRIORITY_DEFAULT
                    NotificationType.PROMOTION -> NotificationCompat.PRIORITY_DEFAULT
                }
            )

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notification.id.hashCode(), builder.build())
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channels = listOf(
            NotificationChannel(
                CHANNEL_NEW_MESSAGE,
                "New Messages",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new messages received"
            },
            NotificationChannel(
                CHANNEL_SYSTEM_ALERT,
                "System Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical system alerts and notifications"
            },
            NotificationChannel(
                CHANNEL_PROMOTION,
                "Promotions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Promotional offers and news"
            }
        )
        notificationManager.createNotificationChannels(channels)
    }

    private fun getChannelId(type: NotificationType): String {
        return when (type) {
            NotificationType.NEW_MESSAGE -> CHANNEL_NEW_MESSAGE
            NotificationType.SYSTEM_ALERT -> CHANNEL_SYSTEM_ALERT
            NotificationType.PROMOTION -> CHANNEL_PROMOTION
        }
    }

    companion object {
        private const val CHANNEL_NEW_MESSAGE = "new_messages"
        private const val CHANNEL_SYSTEM_ALERT = "system_alerts"
        private const val CHANNEL_PROMOTION = "promotions"
    }
}
