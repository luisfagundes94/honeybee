package com.luisfagundes.library.impl.data.repository

import android.database.sqlite.SQLiteException
import android.os.Build
import android.util.Log
import com.luisfagundes.core.common.di.DefaultDispatcher
import com.luisfagundes.core.common.tools.safeRunCatching
import com.luisfagundes.library.api.domain.model.DeletionQuota
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.api.domain.model.MediaDeleteRequest
import com.luisfagundes.library.api.domain.model.Statistics
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import com.luisfagundes.library.impl.data.database.dao.StatisticsDao
import com.luisfagundes.library.impl.data.datasource.LibraryDataSource
import com.luisfagundes.library.impl.data.datasource.LibraryPreferences
import com.luisfagundes.library.impl.data.database.entity.StatisticsEntity
import com.luisfagundes.library.impl.data.model.MediaDto
import com.luisfagundes.library.impl.domain.policy.DeletionQuotaPolicy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Clock
import javax.inject.Inject

private const val TAG = "LibraryRepository"

@Suppress("TooManyFunctions")
internal class LibraryRepositoryImpl @Inject constructor(
    private val dataSource: LibraryDataSource,
    private val preferences: LibraryPreferences,
    private val statisticsDao: StatisticsDao,
    private val clock: Clock,
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

    override suspend fun getItemsInTrashCount(): Result<Int> = withContext(dispatcher) {
        safeRunCatching {
            val trashedIds = preferences.getTrashedPhotoIds()
            val deletedIds = preferences.getDeletedPhotoIds()
            trashedIds.subtract(deletedIds).size
        }
    }

    override suspend fun hasSeenSwipeUpTrashOnboarding(): Result<Boolean> = withContext(dispatcher) {
        safeRunCatching { preferences.hasSeenSwipeUpTrashOnboarding() }
    }

    override suspend fun markSwipeUpTrashOnboardingSeen(): Result<Unit> = withContext(dispatcher) {
        safeRunCatching { preferences.markSwipeUpTrashOnboardingSeen() }
    }

    override suspend fun moveToTrash(mediaId: Long): Result<Unit> = withContext(dispatcher) {
        safeRunCatching {
            val trashedIds = preferences.getTrashedPhotoIds().toMutableSet()
            trashedIds.add(mediaId)
            preferences.setTrashedPhotoIds(trashedIds)
        }
    }

    override suspend fun restoreFromTrash(mediaIds: List<Long>): Result<Unit> =
        withContext(dispatcher) {
            safeRunCatching {
                val trashedIds = preferences.getTrashedPhotoIds().toMutableSet()
                trashedIds.removeAll(mediaIds.toSet())
                preferences.setTrashedPhotoIds(trashedIds)
            }
        }

    override suspend fun permanentlyDelete(mediaList: List<Media>): Result<Unit> =
        withContext(dispatcher) {
            safeRunCatching {
                val mediaIds = mediaList.map { it.id }
                updatePreferencesForDeletion(mediaIds)

                val videoIds = mediaList.filter { it.isVideo }.map { it.id }.toSet()

                updateStatisticsForDeletedMedia(mediaList)
                dataSource.deleteMedia(mediaIds, videoIds)
                recordDeletionQuota(mediaList.size)
            }
        }

    override suspend fun getDeletionQuota(): Result<DeletionQuota> = withContext(dispatcher) {
        safeRunCatching {
            DeletionQuotaPolicy.quotaAt(preferences.getDeletionCounters(), clock.millis())
        }
    }

    private fun recordDeletionQuota(count: Int) {
        val updated = DeletionQuotaPolicy.record(
            counters = preferences.getDeletionCounters(),
            count = count,
            nowMillis = clock.millis()
        )
        preferences.setDeletionCounters(updated)
    }

    override suspend fun createDeleteRequest(mediaIds: List<Long>): Result<MediaDeleteRequest?> =
        withContext(dispatcher) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                return@withContext Result.success(null)
            }

            dataSource.fetchMediaList().fold(
                onSuccess = { allMedia ->
                    val videoIds = allMedia.filter { it.isVideo }.map { it.id }.toSet()
                    Result.success(MediaDeleteRequest(dataSource.mediaUris(mediaIds, videoIds)))
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

    override suspend fun getStatistics(): Result<Statistics> = withContext(dispatcher) {
        safeRunCatching {
            val entity = statisticsDao.getStatistics()
            entity.toDomain()
        }
    }
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
