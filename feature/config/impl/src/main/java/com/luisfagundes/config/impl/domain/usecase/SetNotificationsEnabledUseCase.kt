package com.luisfagundes.config.impl.domain.usecase

import com.luisfagundes.config.impl.domain.repository.ConfigRepository
import javax.inject.Inject

internal class SetNotificationsEnabledUseCase @Inject constructor(
    private val repository: ConfigRepository
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> =
        repository.setNotificationsEnabled(enabled)
}
