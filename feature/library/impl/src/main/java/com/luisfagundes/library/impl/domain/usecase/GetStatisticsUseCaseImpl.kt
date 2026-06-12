package com.luisfagundes.library.impl.domain.usecase

import com.luisfagundes.library.api.domain.model.Statistics
import com.luisfagundes.library.api.domain.usecase.GetStatisticsUseCase
import com.luisfagundes.library.impl.domain.repository.LibraryRepository
import javax.inject.Inject

internal class GetStatisticsUseCaseImpl @Inject constructor(
    private val repository: LibraryRepository
) : GetStatisticsUseCase {
    override suspend operator fun invoke(): Result<Statistics> = repository.getStatistics()
}
