package com.luisfagundes.library.impl.data.datasource

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.luisfagundes.core.common.tools.safeRunCatching
import com.luisfagundes.library.impl.data.model.PhotoDto
import com.luisfagundes.core.common.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class LocalLibraryDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher
) : LibraryDataSource {
    private val photoProjection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATE_ADDED
    )
    private val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

    override suspend fun fetchPhotoList(): Result<List<PhotoDto>> = withContext(dispatcher) {
        val photoList = mutableListOf<PhotoDto>()

        val query = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            photoProjection,
            null,
            null,
            sortOrder
        )

        safeRunCatching {
            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val photoUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    photoList.add(
                        PhotoDto(
                            id = id,
                            uri = photoUri,
                            dateAdded = dateAdded
                        )
                    )
                }
            }
            photoList
        }
    }
}