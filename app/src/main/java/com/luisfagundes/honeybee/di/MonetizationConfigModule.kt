package com.luisfagundes.honeybee.di

import com.luisfagundes.core.ads.AdsConfig
import com.luisfagundes.core.common.provider.SubscriptionConfig
import com.luisfagundes.honeybee.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object MonetizationConfigModule {
    @Provides
    @Singleton
    fun provideAdsConfig(): AdsConfig = AdsConfig(
        settingsBannerAdUnitId = BuildConfig.ADMOB_SETTINGS_BANNER_AD_UNIT_ID,
        cleanupInterstitialAdUnitId = BuildConfig.ADMOB_CLEANUP_INTERSTITIAL_AD_UNIT_ID,
    )

    @Provides
    @Singleton
    fun provideSubscriptionConfig(): SubscriptionConfig = SubscriptionConfig(
        productId = BuildConfig.PREMIUM_PRODUCT_ID,
        monthlyBasePlanId = BuildConfig.PREMIUM_MONTHLY_BASE_PLAN_ID,
        annualBasePlanId = BuildConfig.PREMIUM_ANNUAL_BASE_PLAN_ID,
    )
}
