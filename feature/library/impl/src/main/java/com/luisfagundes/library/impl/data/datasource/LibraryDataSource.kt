package com.luisfagundes.library.impl.data.datasource

import com.luisfagundes.library.impl.data.model.MediaDto

internal interface LibraryDataSource {
    suspend fun fetchMediaList(): Result<List<MediaDto>>
}