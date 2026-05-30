package com.luisfagundes.onboarding.impl.di

import com.luisfagundes.onboarding.api.domain.usecase.GetOnboardingStatusUseCase
import com.luisfagundes.onboarding.impl.domain.usecase.GetOnboardingStatusUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UseCaseModule {

    @Binds
    @Singleton
    abstract fun bindGetOnboardingStatusUseCase(
        getOnboardingStatusUseCaseImpl: GetOnboardingStatusUseCaseImpl
    ): GetOnboardingStatusUseCase
}
