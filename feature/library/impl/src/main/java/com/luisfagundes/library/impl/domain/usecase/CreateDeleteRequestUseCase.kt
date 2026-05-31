package com.luisfagundes.library.impl.domain.usecase

import android.app.PendingIntent
import com.luisfagundes.library.impl.domain.repository.LibraryRepository
import javax.inject.Inject

internal class CreateDeleteRequestUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    operator fun invoke(photoIds: List<Long>): PendingIntent? = repository.createDeleteRequest(photoIds)
}
