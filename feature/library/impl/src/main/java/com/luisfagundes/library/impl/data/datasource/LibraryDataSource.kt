package com.luisfagundes.library.impl.data.datasource

import com.luisfagundes.library.impl.data.model.MediaDto

internal interface LibraryDataSource {
    suspend fun fetchMediaList(): Result<List<MediaDto>>
    fun mediaUris(mediaIds: List<Long>, videoIds: Set<Long>): List<String>
    suspend fun deleteMedia(mediaIds: List<Long>, videoIds: Set<Long>)
}
