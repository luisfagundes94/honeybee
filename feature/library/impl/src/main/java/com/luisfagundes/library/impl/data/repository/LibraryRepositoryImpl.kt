package com.luisfagundes.library.impl.data.repository

import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.luisfagundes.core.common.di.DefaultDispatcher
import com.luisfagundes.core.common.tools.safeRunCatching
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.api.domain.model.Statistics
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import com.luisfagundes.library.impl.data.database.dao.StatisticsDao
import com.luisfagundes.library.impl.data.datasource.LibraryDataSource
import com.luisfagundes.library.impl.data.datasource.LibraryPreferences
import com.luisfagundes.library.impl.data.mapper.MediaMapper
import com.luisfagundes.library.impl.data.mapper.StatisticsMapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class LibraryRepositoryImpl @Inject constructor(
    private val dataSource: LibraryDataSource,
    private val mediaMapper: MediaMapper,
    private val preferences: LibraryPreferences,
    private val statisticsDao: StatisticsDao,
    private val statisticsMapper: StatisticsMapper,
    @param:ApplicationContext private val context: Context,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : LibraryRepository {

    override suspend fun getActiveMedia(): Result<List<Media>> = withContext(dispatcher) {
        dataSource.fetchMediaList().map { mediaList ->
            val trashedIds = preferences.getTrashedPhotoIds()
            val deletedIds = preferences.getDeletedPhotoIds()
            mediaList.filter { media ->
                media.id !in trashedIds && media.id !in deletedIds
            }.map { mediaMapper.mapToDomain(it) }
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

    override suspend fun permanentlyDelete(mediaList: List<Media>) = withContext(dispatcher) {
        val mediaIds = mediaList.map { it.id }
        updatePreferencesForDeletion(mediaIds)

        val videoIds = mediaList.filter { it.isVideo }.map { it.id }.toSet()

        updateStatisticsForDeletedMedia(mediaList)
        deleteFromMediaStore(mediaIds, videoIds)
    }

    override suspend fun createDeleteRequest(mediaIds: List<Long>): PendingIntent? =
        withContext(dispatcher) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val allMedia = dataSource.fetchMediaList().getOrNull() ?: emptyList()
                val videoIds = allMedia.filter { it.isVideo }.map { it.id }.toSet()
                val uris = mediaIds.map { getMediaUri(it, videoIds) }
                return@withContext MediaStore.createDeleteRequest(context.contentResolver, uris)
            }
            return@withContext null
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
            val updatedStats = statisticsMapper.mapToUpdatedEntity(currentStats, mediaList)
            statisticsDao.insertOrUpdate(updatedStats)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteFromMediaStore(mediaIds: List<Long>, videoIds: Set<Long>) {
        mediaIds.forEach { id ->
            try {
                val uri = getMediaUri(id, videoIds)
                context.contentResolver.delete(uri, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
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

    override suspend fun getStatistics(): Result<Statistics> = withContext(dispatcher) {
        safeRunCatching {
            val entity = statisticsDao.getStatistics()
            statisticsMapper.mapToDomain(entity)
        }
    }
}
