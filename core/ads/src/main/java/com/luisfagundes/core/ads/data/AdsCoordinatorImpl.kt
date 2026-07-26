package com.luisfagundes.core.ads.data

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.luisfagundes.core.ads.AdsConfig
import com.luisfagundes.core.ads.AdsCoordinator
import com.luisfagundes.core.ads.AdsState
import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.core.common.provider.SubscriptionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val MINIMUM_DELETED_ITEMS_FOR_INTERSTITIAL = 5
private const val INTERSTITIAL_COOLDOWN_MILLIS = 15 * 60 * 1_000L

@Singleton
internal class AdsCoordinatorImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val subscriptionProvider: SubscriptionProvider,
    private val adsConfig: AdsConfig,
    private val adsPreferences: AdsPreferences,
    private val clock: AdsClock,
) : AdsCoordinator {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)
    private val mutableState = MutableStateFlow(AdsState())
    private var isConsentRequestInFlight = false
    private var isMobileAdsInitialized = false
    private var isInterstitialLoading = false
    private var isInterstitialShowing = false
    private var interstitialAd: InterstitialAd? = null

    override val state: StateFlow<AdsState> = mutableState.asStateFlow()

    init {
        scope.launch {
            subscriptionProvider.status.collectLatest { status ->
                if (status == SubscriptionStatus.Premium) {
                    interstitialAd = null
                    mutableState.value = AdsState()
                } else if (status == SubscriptionStatus.Free && consentInformation.canRequestAds()) {
                    enableAds()
                }
            }
        }
    }

    override fun gatherConsent(activity: Activity) {
        if (subscriptionProvider.status.value != SubscriptionStatus.Free || isConsentRequestInFlight) return

        isConsentRequestInFlight = true
        consentInformation.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                if (consentInformation.canRequestAds()) enableAds()
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    isConsentRequestInFlight = false
                    if (consentInformation.canRequestAds()) enableAds() else updatePrivacyState()
                }
            },
            {
                isConsentRequestInFlight = false
                if (consentInformation.canRequestAds()) enableAds() else updatePrivacyState()
            },
        )
    }

    override fun showPrivacyOptions(activity: Activity) {
        if (!state.value.isPrivacyOptionsRequired) return
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {
            if (subscriptionProvider.status.value == SubscriptionStatus.Free && consentInformation.canRequestAds()) {
                enableAds()
            } else {
                updatePrivacyState()
            }
        }
    }

    override fun showCleanupInterstitial(
        activity: Activity,
        deletedCount: Int,
        onComplete: () -> Unit,
    ) {
        if (
            deletedCount < MINIMUM_DELETED_ITEMS_FOR_INTERSTITIAL ||
            subscriptionProvider.status.value != SubscriptionStatus.Free ||
            !state.value.canShowAds ||
            isInterstitialShowing
        ) {
            onComplete()
            return
        }

        scope.launch {
            val elapsed = clock.nowMillis() - adsPreferences.lastInterstitialShownAt()
            val ad = interstitialAd
            if (elapsed < INTERSTITIAL_COOLDOWN_MILLIS || ad == null) {
                onComplete()
                return@launch
            }

            isInterstitialShowing = true
            interstitialAd = null
            var completed = false
            fun completeOnce() {
                if (!completed) {
                    completed = true
                    isInterstitialShowing = false
                    loadInterstitial()
                    onComplete()
                }
            }

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    scope.launch { adsPreferences.setLastInterstitialShownAt(clock.nowMillis()) }
                }

                override fun onAdDismissedFullScreenContent() = completeOnce()

                override fun onAdFailedToShowFullScreenContent(adError: AdError) = completeOnce()
            }
            ad.setImmersiveMode(true)
            ad.show(activity)
        }
    }

    private fun enableAds() {
        if (subscriptionProvider.status.value != SubscriptionStatus.Free) return
        updatePrivacyState(canShowAds = true)
        if (!isMobileAdsInitialized) {
            isMobileAdsInitialized = true
            MobileAds.initialize(context) { loadInterstitial() }
        } else {
            loadInterstitial()
        }
    }

    private fun updatePrivacyState(canShowAds: Boolean = false) {
        mutableState.value = AdsState(
            canShowAds = canShowAds && subscriptionProvider.status.value == SubscriptionStatus.Free,
            isPrivacyOptionsRequired = consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED,
        )
    }

    private fun loadInterstitial() {
        if (
            subscriptionProvider.status.value != SubscriptionStatus.Free ||
            !state.value.canShowAds ||
            interstitialAd != null ||
            isInterstitialLoading
        ) return

        isInterstitialLoading = true
        InterstitialAd.load(
            context,
            adsConfig.cleanupInterstitialAdUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isInterstitialLoading = false
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isInterstitialLoading = false
                }
            },
        )
    }
}
