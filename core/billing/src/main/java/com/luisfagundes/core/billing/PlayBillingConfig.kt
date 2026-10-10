package com.luisfagundes.core.billing

data class PlayBillingConfig(
    val subscriptionProductId: String,
    val monthlyBasePlanId: String,
    val lifetimeProductId: String,
)
