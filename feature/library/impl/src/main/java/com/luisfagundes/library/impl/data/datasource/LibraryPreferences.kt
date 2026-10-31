package com.luisfagundes.library.impl.data.datasource

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import com.luisfagundes.library.impl.domain.policy.DeletionCounters

private const val KEY_DELETED_IN_WINDOW = "deleted_in_window"
private const val KEY_WINDOW_START = "deletion_window_start"
private const val KEY_LOCKED_UNTIL = "deletion_locked_until"
private const val KEY_TRASHED = "trashed_photo_ids"
private const val KEY_DELETED = "deleted_photo_ids"
private const val KEY_SWIPE_UP_TRASH_ONBOARDING_SEEN = "swipe_up_trash_onboarding_seen"

@Singleton
internal class LibraryPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("library_prefs", Context.MODE_PRIVATE)

    fun getTrashedPhotoIds(): Set<Long> {
        return prefs.getStringSet(KEY_TRASHED, emptySet()).orEmpty()
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }

    fun setTrashedPhotoIds(ids: Set<Long>) {
        prefs.edit { putStringSet(KEY_TRASHED, ids.map { it.toString() }.toSet()) }
    }

    fun getDeletedPhotoIds(): Set<Long> {
        return prefs.getStringSet(KEY_DELETED, emptySet()).orEmpty()
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }

    fun setDeletedPhotoIds(ids: Set<Long>) {
        prefs.edit { putStringSet(KEY_DELETED, ids.map { it.toString() }.toSet()) }
    }

    fun hasSeenSwipeUpTrashOnboarding(): Boolean =
        prefs.getBoolean(KEY_SWIPE_UP_TRASH_ONBOARDING_SEEN, false)

    fun markSwipeUpTrashOnboardingSeen() {
        prefs.edit { putBoolean(KEY_SWIPE_UP_TRASH_ONBOARDING_SEEN, true) }
    }

    fun getDeletionCounters(): DeletionCounters = DeletionCounters(
        deleted = prefs.getInt(KEY_DELETED_IN_WINDOW, 0),
        windowStartMillis = prefs.getLong(KEY_WINDOW_START, 0L),
        lockedUntilMillis = prefs.getLong(KEY_LOCKED_UNTIL, 0L)
    )

    fun setDeletionCounters(counters: DeletionCounters) {
        prefs.edit {
            putInt(KEY_DELETED_IN_WINDOW, counters.deleted)
            putLong(KEY_WINDOW_START, counters.windowStartMillis)
            putLong(KEY_LOCKED_UNTIL, counters.lockedUntilMillis)
        }
    }
}
