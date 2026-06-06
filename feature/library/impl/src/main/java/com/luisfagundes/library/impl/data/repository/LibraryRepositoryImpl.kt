package com.luisfagundes.library.impl.data.repository

import android.app.PendingIntent
import android.os.Build
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.luisfagundes.core.common.di.DefaultDispatcher
import com.luisfagundes.library.impl.data.datasource.LibraryDataSource
import com.luisfagundes.library.impl.data.datasource.LibraryPreferences
import com.luisfagundes.library.impl.data.mapper.MediaMapper
import com.luisfagundes.library.impl.domain.model.Media
import com.luisfagundes.library.impl.domain.model.MediaSection
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
    private val mediaMapper: MediaMapper,
    private val preferences: LibraryPreferences,
    @param:ApplicationContext private val context: Context,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : LibraryRepository {

    override suspend fun getMediaByMonth(): Result<List<MediaSection>> = withContext(dispatcher) {
        dataSource.fetchMediaList().map { mediaList ->
            val trashedIds = preferences.getTrashedPhotoIds()
            val deletedIds = preferences.getDeletedPhotoIds()
            val filteredMedia = mediaList.filter { media ->
                media.id !in trashedIds && media.id !in deletedIds
            }
            val mediaByMonth = filteredMedia.groupBy { media ->
                val instant = Instant.ofEpochSecond(media.dateAdded)
                YearMonth.from(instant.atZone(ZoneId.systemDefault()))
            }
            mediaByMonth.map { (month, list) ->
                MediaSection(
                    yearMonth = month,
                    mediaList = list
                        .map { mediaMapper.mapToDomain(it) }
                        .sortedByDescending { it.dateAdded }
                )
            }.sortedByDescending { it.yearMonth }
        }
    }

    override suspend fun getActiveMedia(): Result<List<Media>> = withContext(dispatcher) {
        dataSource.fetchMediaList().map { mediaList ->
            val trashedIds = preferences.getTrashedPhotoIds()
            val deletedIds = preferences.getDeletedPhotoIds()
            mediaList.filter { media ->
                media.id !in trashedIds && media.id !in deletedIds
            }.map { mediaMapper.mapToDomain(it) }
             .sortedByDescending { it.dateAdded }
        }
    }

    override suspend fun getTrashMedia(): Result<List<Media>> = withContext(dispatcher) {
        dataSource.fetchMediaList().map { mediaList ->
            val trashedIds = preferences.getTrashedPhotoIds()
            val deletedIds = preferences.getDeletedPhotoIds()
            mediaList.filter { media ->
                media.id in trashedIds && media.id !in deletedIds
            }.map { mediaMapper.mapToDomain(it) }
        }
    }

    override suspend fun getItemsInTrashCount(): Int = withContext(dispatcher) {
        val trashedIds = preferences.getTrashedPhotoIds()
        val deletedIds = preferences.getDeletedPhotoIds()
        trashedIds.subtract(deletedIds).size
    }

    override suspend fun moveToTrash(mediaId: Long) = withContext(dispatcher) {
        val trashedIds = preferences.getTrashedPhotoIds().toMutableSet()
        trashedIds.add(mediaId)
        preferences.setTrashedPhotoIds(trashedIds)
    }

    override suspend fun restoreFromTrash(mediaIds: List<Long>) = withContext(dispatcher) {
        val trashedIds = preferences.getTrashedPhotoIds().toMutableSet()
        trashedIds.removeAll(mediaIds.toSet())
        preferences.setTrashedPhotoIds(trashedIds)
    }

    override suspend fun permanentlyDelete(mediaIds: List<Long>) = withContext(dispatcher) {
        val trashedIds = preferences.getTrashedPhotoIds().toMutableSet()
        trashedIds.removeAll(mediaIds.toSet())
        preferences.setTrashedPhotoIds(trashedIds)

        val deletedIds = preferences.getDeletedPhotoIds().toMutableSet()
        deletedIds.addAll(mediaIds)
        preferences.setDeletedPhotoIds(deletedIds)

        // Query the dataSource to find which IDs correspond to videos
        val allMedia = dataSource.fetchMediaList().getOrNull() ?: emptyList()
        val videoIds = allMedia.filter { it.isVideo }.map { it.id }.toSet()

        mediaIds.forEach { id ->
            try {
                val isVideo = id in videoIds
                val uri = ContentUris.withAppendedId(
                    if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                context.contentResolver.delete(uri, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun createDeleteRequest(mediaIds: List<Long>): PendingIntent? = withContext(dispatcher) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val allMedia = dataSource.fetchMediaList().getOrNull() ?: emptyList()
            val videoIds = allMedia.filter { it.isVideo }.map { it.id }.toSet()

            val uris = mediaIds.map { id ->
                val isVideo = id in videoIds
                ContentUris.withAppendedId(
                    if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
            }
            return@withContext MediaStore.createDeleteRequest(context.contentResolver, uris)
        }
        return@withContext null
    }
}
