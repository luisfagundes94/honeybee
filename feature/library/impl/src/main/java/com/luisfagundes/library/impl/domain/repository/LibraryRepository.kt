package com.luisfagundes.library.impl.domain.repository

import com.luisfagundes.library.impl.domain.model.PhotoSection

internal interface LibraryRepository {
    suspend fun getPhotosByMonth(): Result<List<PhotoSection>>
}