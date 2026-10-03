package com.luisfagundes.core.ads.data

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CleanupInterstitialPolicyTest {
    @Test
    fun `cleanup below threshold does not qualify`() {
        assertFalse(
            shouldShowCleanupInterstitial(
                deletedCount = MINIMUM_DELETED_ITEMS_FOR_INTERSTITIAL - 1,
                lastShownAtMillis = 0L,
                nowMillis = 1L,
            ),
        )
    }

    @Test
    fun `cleanup at threshold qualifies when no prior ad was shown`() {
        assertTrue(
            shouldShowCleanupInterstitial(
                deletedCount = MINIMUM_DELETED_ITEMS_FOR_INTERSTITIAL,
                lastShownAtMillis = 0L,
                nowMillis = 1L,
            ),
        )
    }

    @Test
    fun `cleanup during cooldown does not qualify`() {
        assertFalse(
            shouldShowCleanupInterstitial(
                deletedCount = MINIMUM_DELETED_ITEMS_FOR_INTERSTITIAL,
                lastShownAtMillis = 1_000L,
                nowMillis = 1_000L + CLEANUP_INTERSTITIAL_COOLDOWN_MILLIS - 1L,
            ),
        )
    }

    @Test
    fun `cleanup qualifies when cooldown has elapsed`() {
        assertTrue(
            shouldShowCleanupInterstitial(
                deletedCount = MINIMUM_DELETED_ITEMS_FOR_INTERSTITIAL,
                lastShownAtMillis = 1_000L,
                nowMillis = 1_000L + CLEANUP_INTERSTITIAL_COOLDOWN_MILLIS,
            ),
        )
    }
}
