package com.luisfagundes.library.api.domain.usecase

import com.luisfagundes.library.api.domain.model.Statistics

interface GetStatisticsUseCase {
    suspend operator fun invoke(): Result<Statistics>
}
