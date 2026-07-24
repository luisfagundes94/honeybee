package com.luisfagundes.core.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

data class AdsConfig(
    val settingsBannerAdUnitId: String,
    val cleanupInterstitialAdUnitId: String,
)

data class AdsState(
    val canShowAds: Boolean = false,
    val isPrivacyOptionsRequired: Boolean = false,
)

interface AdsCoordinator {
    val state: StateFlow<AdsState>

    fun gatherConsent(activity: Activity)

    fun showPrivacyOptions(activity: Activity)

    fun showCleanupInterstitial(
        activity: Activity,
        deletedCount: Int,
        onComplete: () -> Unit,
    )
}
