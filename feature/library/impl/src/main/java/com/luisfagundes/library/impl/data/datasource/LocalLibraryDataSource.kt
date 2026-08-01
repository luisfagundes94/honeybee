package com.luisfagundes.library.impl.data.datasource

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import com.luisfagundes.core.common.tools.safeRunCatching
import com.luisfagundes.library.impl.data.model.MediaDto
import com.luisfagundes.core.common.di.IoDispatcher
import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.core.common.provider.SubscriptionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class LocalLibraryDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val subscriptionProvider: SubscriptionProvider
) : LibraryDataSource {
    private val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC, " +
        "${MediaStore.Files.FileColumns._ID} DESC"

    override suspend fun fetchMediaList(): Result<List<MediaDto>> = withContext(dispatcher) {
        safeRunCatching { queryMedia() }
    }

    private fun queryMedia(): List<MediaDto> {
        val isPremium = subscriptionProvider.status.value == SubscriptionStatus.Premium
        val query = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            createProjection(),
            createSelection(isPremium),
            createSelectionArgs(isPremium),
            sortOrder
        )
        return query?.use(Cursor::toMediaList).orEmpty()
    }

    private fun createSelection(isPremium: Boolean): String = if (isPremium) {
        "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR " +
            "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
    } else {
        "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
    }

    private fun createSelectionArgs(isPremium: Boolean): Array<String> = if (isPremium) {
        arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )
    } else {
        arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
    }

    private fun createProjection(): Array<String> = buildList {
        add(MediaStore.Files.FileColumns._ID)
        add(MediaStore.Files.FileColumns.DATE_ADDED)
        add(MediaStore.Files.FileColumns.SIZE)
        add(MediaStore.Files.FileColumns.MIME_TYPE)
        add(MediaStore.Files.FileColumns.WIDTH)
        add(MediaStore.Files.FileColumns.HEIGHT)
        add(MediaStore.Video.VideoColumns.DURATION)
        add(MediaStore.Files.FileColumns.MEDIA_TYPE)
        add(BUCKET_DISPLAY_NAME_COLUMN)
        add(BUCKET_ID_COLUMN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            add(MediaStore.MediaColumns.IS_FAVORITE)
        }
    }.toTypedArray()
}

private const val BUCKET_DISPLAY_NAME_COLUMN = "bucket_display_name"
private const val BUCKET_ID_COLUMN = "bucket_id"

private class MediaColumns {
    var id = 0
    var dateAdded = 0
    var size = 0
    var mimeType = 0
    var width = 0
    var height = 0
    var duration = 0
    var mediaType = 0
    var bucketName = 0
    var bucketId = 0
    var favorite = -1
}

private fun Cursor.toMediaList(): List<MediaDto> {
    val columns = MediaColumns().apply {
        id = getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
        dateAdded = getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
        size = getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
        mimeType = getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
        width = getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH)
        height = getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT)
        duration = getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)
        mediaType = getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
        bucketName = getColumnIndexOrThrow(BUCKET_DISPLAY_NAME_COLUMN)
        bucketId = getColumnIndexOrThrow(BUCKET_ID_COLUMN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            favorite = getColumnIndexOrThrow(MediaStore.MediaColumns.IS_FAVORITE)
        }
    }
    val mediaList = mutableListOf<MediaDto>()
    while (moveToNext()) {
        mediaList += readMedia(columns)
    }
    return mediaList
}

private fun Cursor.readMedia(columns: MediaColumns): MediaDto {
    val id = getLong(columns.id)
    val mediaType = getInt(columns.mediaType)
    val isVideo = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
    val mediaUri = ContentUris.withAppendedId(
        if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        id
    )
    return MediaDto(
        id = id,
        uri = mediaUri,
        dateAdded = getLong(columns.dateAdded),
        size = getLong(columns.size),
        mimeType = getString(columns.mimeType),
        width = getInt(columns.width),
        height = getInt(columns.height),
        durationMillis = getLong(columns.duration),
        isVideo = isVideo,
        bucketId = getString(columns.bucketId),
        bucketDisplayName = getString(columns.bucketName),
        isFavorite = columns.favorite >= 0 && getInt(columns.favorite) == 1
    )
}
