package com.luisfagundes.library.impl.data.repository

import android.app.PendingIntent
import android.os.Build
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.luisfagundes.core.common.di.DefaultDispatcher
import com.luisfagundes.library.impl.data.datasource.LibraryDataSource
import com.luisfagundes.library.impl.data.datasource.LibraryPreferences
import com.luisfagundes.library.impl.data.mapper.PhotoMapper
import com.luisfagundes.library.impl.domain.model.Photo
import com.luisfagundes.library.impl.domain.model.PhotoSection
import com.luisfagundes.library.impl.domain.repository.LibraryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

internal class LibraryRepositoryImpl @Inject constructor(
    private val dataSource: LibraryDataSource,
    private val photoMapper: PhotoMapper,
    private val preferences: LibraryPreferences,
    @param:ApplicationContext private val context: Context,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : LibraryRepository {

    override suspend fun getPhotosByMonth(): Result<List<PhotoSection>> = withContext(dispatcher) {
        dataSource.fetchPhotoList().map { photos ->
            val trashedIds = preferences.getTrashedPhotoIds()
            val deletedIds = preferences.getDeletedPhotoIds()
            val filteredPhotos = photos.filter { photo ->
                photo.id !in trashedIds && photo.id !in deletedIds
            }
            val photosByMonth = filteredPhotos.groupBy { photo ->
                val instant = Instant.ofEpochSecond(photo.dateAdded)
                YearMonth.from(instant.atZone(ZoneId.systemDefault()))
            }
            photosByMonth.map { (month, photos) ->
                PhotoSection(
                    yearMonth = month,
                    photos = photos
                        .map { photoMapper.mapToDomain(it) }
                        .sortedByDescending { it.dateAdded }
                )
            }.sortedByDescending { it.yearMonth }
        }
    }

    override suspend fun getActivePhotos(): Result<List<Photo>> = withContext(dispatcher) {
        dataSource.fetchPhotoList().map { photos ->
            val trashedIds = preferences.getTrashedPhotoIds()
            val deletedIds = preferences.getDeletedPhotoIds()
            photos.filter { photo ->
                photo.id !in trashedIds && photo.id !in deletedIds
            }.map { photoMapper.mapToDomain(it) }
             .sortedByDescending { it.dateAdded }
        }
    }

    override suspend fun getTrashPhotos(): Result<List<Photo>> = withContext(dispatcher) {
        dataSource.fetchPhotoList().map { photos ->
            val trashedIds = preferences.getTrashedPhotoIds()
            val deletedIds = preferences.getDeletedPhotoIds()
            photos.filter { photo ->
                photo.id in trashedIds && photo.id !in deletedIds
            }.map { photoMapper.mapToDomain(it) }
        }
    }

    override suspend fun getItemsInTrashCount(): Int = withContext(dispatcher) {
        val trashedIds = preferences.getTrashedPhotoIds()
        val deletedIds = preferences.getDeletedPhotoIds()
        trashedIds.subtract(deletedIds).size
    }

    override suspend fun moveToTrash(photoId: Long) = withContext(dispatcher) {
        val trashedIds = preferences.getTrashedPhotoIds().toMutableSet()
        trashedIds.add(photoId)
        preferences.setTrashedPhotoIds(trashedIds)
    }

    override suspend fun restoreFromTrash(photoIds: List<Long>) = withContext(dispatcher) {
        val trashedIds = preferences.getTrashedPhotoIds().toMutableSet()
        trashedIds.removeAll(photoIds.toSet())
        preferences.setTrashedPhotoIds(trashedIds)
    }

    override suspend fun permanentlyDelete(photoIds: List<Long>) = withContext(dispatcher) {
        val trashedIds = preferences.getTrashedPhotoIds().toMutableSet()
        trashedIds.removeAll(photoIds.toSet())
        preferences.setTrashedPhotoIds(trashedIds)

        val deletedIds = preferences.getDeletedPhotoIds().toMutableSet()
        deletedIds.addAll(photoIds)
        preferences.setDeletedPhotoIds(deletedIds)

        photoIds.forEach { id ->
            try {
                val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                context.contentResolver.delete(uri, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun createDeleteRequest(photoIds: List<Long>): PendingIntent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uris = photoIds.map { id ->
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            }
            return MediaStore.createDeleteRequest(context.contentResolver, uris)
        }
        return null
    }
}
