package com.luisfagundes.library.impl.data.mapper

import com.luisfagundes.impl.data.model.PhotoDto
import com.luisfagundes.impl.domain.model.Photo

import javax.inject.Inject

internal class PhotoMapper @Inject constructor() {
    fun mapToDomain(source: PhotoDto): Photo {
        return Photo(
            id = source.id,
            uri = source.uri,
            dateAdded = source.dateAdded
        )
    }
}