package com.luisfagundes.albums.impl.domain.usecase

import com.luisfagundes.library.api.domain.model.filterBy
import com.luisfagundes.library.api.domain.model.toMediaFilter
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import javax.inject.Inject

internal class GetAlbumMediaUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(albumId: String) =
        libraryRepository.getActiveMedia().map { activeMedia ->
            activeMedia.filterBy(albumId.toMediaFilter())
        }
}
