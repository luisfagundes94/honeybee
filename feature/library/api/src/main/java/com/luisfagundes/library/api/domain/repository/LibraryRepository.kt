package com.luisfagundes.library.api.domain.repository

import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.api.domain.model.MediaDeleteRequest
import com.luisfagundes.library.api.domain.model.Statistics

interface LibraryRepository {
    suspend fun getActiveMedia(): Result<List<Media>>
    suspend fun getTrashMedia(): Result<List<Media>>
    suspend fun getItemsInTrashCount(): Result<Int>
    suspend fun hasSeenSwipeUpTrashOnboarding(): Result<Boolean>
    suspend fun markSwipeUpTrashOnboardingSeen(): Result<Unit>
    suspend fun moveToTrash(mediaId: Long): Result<Unit>
    suspend fun restoreFromTrash(mediaIds: List<Long>): Result<Unit>
    suspend fun permanentlyDelete(mediaList: List<Media>): Result<Unit>
    suspend fun createDeleteRequest(mediaIds: List<Long>): Result<MediaDeleteRequest?>
    suspend fun getStatistics(): Result<Statistics>
}
