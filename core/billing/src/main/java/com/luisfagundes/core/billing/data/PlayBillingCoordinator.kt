package com.luisfagundes.core.billing.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.luisfagundes.core.billing.PlayBillingConfig
import com.luisfagundes.core.billing.PremiumBillingCoordinator
import com.luisfagundes.core.billing.PremiumBillingUiState
import com.luisfagundes.core.billing.PremiumPlan
import com.luisfagundes.core.billing.PremiumPurchaseEvent
import com.luisfagundes.core.common.provider.SubscriptionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private const val TAG = "PlayBilling"

@Singleton
internal class PlayBillingCoordinator @Inject constructor(
    @ApplicationContext context: Context,
    private val config: PlayBillingConfig,
) : PremiumBillingCoordinator {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _status = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.Loading)
    private val _uiState = MutableStateFlow(PremiumBillingUiState())
    private val _purchaseEvents = MutableSharedFlow<PremiumPurchaseEvent>(extraBufferCapacity = 1)
    private var monthlyProduct: PurchasableProduct? = null
    private var lifetimeProduct: PurchasableProduct? = null

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(PurchasesUpdatedListener(::onPurchasesUpdated))
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()
    private val billingQueries = PlayBillingQueries(billingClient)

    override val status = _status.asStateFlow()
    override val uiState = _uiState.asStateFlow()
    override val purchaseEvents = _purchaseEvents.asSharedFlow()

    init {
        scope.launch { refresh() }
    }

    override suspend fun refresh() {
        if (!billingQueries.ensureConnected()) {
            settleInitialStatus()
            return
        }

        val subscriptions = billingQueries.queryPurchases(BillingClient.ProductType.SUBS)
        val oneTimeProducts = billingQueries.queryPurchases(BillingClient.ProductType.INAPP)
        if (subscriptions.billingResult.responseCode != BillingClient.BillingResponseCode.OK ||
            oneTimeProducts.billingResult.responseCode != BillingClient.BillingResponseCode.OK
        ) {
            Log.w(TAG, "Purchase refresh failed: ${subscriptions.billingResult.debugMessage}")
            settleInitialStatus()
            return
        }

        val purchases = subscriptions.purchases + oneTimeProducts.purchases
        val ownedPremiumPurchases = purchases.filter(::isPremiumPurchase)
        ownedPremiumPurchases.forEach(::acknowledgeIfNeeded)
        _status.value = if (ownedPremiumPurchases.isEmpty()) {
            SubscriptionStatus.Free
        } else {
            SubscriptionStatus.Premium
        }
    }

    override fun refreshOffers() {
        scope.launch { loadOffers() }
    }

    override fun purchase(activity: Activity, plan: PremiumPlan) {
        val product = when (plan) {
            PremiumPlan.Monthly -> monthlyProduct
            PremiumPlan.Lifetime -> lifetimeProduct
        }
        if (product == null || !billingClient.isReady) {
            scope.launch { _purchaseEvents.emit(PremiumPurchaseEvent.Unavailable) }
            return
        }

        _uiState.value = _uiState.value.copy(isPurchasing = true)
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product.details)
            .apply { product.offerToken?.let(::setOfferToken) }
            .build()
        val result = billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParams))
                .build()
        )
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _uiState.value = _uiState.value.copy(isPurchasing = false)
            Log.w(TAG, "Purchase flow could not start: ${result.debugMessage}")
            if (result.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                scope.launch { refresh() }
            } else {
                scope.launch { _purchaseEvents.emit(PremiumPurchaseEvent.Failed) }
            }
        }
    }

    private suspend fun loadOffers() {
        _uiState.value = _uiState.value.copy(isLoadingOffers = true, offersUnavailable = false)
        if (!billingQueries.ensureConnected()) {
            _uiState.value = _uiState.value.copy(isLoadingOffers = false, offersUnavailable = true)
            return
        }

        val subscriptions = billingQueries.queryProductDetails(
            config.subscriptionProductId,
            BillingClient.ProductType.SUBS,
        )
        val oneTimeProducts = billingQueries.queryProductDetails(
            config.lifetimeProductId,
            BillingClient.ProductType.INAPP,
        )
        val details = listOf(subscriptions, oneTimeProducts).flatMap { result ->
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                result.productDetails
            } else {
                Log.w(TAG, "Product query failed: ${result.billingResult.debugMessage}")
                emptyList()
            }
        }
        val monthlyDetails = details.firstOrNull {
            it.productId == config.subscriptionProductId &&
                it.productType == BillingClient.ProductType.SUBS
        }
        val monthlyOffer = monthlyDetails?.subscriptionOfferDetails
            ?.firstOrNull { it.basePlanId == config.monthlyBasePlanId && it.offerId == null }
            ?: monthlyDetails?.subscriptionOfferDetails
                ?.firstOrNull { it.basePlanId == config.monthlyBasePlanId }
        val monthlyPricePhase = monthlyOffer?.pricingPhases?.pricingPhaseList
            ?.lastOrNull { it.billingPeriod == "P1M" }
        monthlyProduct = if (monthlyDetails != null && monthlyOffer != null && monthlyPricePhase != null) {
            PurchasableProduct(monthlyDetails, monthlyOffer.offerToken)
        } else {
            null
        }

        val lifetimeDetails = details.firstOrNull {
            it.productId == config.lifetimeProductId &&
                it.productType == BillingClient.ProductType.INAPP
        }
        val lifetimeOffer = lifetimeDetails?.oneTimePurchaseOfferDetailsList?.firstOrNull()
        lifetimeProduct = if (lifetimeDetails != null && lifetimeOffer != null) {
            PurchasableProduct(lifetimeDetails, lifetimeOffer.offerToken)
        } else {
            null
        }

        _uiState.value = PremiumBillingUiState(
            isLoadingOffers = false,
            monthlyPrice = monthlyPricePhase?.formattedPrice,
            lifetimePrice = lifetimeOffer?.formattedPrice,
            offersUnavailable = monthlyProduct == null && lifetimeProduct == null,
        )
    }

    private fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: List<Purchase>?,
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases.isNullOrEmpty()) {
                    updatePurchaseState(false)
                    scope.launch { _purchaseEvents.emit(PremiumPurchaseEvent.Failed) }
                } else {
                    scope.launch {
                        val premiumPurchases = purchases.filter(::isPremiumProductPurchase)
                        premiumPurchases.forEach(::grantAndAcknowledge)
                        if (premiumPurchases.any { it.purchaseState == Purchase.PurchaseState.PENDING }) {
                            _purchaseEvents.emit(PremiumPurchaseEvent.Pending)
                        } else if (premiumPurchases.isNotEmpty()) {
                            _purchaseEvents.emit(PremiumPurchaseEvent.Purchased)
                        } else {
                            _purchaseEvents.emit(PremiumPurchaseEvent.Failed)
                        }
                        updatePurchaseState(false)
                        refresh()
                    }
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                updatePurchaseState(false)
                scope.launch { _purchaseEvents.emit(PremiumPurchaseEvent.Cancelled) }
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                updatePurchaseState(false)
                scope.launch { refresh() }
            }

            else -> {
                updatePurchaseState(false)
                Log.w(TAG, "Purchase update failed: ${billingResult.debugMessage}")
                scope.launch { _purchaseEvents.emit(PremiumPurchaseEvent.Failed) }
            }
        }
    }

    private fun isPremiumProductPurchase(purchase: Purchase): Boolean =
        purchase.products.any { it == config.subscriptionProductId || it == config.lifetimeProductId }

    private fun isPremiumPurchase(purchase: Purchase): Boolean =
        purchase.purchaseState == Purchase.PurchaseState.PURCHASED && isPremiumProductPurchase(purchase)

    private fun grantAndAcknowledge(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PENDING || !isPremiumPurchase(purchase)) return

        _status.value = SubscriptionStatus.Premium
        if (purchase.isAcknowledged) return

        billingClient.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        ) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "Purchase acknowledgement failed: ${result.debugMessage}")
            }
        }
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            ) { result ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "Purchase acknowledgement failed: ${result.debugMessage}")
                }
            }
        }
    }

    private fun updatePurchaseState(isPurchasing: Boolean) {
        _uiState.value = _uiState.value.copy(isPurchasing = isPurchasing)
    }

    private fun settleInitialStatus() {
        if (_status.value == SubscriptionStatus.Loading) {
            _status.value = SubscriptionStatus.Free
        }
    }

    private data class PurchasableProduct(
        val details: ProductDetails,
        val offerToken: String?,
    )

}

private class PlayBillingQueries(
    private val billingClient: BillingClient,
) {
    private val connectionMutex = Mutex()

    suspend fun ensureConnected(): Boolean = connectionMutex.withLock {
        if (billingClient.isReady) return@withLock true

        suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(object : com.android.billingclient.api.BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (continuation.isActive) {
                        continuation.resume(
                            billingResult.responseCode == BillingClient.BillingResponseCode.OK
                        )
                    }
                }

                override fun onBillingServiceDisconnected() = Unit
            })
        }
    }

    suspend fun queryPurchases(productType: String): PurchaseQuery =
        suspendCancellableCoroutine { continuation ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build()
            billingClient.queryPurchasesAsync(params) { result, purchases ->
                if (continuation.isActive) {
                    continuation.resume(PurchaseQuery(result, purchases))
                }
            }
        }

    suspend fun queryProductDetails(productId: String, productType: String): ProductQuery =
        suspendCancellableCoroutine { continuation ->
            val product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(product))
                .build()
            billingClient.queryProductDetailsAsync(params) { result, details ->
                if (continuation.isActive) {
                    continuation.resume(ProductQuery(result, details.productDetailsList))
                }
            }
        }
}

private data class PurchaseQuery(
    val billingResult: BillingResult,
    val purchases: List<Purchase>,
)

private data class ProductQuery(
    val billingResult: BillingResult,
    val productDetails: List<ProductDetails>,
)
