package com.luisfagundes.library.impl.domain.usecase

import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import javax.inject.Inject

internal class GetActiveMediaUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke(): Result<List<Media>> = repository.getActiveMedia()
}
