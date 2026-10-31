package com.luisfagundes.core.billing.data

private const val SUBSCRIPTIONS_URL = "https://play.google.com/store/account/subscriptions"

internal fun subscriptionManagementUrl(productId: String, packageName: String): String =
    "$SUBSCRIPTIONS_URL?sku=$productId&package=$packageName"
