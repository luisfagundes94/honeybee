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

@Singleton
internal class AdsCoordinatorImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val subscriptionProvider: SubscriptionProvider,
    private val adsConfig: AdsConfig
) : AdsCoordinator {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)
    private val _state = MutableStateFlow(AdsState())
    private var isConsentRequestInFlight = false
    private var isMobileAdsInitialized = false
    private var isInterstitialLoading = false
    private var isInterstitialShowing = false
    private var interstitialAd: InterstitialAd? = null

    override val state: StateFlow<AdsState> = _state.asStateFlow()

    init {
        scope.launch {
            subscriptionProvider.status.collectLatest { status ->
                if (status == SubscriptionStatus.Premium) {
                    interstitialAd = null
                    _state.value = AdsState()
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
        if (!canShowCleanupInterstitial(deletedCount)) {
            onComplete()
        } else scope.launch {
            val ad = interstitialAd
            if (ad == null) {
                onComplete()
            } else {
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
                    override fun onAdDismissedFullScreenContent() = completeOnce()

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) = completeOnce()
                }
                ad.setImmersiveMode(true)
                ad.show(activity)
            }
        }
    }

    private fun canShowCleanupInterstitial(deletedCount: Int): Boolean {
        var canShow = deletedCount >= MINIMUM_DELETED_ITEMS_FOR_INTERSTITIAL
        if (canShow) canShow = subscriptionProvider.status.value == SubscriptionStatus.Free
        if (canShow) canShow = state.value.canShowAds
        if (canShow) canShow = !isInterstitialShowing
        return canShow
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
        _state.value = AdsState(
            canShowAds = canShowAds && subscriptionProvider.status.value == SubscriptionStatus.Free,
            isPrivacyOptionsRequired = consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED,
        )
    }

    private fun loadInterstitial() {
        if (canLoadInterstitial()) {
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

    private fun canLoadInterstitial(): Boolean {
        var canLoad = subscriptionProvider.status.value == SubscriptionStatus.Free
        if (canLoad) canLoad = state.value.canShowAds
        if (canLoad) canLoad = interstitialAd == null
        if (canLoad) canLoad = !isInterstitialLoading
        return canLoad
    }
}
