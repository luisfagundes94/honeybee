package com.luisfagundes.library.impl.domain.usecase

import com.luisfagundes.library.impl.domain.model.Photo
import com.luisfagundes.library.impl.domain.repository.LibraryRepository
import javax.inject.Inject

internal class GetActivePhotosUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke(): Result<List<Photo>> = repository.getActivePhotos()
}
