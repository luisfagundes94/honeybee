package com.luisfagundes.honeybee.data.notification

import com.google.firebase.messaging.RemoteMessage
import com.luisfagundes.core.common.domain.preferences.UserPreferences
import com.luisfagundes.honeybee.domain.model.HoneybeeNotification
import com.luisfagundes.honeybee.domain.repository.NotificationRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class HoneybeeFirebaseMessagingServiceTest {

    private val repository: NotificationRepository = mockk()
    private val userPreferences: UserPreferences = mockk()
    private val postNotification: (HoneybeeNotification) -> Unit = mockk(relaxed = true)

    @Test
    fun `handleMessage should not process or post notification when disabled`() = runTest {
        // Given
        every { userPreferences.notificationsEnabled() } returns flowOf(false)
        val service = HoneybeeFirebaseMessagingService().apply {
            repository = this@HoneybeeFirebaseMessagingServiceTest.repository
            userPreferences = this@HoneybeeFirebaseMessagingServiceTest.userPreferences
        }

        // When
        service.handleMessage(mockk<RemoteMessage>(), postNotification)

        // Then
        coVerify(exactly = 0) { repository.processIncomingPayload(any()) }
        verify(exactly = 0) { postNotification(any()) }
    }
}
