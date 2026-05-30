package com.luisfagundes.impl.data.datasource

import com.luisfagundes.impl.data.model.PhotoDto

internal interface LibraryDataSource {
    suspend fun fetchPhotoList(): Result<List<PhotoDto>>
}