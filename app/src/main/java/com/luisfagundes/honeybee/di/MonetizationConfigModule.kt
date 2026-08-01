package com.luisfagundes.honeybee.di

import com.luisfagundes.core.ads.AdsConfig
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

}
