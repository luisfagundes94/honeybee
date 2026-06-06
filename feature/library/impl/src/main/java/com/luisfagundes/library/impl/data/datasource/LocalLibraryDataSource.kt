package com.luisfagundes.library.impl.data.datasource

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.luisfagundes.core.common.tools.safeRunCatching
import com.luisfagundes.library.impl.data.model.MediaDto
import com.luisfagundes.core.common.di.IoDispatcher
import com.luisfagundes.core.common.provider.SubscriptionProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class LocalLibraryDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val subscriptionProvider: SubscriptionProvider
) : LibraryDataSource {
    private val mediaProjection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.DATE_ADDED,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Files.FileColumns.MIME_TYPE,
        MediaStore.Files.FileColumns.WIDTH,
        MediaStore.Files.FileColumns.HEIGHT,
        MediaStore.Files.FileColumns.MEDIA_TYPE
    )
    private val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

    override suspend fun fetchMediaList(): Result<List<MediaDto>> = withContext(dispatcher) {
        val mediaList = mutableListOf<MediaDto>()

        val isPremium = subscriptionProvider.isPremium()

        val selection = if (isPremium) {
            "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
        } else {
            "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
        }

        val selectionArgs = if (isPremium) {
            arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )
        } else {
            arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString()
            )
        }

        val query = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            mediaProjection,
            selection,
            selectionArgs,
            sortOrder
        )

        safeRunCatching {
            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT)
                val mediaTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val size = cursor.getLong(sizeColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val mediaType = cursor.getInt(mediaTypeColumn)

                    val isVideo = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                    val mediaUri = ContentUris.withAppendedId(
                        if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    mediaList.add(
                        MediaDto(
                            id = id,
                            uri = mediaUri,
                            dateAdded = dateAdded,
                            size = size,
                            mimeType = mimeType,
                            width = width,
                            height = height,
                            isVideo = isVideo
                        )
                    )
                }
            }
            mediaList
        }
    }
}