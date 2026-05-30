package com.luisfagundes.library.impl.domain.repository

import com.luisfagundes.impl.domain.model.PhotoSection

internal interface LibraryRepository {
    suspend fun getPhotosByMonth(): Result<List<PhotoSection>>
}