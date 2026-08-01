package com.luisfagundes.premium.impl.data

import android.app.Activity
import android.util.Log
import com.luisfagundes.core.common.di.MainDispatcher
import com.luisfagundes.core.common.provider.SubscriptionProvider
import com.luisfagundes.core.common.provider.SubscriptionStatus
import com.luisfagundes.premium.impl.domain.model.CustomerInfoSnapshot
import com.luisfagundes.premium.impl.domain.model.PremiumSubscriptionState
import com.luisfagundes.premium.impl.domain.model.SubscriptionOffer
import com.luisfagundes.premium.impl.domain.model.SubscriptionPlan
import com.luisfagundes.premium.impl.domain.repository.PremiumSubscriptionRepository
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.PurchasesTransactionException
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val LOG_TAG = "RevenueCatSubscriptions"

@Singleton
internal class RevenueCatSubscriptionProvider @Inject constructor(
    @MainDispatcher mainDispatcher: CoroutineDispatcher,
) :
    SubscriptionProvider,
    PremiumSubscriptionRepository {

    private val purchases = Purchases.sharedInstance
    private val _state = MutableStateFlow(PremiumSubscriptionState())
    private val purchaseScope = CoroutineScope(SupervisorJob() + mainDispatcher)
    private var packagesByProductId: Map<String, Package> = emptyMap()

    override val state: StateFlow<PremiumSubscriptionState> = _state.asStateFlow()
    override val status: StateFlow<SubscriptionStatus> = state
        .map { it.subscriptionStatus }
        .stateIn(
            scope = purchaseScope,
            started = SharingStarted.Eagerly,
            initialValue = SubscriptionStatus.Loading,
        )

    init {
        purchases.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
            applyCustomerInfo(customerInfo)
        }
    }

    override suspend fun refresh() {
        _state.update { it.copy(errorMessage = null) }
        refreshOfferings()
        refreshCustomerInfo()
    }

    override suspend fun restorePurchases(): Result<Unit> {
        _state.update { it.copy(isPurchasePending = true, errorMessage = null) }
        return try {
            val restoredCustomerInfo = purchases.awaitRestore()
            applyCustomerInfo(restoredCustomerInfo)
            Result.success(Unit)
        } catch (error: PurchasesException) {
            handleError(error)
            Result.failure(error)
        } finally {
            _state.update { it.copy(isPurchasePending = false) }
        }
    }

    override suspend fun getCustomerInfo(): Result<CustomerInfoSnapshot> {
        return try {
            Result.success(purchases.awaitCustomerInfo().also(::applyCustomerInfo).toDomain())
        } catch (error: PurchasesException) {
            handleError(error)
            Result.failure(error)
        }
    }

    fun launchPurchase(activity: Activity, productId: String) {
        val packageToPurchase = packagesByProductId[productId]
        if (packageToPurchase == null) {
            val error = "RevenueCat product is not available: $productId"
            handleErrorMessage(error)
            Log.w(LOG_TAG, error)
            return
        }

        _state.update { it.copy(isPurchasePending = true, errorMessage = null) }
        purchaseScope.launch {
            try {
                val purchaseResult = purchases.awaitPurchase(
                    PurchaseParams.Builder(activity, packageToPurchase).build(),
                )
                applyCustomerInfo(purchaseResult.customerInfo)
            } catch (error: PurchasesTransactionException) {
                if (!error.userCancelled) {
                    handleError(error)
                }
            } finally {
                _state.update { it.copy(isPurchasePending = false) }
            }
        }
    }

    private suspend fun refreshOfferings() {
        try {
            val currentOffering = purchases.awaitOfferings().current
            if (currentOffering == null) {
                packagesByProductId = emptyMap()
                _state.update { it.copy(offers = emptyList()) }
                handleErrorMessage("RevenueCat did not return a current offering.")
                return
            }

            packagesByProductId = currentOffering.availablePackages
                .filter { it.product.id in RevenueCatProductConfig.supportedProductIds }
                .associateBy { it.product.id }
            val offers = packagesByProductId.values
                .mapNotNull { it.toSubscriptionOffer() }
                .sortedBy { it.plan.ordinal }
            _state.update { it.copy(offers = offers) }
        } catch (error: PurchasesException) {
            handleError(error)
        }
    }

    private suspend fun refreshCustomerInfo() {
        try {
            purchases.awaitCustomerInfo().also(::applyCustomerInfo)
        } catch (error: PurchasesException) {
            handleError(error)
        }
    }

    private fun applyCustomerInfo(customerInfo: CustomerInfo) {
        val subscriptionStatus = if (
            customerInfo.entitlements[RevenueCatProductConfig.HONEYBEE_PRO_ENTITLEMENT_ID]
                ?.isActive == true
        ) {
            SubscriptionStatus.Premium
        } else {
            SubscriptionStatus.Free
        }
        _state.update {
            it.copy(
                subscriptionStatus = subscriptionStatus,
                customerInfo = customerInfo.toDomain(),
            )
        }
    }

    private fun Package.toSubscriptionOffer(): SubscriptionOffer? {
        val plan = when (product.id) {
            RevenueCatProductConfig.MONTHLY_PRODUCT_ID -> SubscriptionPlan.MONTHLY
            RevenueCatProductConfig.YEARLY_PRODUCT_ID -> SubscriptionPlan.YEARLY
            else -> return null
        }
        return SubscriptionOffer(
            id = product.id,
            plan = plan,
            formattedPrice = product.price.formatted,
        )
    }

    private fun CustomerInfo.toDomain() = CustomerInfoSnapshot(
        appUserId = purchases.appUserID,
        originalAppUserId = originalAppUserId,
        activeEntitlements = entitlements.active.keys,
        managementUrl = managementURL?.toString(),
    )

    private fun handleError(error: PurchasesException) {
        _state.update { currentState ->
            currentState.copy(
                subscriptionStatus = if (currentState.subscriptionStatus == SubscriptionStatus.Loading) {
                    SubscriptionStatus.Free
                } else {
                    currentState.subscriptionStatus
                },
                errorMessage = error.message,
            )
        }
        Log.w(LOG_TAG, error.message, error)
    }

    private fun handleErrorMessage(message: String) {
        _state.update { it.copy(errorMessage = message) }
    }
}
