package com.luisfagundes.core.billing

import android.app.Activity
import com.luisfagundes.core.common.provider.SubscriptionProvider
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

enum class PremiumPlan {
    Monthly,
    Lifetime,
}

data class PremiumBillingUiState(
    val isLoadingOffers: Boolean = true,
    val monthlyPrice: String? = null,
    val lifetimePrice: String? = null,
    val isPurchasing: Boolean = false,
    val offersUnavailable: Boolean = false,
)

enum class PremiumPurchaseEvent {
    Purchased,
    Pending,
    Cancelled,
    Unavailable,
    Failed,
}

interface PremiumBillingCoordinator : SubscriptionProvider {
    val uiState: StateFlow<PremiumBillingUiState>
    val purchaseEvents: SharedFlow<PremiumPurchaseEvent>

    fun refreshOffers()

    fun purchase(activity: Activity, plan: PremiumPlan)
}
