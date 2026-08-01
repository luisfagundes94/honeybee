package com.luisfagundes.premium.impl.domain.repository

import com.luisfagundes.core.common.provider.SubscriptionStatus
import com.luisfagundes.premium.impl.domain.model.SubscriptionOffer
import kotlinx.coroutines.flow.StateFlow

internal interface PremiumSubscriptionRepository {
    val status: StateFlow<SubscriptionStatus>
    val offers: StateFlow<List<SubscriptionOffer>>
    val isPurchasePending: StateFlow<Boolean>

    suspend fun refresh()
}
