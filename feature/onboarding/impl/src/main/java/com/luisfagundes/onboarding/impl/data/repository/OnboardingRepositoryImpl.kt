package com.luisfagundes.onboarding.impl.data.repository

import com.luisfagundes.core.common.di.IoDispatcher
import com.luisfagundes.onboarding.impl.data.datasource.OnboardingDataSource
import com.luisfagundes.onboarding.impl.domain.repository.OnboardingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class OnboardingRepositoryImpl @Inject constructor(
    private val dataSource: OnboardingDataSource,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher
) : OnboardingRepository {

    override fun getOnboardingStatus(): Flow<Boolean> = dataSource.isOnboardingCompleted()
        .flowOn(dispatcher)

    override suspend fun completeOnboarding() {
        withContext(dispatcher) {
            dataSource.setOnboardingCompleted()
        }
    }
}
