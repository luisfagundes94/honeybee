package com.luisfagundes.core.ads.data

internal const val MINIMUM_DELETED_ITEMS_FOR_INTERSTITIAL = 20
internal const val CLEANUP_INTERSTITIAL_COOLDOWN_MILLIS = 15 * 60 * 1000L

internal fun shouldShowCleanupInterstitial(
    deletedCount: Int,
    lastShownAtMillis: Long,
    nowMillis: Long,
): Boolean {
    val hasEnoughDeletedItems = deletedCount >= MINIMUM_DELETED_ITEMS_FOR_INTERSTITIAL
    val cooldownHasElapsed = lastShownAtMillis == 0L ||
        nowMillis - lastShownAtMillis >= CLEANUP_INTERSTITIAL_COOLDOWN_MILLIS
    return hasEnoughDeletedItems && cooldownHasElapsed
}
