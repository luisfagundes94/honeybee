package com.luisfagundes.library.impl.data.datasource

import android.content.Context
import android.content.SharedPreferences
import com.luisfagundes.library.impl.domain.policy.DeletionCounters
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val ONBOARDING_SEEN_KEY = "swipe_up_trash_onboarding_seen"

internal class LibraryPreferencesTest {

    private val context: Context = mockk()
    private val sharedPreferences: SharedPreferences = mockk()
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)
    private var onboardingSeen = false

    @BeforeEach
    fun setUp() {
        every {
            context.getSharedPreferences("library_prefs", Context.MODE_PRIVATE)
        } returns sharedPreferences
        every { sharedPreferences.getBoolean(ONBOARDING_SEEN_KEY, false) } answers {
            onboardingSeen
        }
        every { sharedPreferences.edit() } returns editor
        every { editor.putBoolean(ONBOARDING_SEEN_KEY, any()) } answers {
            onboardingSeen = secondArg()
            editor
        }
    }

    @Test
    fun `onboarding flag defaults to false and remains true after saving`() {
        // Given
        val preferences = LibraryPreferences(context)

        // Then
        assertFalse(preferences.hasSeenSwipeUpTrashOnboarding())

        // When
        preferences.markSwipeUpTrashOnboardingSeen()

        // Then
        assertTrue(LibraryPreferences(context).hasSeenSwipeUpTrashOnboarding())
    }

    @Test
    fun `deletion counters default to zero and round trip after saving`() {
        // Given
        var deleted = 0
        var windowStart = 0L
        var lockedUntil = 0L
        every { sharedPreferences.getInt("deleted_in_window", 0) } answers { deleted }
        every { sharedPreferences.getLong("deletion_window_start", 0L) } answers { windowStart }
        every { sharedPreferences.getLong("deletion_locked_until", 0L) } answers { lockedUntil }
        every { editor.putInt("deleted_in_window", any()) } answers {
            deleted = secondArg()
            editor
        }
        every { editor.putLong("deletion_window_start", any()) } answers {
            windowStart = secondArg()
            editor
        }
        every { editor.putLong("deletion_locked_until", any()) } answers {
            lockedUntil = secondArg()
            editor
        }
        val preferences = LibraryPreferences(context)

        // Then
        assertEquals(DeletionCounters(), preferences.getDeletionCounters())

        // When
        val counters = DeletionCounters(deleted = 25, windowStartMillis = 10L, lockedUntilMillis = 20L)
        preferences.setDeletionCounters(counters)

        // Then
        assertEquals(counters, preferences.getDeletionCounters())
    }
}
