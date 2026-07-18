package com.luisfagundes.library.impl.domain.usecase

import com.luisfagundes.core.common.di.DefaultDispatcher
import com.luisfagundes.library.api.domain.model.MediaSection
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

internal class GetMediaByMonthUseCase @Inject constructor(
    private val repository: LibraryRepository,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(): Result<List<MediaSection>> = withContext(dispatcher) {
        return@withContext repository.getActiveMedia().map { activeMedia ->
            val mediaByMonth = activeMedia.groupBy { media ->
                val instant = Instant.ofEpochSecond(media.dateAdded)
                YearMonth.from(instant.atZone(ZoneId.systemDefault()))
            }
            mediaByMonth.map { (month, list) ->
                MediaSection(
                    yearMonth = month,
                    mediaList = list.sortedByDescending { it.dateAdded }
                )
            }.sortedByDescending { it.yearMonth }
        }
    }
}
