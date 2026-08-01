package com.luisfagundes.premium.impl.domain.model

import com.luisfagundes.core.common.provider.SubscriptionStatus

internal data class PremiumSubscriptionState(
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.Loading,
    val offers: List<SubscriptionOffer> = emptyList(),
    val isPurchasePending: Boolean = false,
    val errorMessage: String? = null,
    val customerInfo: CustomerInfoSnapshot? = null,
)
