package com.luisfagundes.library.impl.domain.usecase

import android.app.PendingIntent
import com.luisfagundes.library.impl.domain.repository.LibraryRepository
import javax.inject.Inject

internal class CreateDeleteRequestUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke(mediaIds: List<Long>): PendingIntent? = repository.createDeleteRequest(mediaIds)
}
