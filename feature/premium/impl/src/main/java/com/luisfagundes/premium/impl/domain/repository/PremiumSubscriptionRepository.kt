package com.luisfagundes.premium.impl.domain.repository

import com.luisfagundes.premium.impl.domain.model.CustomerInfoSnapshot
import com.luisfagundes.premium.impl.domain.model.PremiumSubscriptionState
import kotlinx.coroutines.flow.StateFlow

internal interface PremiumSubscriptionRepository {
    val state: StateFlow<PremiumSubscriptionState>

    suspend fun refresh()
    suspend fun restorePurchases(): Result<Unit>
    suspend fun getCustomerInfo(): Result<CustomerInfoSnapshot>
}
