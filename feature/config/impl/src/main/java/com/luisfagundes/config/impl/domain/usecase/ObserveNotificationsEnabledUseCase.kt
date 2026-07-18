package com.luisfagundes.config.impl.domain.usecase

import com.luisfagundes.config.impl.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class ObserveNotificationsEnabledUseCase @Inject constructor(
    private val repository: ConfigRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.notificationsEnabled()
}
