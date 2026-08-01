package com.luisfagundes.premium.impl.domain.model

internal enum class SubscriptionPlan { MONTHLY, ANNUAL }

internal class SubscriptionOffer(
    val id: String,
    val plan: SubscriptionPlan,
    val formattedPrice: String,
    val offerToken: String,
)
