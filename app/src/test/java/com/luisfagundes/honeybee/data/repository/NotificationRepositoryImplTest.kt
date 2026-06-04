package com.luisfagundes.honeybee.data.repository

import com.luisfagundes.honeybee.domain.model.NotificationType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationRepositoryImplTest {

    private val repository = NotificationRepositoryImpl()

    @Test
    fun `registerDeviceToken should complete successfully`() = runTest {
        // Given
        val token = "test_token"

        // When/Then
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
        val result = repository.processIncomingPayload(rawData)

        // Then
        assertEquals("123", result.id)
        assertEquals("Test Title", result.title)
        assertEquals("Test Body", result.body)
        assertEquals(NotificationType.NEW_MESSAGE, result.type)
        assertEquals("https://honeybee.com/promo", result.deepLinkUrl)
    }

    @Test
    fun `processIncomingPayload should handle missing or empty fields with default values`() = runTest {
        // Given
        val rawData = emptyMap<String, String>()

        // When
        val result = repository.processIncomingPayload(rawData)

        // Then
        assertNotNull(result.id)
        assertEquals("", result.title)
        assertEquals("", result.body)
        assertEquals(NotificationType.SYSTEM_ALERT, result.type)
        assertEquals(null, result.deepLinkUrl)
    }

    @Test
    fun `processIncomingPayload should default to SYSTEM_ALERT when type is invalid`() = runTest {
        // Given
        val rawData = mapOf(
            "type" to "INVALID_TYPE_STUFF"
        )

        // When
        val result = repository.processIncomingPayload(rawData)

        // Then
        assertEquals(NotificationType.SYSTEM_ALERT, result.type)
    }
}
