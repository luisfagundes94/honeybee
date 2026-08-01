package com.luisfagundes.library.impl.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.luisfagundes.core.common.di.DefaultDispatcher
import com.luisfagundes.core.common.tools.safeRunCatching
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.api.domain.model.MediaDeleteRequest
import com.luisfagundes.library.api.domain.model.Statistics
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import com.luisfagundes.library.impl.data.database.dao.StatisticsDao
import com.luisfagundes.library.impl.data.datasource.LibraryDataSource
import com.luisfagundes.library.impl.data.datasource.LibraryPreferences
import com.luisfagundes.library.impl.data.database.entity.StatisticsEntity
import com.luisfagundes.library.impl.data.model.MediaDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "LibraryRepository"

internal class LibraryRepositoryImpl @Inject constructor(
    private val dataSource: LibraryDataSource,
    private val preferences: LibraryPreferences,
    private val statisticsDao: StatisticsDao,
    @param:ApplicationContext private val context: Context,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : LibraryRepository {

    override suspend fun getActiveMedia(): Result<List<Media>> = withContext(dispatcher) {
        dataSource.fetchMediaList().map { mediaList ->
            val trashedIds = preferences.getTrashedPhotoIds()
            val deletedIds = preferences.getDeletedPhotoIds()
            mediaList.filter { media ->
                media.id !in trashedIds && media.id !in deletedIds
            }.map { it.toDomain() }
        }
    }

    override suspend fun getTrashMedia(): Result<List<Media>> = withContext(dispatcher) {
        dataSource.fetchMediaList().map { mediaList ->
            val trashedIds = preferences.getTrashedPhotoIds()
            val deletedIds = preferences.getDeletedPhotoIds()
            mediaList.filter { media ->
                media.id in trashedIds && media.id !in deletedIds
            }.map { it.toDomain() }
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

    override suspend fun permanentlyDelete(mediaList: List<Media>) = withContext(dispatcher) {
        val mediaIds = mediaList.map { it.id }
        updatePreferencesForDeletion(mediaIds)

        val videoIds = mediaList.filter { it.isVideo }.map { it.id }.toSet()

        updateStatisticsForDeletedMedia(mediaList)
        deleteFromMediaStore(mediaIds, videoIds)
    }

    override suspend fun createDeleteRequest(mediaIds: List<Long>): Result<MediaDeleteRequest?> =
        withContext(dispatcher) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                return@withContext Result.success(null)
            }

            dataSource.fetchMediaList().fold(
                onSuccess = { allMedia ->
                    val videoIds = allMedia.filter { it.isVideo }.map { it.id }.toSet()
                    val mediaUris = mediaIds.map { getMediaUri(it, videoIds).toString() }
                    Result.success(MediaDeleteRequest(mediaUris))
                },
                onFailure = { Result.failure(it) }
            )
        }

    private fun updatePreferencesForDeletion(mediaIds: List<Long>) {
        val trashedIds = preferences.getTrashedPhotoIds().toMutableSet()
        trashedIds.removeAll(mediaIds.toSet())
        preferences.setTrashedPhotoIds(trashedIds)

        val deletedIds = preferences.getDeletedPhotoIds().toMutableSet()
        deletedIds.addAll(mediaIds)
        preferences.setDeletedPhotoIds(deletedIds)
    }

    private fun updateStatisticsForDeletedMedia(mediaList: List<Media>) {
        try {
            if (mediaList.isEmpty()) return
            val currentStats = statisticsDao.getStatistics()
            val updatedStats = currentStats.toUpdatedEntity(mediaList)
            statisticsDao.insertOrUpdate(updatedStats)
        } catch (e: SQLiteException) {
            Log.e(TAG, "Failed to update library statistics", e)
        }
    }

    private fun deleteFromMediaStore(mediaIds: List<Long>, videoIds: Set<Long>) {
        mediaIds.forEach { id ->
            try {
                val uri = getMediaUri(id, videoIds)
                context.contentResolver.delete(uri, null, null)
            } catch (e: SecurityException) {
                Log.e(TAG, "Unable to delete media id=$id due to missing permission", e)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Unable to delete media id=$id due to an invalid URI", e)
            }
        }
    }

    override suspend fun getStatistics(): Result<Statistics> = withContext(dispatcher) {
        safeRunCatching {
            val entity = statisticsDao.getStatistics()
            entity.toDomain()
        }
    }
}

private fun getMediaUri(id: Long, videoIds: Set<Long>): Uri {
    val isVideo = id in videoIds
    return ContentUris.withAppendedId(
        if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        id
    )
}

private fun MediaDto.toDomain() = Media(
    id = id,
    uri = uri.toString(),
    dateAdded = dateAdded,
    size = size,
    mimeType = mimeType,
    width = width,
    height = height,
    durationMillis = durationMillis,
    isVideo = isVideo,
    bucketId = bucketId,
    bucketDisplayName = bucketDisplayName,
    isFavorite = isFavorite
)

private fun StatisticsEntity?.toDomain() = Statistics(
    memoryCleared = this?.memoryCleared ?: 0L,
    mediaDeleted = this?.mediaDeleted ?: 0,
    photosDeleted = this?.photosDeleted ?: 0,
    videosDeleted = this?.videosDeleted ?: 0
)

private fun StatisticsEntity?.toUpdatedEntity(deletedMedia: List<Media>): StatisticsEntity {
    val current = this ?: StatisticsEntity(
        memoryCleared = 0L,
        mediaDeleted = 0,
        photosDeleted = 0,
        videosDeleted = 0
    )
    return StatisticsEntity(
        id = current.id,
        memoryCleared = current.memoryCleared + deletedMedia.sumOf { it.size },
        mediaDeleted = current.mediaDeleted + deletedMedia.size,
        photosDeleted = current.photosDeleted + deletedMedia.count { !it.isVideo },
        videosDeleted = current.videosDeleted + deletedMedia.count { it.isVideo }
    )
}
