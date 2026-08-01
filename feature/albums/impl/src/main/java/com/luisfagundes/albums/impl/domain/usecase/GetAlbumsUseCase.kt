package com.luisfagundes.albums.impl.domain.usecase

import com.luisfagundes.library.api.domain.repository.LibraryRepository
import javax.inject.Inject

internal class GetAlbumsUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke() =
        libraryRepository.getActiveMedia().map { activeMedia ->
            activeMedia.toAlbums()
        }
}
