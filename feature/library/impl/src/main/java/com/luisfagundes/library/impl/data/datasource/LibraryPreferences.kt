package com.luisfagundes.library.impl.data.datasource

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

private const val KEY_TRASHED = "trashed_photo_ids"
private const val KEY_DELETED = "deleted_photo_ids"
private const val KEY_SWIPE_UP_TRASH_ONBOARDING_SEEN = "swipe_up_trash_onboarding_seen"

@Singleton
internal class LibraryPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("library_prefs", Context.MODE_PRIVATE)

    fun getTrashedPhotoIds(): Set<Long> {
        return prefs.getStringSet(KEY_TRASHED, emptySet())?.mapNotNull { it.toLongOrNull() }
            ?.toSet() ?: emptySet()
    }

    fun setTrashedPhotoIds(ids: Set<Long>) {
        prefs.edit { putStringSet(KEY_TRASHED, ids.map { it.toString() }.toSet()) }
    }

    fun getDeletedPhotoIds(): Set<Long> {
        return prefs.getStringSet(KEY_DELETED, emptySet())?.mapNotNull { it.toLongOrNull() }
            ?.toSet() ?: emptySet()
    }

    fun setDeletedPhotoIds(ids: Set<Long>) {
        prefs.edit { putStringSet(KEY_DELETED, ids.map { it.toString() }.toSet()) }
    }

    fun hasSeenSwipeUpTrashOnboarding(): Boolean =
        prefs.getBoolean(KEY_SWIPE_UP_TRASH_ONBOARDING_SEEN, false)

    fun markSwipeUpTrashOnboardingSeen() {
        prefs.edit { putBoolean(KEY_SWIPE_UP_TRASH_ONBOARDING_SEEN, true) }
    }

}
