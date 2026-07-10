package com.luisfagundes.library.impl.domain.usecase

import com.luisfagundes.library.api.domain.repository.LibraryRepository
import javax.inject.Inject

internal class GetItemsInTrashCountUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke(): Int = repository.getItemsInTrashCount()
}
