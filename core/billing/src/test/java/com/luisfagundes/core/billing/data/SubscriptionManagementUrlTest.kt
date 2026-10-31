package com.luisfagundes.core.billing.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SubscriptionManagementUrlTest {
    @Test
    fun `url targets the subscription of the given product and package`() {
        assertEquals(
            "https://play.google.com/store/account/subscriptions" +
                    "?sku=honeybee_premium&package=com.luisfagundes.honeybee",
            subscriptionManagementUrl("honeybee_premium", "com.luisfagundes.honeybee"),
        )
    }
}
