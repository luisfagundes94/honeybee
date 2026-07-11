package com.luisfagundes.library.api.domain.repository

import android.app.PendingIntent
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.api.domain.model.Statistics

interface LibraryRepository {
    suspend fun getActiveMedia(): Result<List<Media>>
    suspend fun getTrashMedia(): Result<List<Media>>
    suspend fun getItemsInTrashCount(): Int
    suspend fun moveToTrash(mediaId: Long)
    suspend fun restoreFromTrash(mediaIds: List<Long>)
    suspend fun permanentlyDelete(mediaList: List<Media>)
    suspend fun createDeleteRequest(mediaIds: List<Long>): PendingIntent?
    suspend fun getStatistics(): Result<Statistics>
}
