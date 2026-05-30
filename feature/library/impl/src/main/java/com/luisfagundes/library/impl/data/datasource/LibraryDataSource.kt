package com.luisfagundes.library.impl.data.datasource

import com.luisfagundes.library.impl.data.model.PhotoDto

internal interface LibraryDataSource {
    suspend fun fetchPhotoList(): Result<List<PhotoDto>>
}