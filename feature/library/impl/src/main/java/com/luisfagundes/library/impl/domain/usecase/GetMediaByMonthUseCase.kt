package com.luisfagundes.library.impl.domain.usecase

import com.luisfagundes.library.api.domain.model.MediaSection
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

internal class GetMediaByMonthUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend operator fun invoke(): Result<List<MediaSection>> {
        return repository.getActiveMedia().map { activeMedia ->
            val mediaByMonth = activeMedia.groupBy { media ->
                val instant = Instant.ofEpochSecond(media.dateAdded)
                YearMonth.from(instant.atZone(ZoneId.systemDefault()))
            }
            mediaByMonth.map { (month, list) ->
                MediaSection(
                    yearMonth = month,
                    mediaList = list
                )
            }
        }
    }
}
