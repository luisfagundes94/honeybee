package com.luisfagundes.library.impl.domain.policy

import com.luisfagundes.library.api.domain.model.DELETION_LIMIT
import com.luisfagundes.library.api.domain.model.DELETION_WINDOW_MILLIS
import com.luisfagundes.library.api.domain.model.DeletionQuota

internal data class DeletionCounters(
    val deleted: Int = 0,
    val windowStartMillis: Long = 0L,
    val lockedUntilMillis: Long = 0L
)

internal object DeletionQuotaPolicy {

    fun quotaAt(counters: DeletionCounters, nowMillis: Long): DeletionQuota {
        if (counters.lockedUntilMillis > nowMillis) {
            return DeletionQuota.Locked(counters.lockedUntilMillis)
        }
        val current = normalize(counters, nowMillis)
        return DeletionQuota.Available((DELETION_LIMIT - current.deleted).coerceAtLeast(0))
    }

    fun record(counters: DeletionCounters, count: Int, nowMillis: Long): DeletionCounters {
        val current = normalize(counters, nowMillis)
        val windowStart = if (current.deleted == 0) nowMillis else current.windowStartMillis
        val total = current.deleted + count
        val lockedUntil = if (total >= DELETION_LIMIT) nowMillis + DELETION_WINDOW_MILLIS else 0L
        return DeletionCounters(total, windowStart, lockedUntil)
    }

    private fun normalize(counters: DeletionCounters, nowMillis: Long): DeletionCounters {
        val lockExpired = counters.lockedUntilMillis != 0L && nowMillis >= counters.lockedUntilMillis
        val windowExpired = counters.lockedUntilMillis == 0L &&
            counters.deleted > 0 &&
            nowMillis - counters.windowStartMillis >= DELETION_WINDOW_MILLIS
        return if (lockExpired || windowExpired) DeletionCounters() else counters
    }
}
