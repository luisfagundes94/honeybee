package com.luisfagundes.honeybee.data.repository

import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.honeybee.domain.model.HoneybeeNotification
import com.luisfagundes.honeybee.domain.model.NotificationType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class NotificationRepositoryImplTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val repository = NotificationRepositoryImpl()

    @Test
    fun `registerDeviceToken should complete successfully`() = runTest {
        // Given
        val token = "test_token"

        // When & Then
        repository.registerDeviceToken(token)
    }

    @Test
    fun `processIncomingPayload should parse all values correctly`() = runTest {
        // Given
        val rawData = mapOf(
            "id" to "123",
            "title" to "Test Title",
            "body" to "Test Body",
            "type" to "NEW_MESSAGE",
            "deepLinkUrl" to "https://honeybee.com/promo"
        )

        // When
        val notification = repository.processIncomingPayload(rawData)

        // Then
        assertEquals(
            HoneybeeNotification(
                id = "123",
                title = "Test Title",
                body = "Test Body",
                type = NotificationType.NEW_MESSAGE,
                deepLinkUrl = "https://honeybee.com/promo"
            ),
            notification
        )
    }

    @Test
    fun `processIncomingPayload should handle missing or empty fields with default values`() = runTest {
        // Given
        val rawData = emptyMap<String, String>()

        // When
        val notification = repository.processIncomingPayload(rawData)

        // Then
        assertNotNull(notification.id)
        assertEquals("", notification.title)
        assertEquals("", notification.body)
        assertEquals(NotificationType.SYSTEM_ALERT, notification.type)
        assertEquals(null, notification.deepLinkUrl)
    }

    @Test
    fun `processIncomingPayload should default to SYSTEM_ALERT when type is invalid`() = runTest {
        // Given
        val rawData = mapOf(
            "type" to "INVALID_TYPE_STUFF"
        )

        // When
        val notification = repository.processIncomingPayload(rawData)

        // Then
        assertEquals(NotificationType.SYSTEM_ALERT, notification.type)
    }
}
