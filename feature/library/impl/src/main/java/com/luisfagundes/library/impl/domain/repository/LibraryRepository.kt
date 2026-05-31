package com.luisfagundes.library.impl.domain.repository

import android.app.PendingIntent
import com.luisfagundes.library.impl.domain.model.Photo
import com.luisfagundes.library.impl.domain.model.PhotoSection

internal interface LibraryRepository {
    suspend fun getPhotosByMonth(): Result<List<PhotoSection>>
    suspend fun getActivePhotos(): Result<List<Photo>>
    suspend fun getTrashPhotos(): Result<List<Photo>>
    suspend fun getItemsInTrashCount(): Int
    suspend fun moveToTrash(photoId: Long)
    suspend fun restoreFromTrash(photoIds: List<Long>)
    suspend fun permanentlyDelete(photoIds: List<Long>)
    fun createDeleteRequest(photoIds: List<Long>): PendingIntent?
}