package com.luisfagundes.premium.impl.data

internal object RevenueCatProductConfig {
    const val MONTHLY_PRODUCT_ID = "monthly"
    const val YEARLY_PRODUCT_ID = "yearly"
    const val HONEYBEE_PRO_ENTITLEMENT_ID = "Honeybee Pro"

    val supportedProductIds = setOf(MONTHLY_PRODUCT_ID, YEARLY_PRODUCT_ID)
}
