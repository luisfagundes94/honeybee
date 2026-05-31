package com.luisfagundes.library.impl.domain.usecase

import com.luisfagundes.library.impl.domain.repository.LibraryRepository
import javax.inject.Inject

internal class RestoreFromTrashUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke(photoIds: List<Long>) = repository.restoreFromTrash(photoIds)
}
