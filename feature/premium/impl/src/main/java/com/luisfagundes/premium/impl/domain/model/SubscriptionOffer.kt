package com.luisfagundes.premium.impl.domain.model

internal enum class SubscriptionPlan { MONTHLY, YEARLY }

internal data class SubscriptionOffer(
    val id: String,
    val plan: SubscriptionPlan,
    val formattedPrice: String,
)
