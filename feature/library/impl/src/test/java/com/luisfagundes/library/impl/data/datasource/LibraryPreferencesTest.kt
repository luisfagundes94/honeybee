package com.luisfagundes.library.impl.data.datasource

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
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
}
