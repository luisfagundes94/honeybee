package com.luisfagundes.library.impl.domain.repository

import android.app.PendingIntent
import com.luisfagundes.library.impl.domain.model.Media
import com.luisfagundes.library.impl.domain.model.MediaSection

internal interface LibraryRepository {
    suspend fun getMediaByMonth(): Result<List<MediaSection>>
    suspend fun getActiveMedia(): Result<List<Media>>
    suspend fun getTrashMedia(): Result<List<Media>>
    suspend fun getItemsInTrashCount(): Int
    suspend fun moveToTrash(mediaId: Long)
    suspend fun restoreFromTrash(mediaIds: List<Long>)
    suspend fun permanentlyDelete(mediaIds: List<Long>)
    suspend fun createDeleteRequest(mediaIds: List<Long>): PendingIntent?
}