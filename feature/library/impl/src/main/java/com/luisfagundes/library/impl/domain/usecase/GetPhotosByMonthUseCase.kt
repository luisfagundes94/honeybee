package com.luisfagundes.library.impl.domain.usecase

import com.luisfagundes.library.impl.domain.repository.LibraryRepository
import javax.inject.Inject

internal class GetPhotosByMonthUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke() = repository.getPhotosByMonth()
}