package com.luisfagundes.premium.impl.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.luisfagundes.core.common.provider.SubscriptionConfig
import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.core.common.provider.SubscriptionStatus
import com.luisfagundes.premium.impl.domain.model.SubscriptionOffer
import com.luisfagundes.premium.impl.domain.model.SubscriptionPlan
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PlayBillingSubscriptionProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val config: SubscriptionConfig,
) : SubscriptionProvider, PurchasesUpdatedListener {
    private val mutableStatus = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.Loading)
    private val mutableOffers = MutableStateFlow<List<SubscriptionOffer>>(emptyList())
    private val mutablePurchasePending = MutableStateFlow(false)
    private var productDetails: ProductDetails? = null
    private var isReady = false
    private var isConnecting = false

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enableAutoServiceReconnection()
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build(),
        )
        .build()

    override val status: StateFlow<SubscriptionStatus> = mutableStatus.asStateFlow()
    val offers: StateFlow<List<SubscriptionOffer>> = mutableOffers.asStateFlow()
    val isPurchasePending: StateFlow<Boolean> = mutablePurchasePending.asStateFlow()

    init {
        connect()
    }

    override suspend fun refresh() {
        if (isReady) {
            queryPurchases()
            queryProductDetails()
        } else {
            connect()
        }
    }

    fun launchPurchase(activity: Activity, offerToken: String) {
        val details = productDetails ?: return
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()
        billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build(),
        )
    }

    fun openSubscriptionManagement(activity: Activity) {
        val uri = ("https://play.google.com/store/account/" +
                "subscriptions?package=${context.packageName}&sku=${config.productId}").toUri()
        activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases)
        }
    }

    private fun connect() {
        if (isReady || isConnecting) return
        isConnecting = true
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isConnecting = false
                isReady = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                if (isReady) {
                    queryPurchases()
                    queryProductDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                isReady = false
            }
        })
    }

    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases)
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        mutablePurchasePending.value = purchases.any { purchase ->
            config.productId in purchase.products &&
                purchase.purchaseState == Purchase.PurchaseState.PENDING
        }
        val premiumPurchase = purchases.firstOrNull { purchase ->
            config.productId in purchase.products &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        mutableStatus.value = if (premiumPurchase == null) {
            SubscriptionStatus.Free
        } else {
            acknowledge(premiumPurchase)
            SubscriptionStatus.Premium
        }
    }

    private fun acknowledge(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        billingClient.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build(),
        ) { }
    }

    private fun queryProductDetails() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(config.productId)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build(),
        ) { billingResult, result ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
            productDetails = result.productDetailsList.firstOrNull()
            mutableOffers.value = productDetails?.toOffers().orEmpty()
        }
    }

    private fun ProductDetails.toOffers(): List<SubscriptionOffer> = subscriptionOfferDetails.orEmpty()
        .mapNotNull { offer ->
            val plan = when (offer.basePlanId) {
                config.monthlyBasePlanId -> SubscriptionPlan.MONTHLY
                config.annualBasePlanId -> SubscriptionPlan.ANNUAL
                else -> return@mapNotNull null
            }
            val price = offer.pricingPhases.pricingPhaseList.lastOrNull()?.formattedPrice
                ?: return@mapNotNull null
            SubscriptionOffer(
                id = offer.basePlanId,
                plan = plan,
                formattedPrice = price,
                offerToken = offer.offerToken,
            )
        }
        .distinctBy { it.plan }
        .sortedBy { it.plan.ordinal }
}
