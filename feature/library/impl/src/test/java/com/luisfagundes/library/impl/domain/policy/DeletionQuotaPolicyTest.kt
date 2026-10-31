package com.luisfagundes.library.impl.domain.policy

import com.luisfagundes.library.api.domain.model.DELETION_LIMIT
import com.luisfagundes.library.api.domain.model.DELETION_WINDOW_MILLIS
import com.luisfagundes.library.api.domain.model.DeletionQuota
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DeletionQuotaPolicyTest {

    private val now = 1_000_000_000L

    @Test
    fun `fresh counters expose the full quota`() {
        // When
        val quota = DeletionQuotaPolicy.quotaAt(DeletionCounters(), now)

        // Then
        assertEquals(DeletionQuota.Available(DELETION_LIMIT), quota)
    }

    @Test
    fun `partial usage reduces the remaining quota`() {
        // Given
        val counters = DeletionQuotaPolicy.record(DeletionCounters(), count = 10, nowMillis = now)

        // When
        val quota = DeletionQuotaPolicy.quotaAt(counters, now + 1_000L)

        // Then
        assertEquals(DeletionQuota.Available(DELETION_LIMIT - 10), quota)
        assertEquals(0L, counters.lockedUntilMillis)
        assertEquals(now, counters.windowStartMillis)
    }

    @Test
    fun `reaching the limit locks for the full window`() {
        // When
        val counters = DeletionQuotaPolicy.record(DeletionCounters(), DELETION_LIMIT, now)

        // Then
        assertEquals(now + DELETION_WINDOW_MILLIS, counters.lockedUntilMillis)
        assertEquals(
            DeletionQuota.Locked(now + DELETION_WINDOW_MILLIS),
            DeletionQuotaPolicy.quotaAt(counters, now + 1L)
        )
    }

    @Test
    fun `multiple deletions in the same window accumulate until the lock`() {
        // Given
        val first = DeletionQuotaPolicy.record(DeletionCounters(), 20, now)

        // When
        val second = DeletionQuotaPolicy.record(first, 5, now + 60_000L)

        // Then
        assertEquals(DELETION_LIMIT, second.deleted)
        assertEquals(now, second.windowStartMillis)
        assertEquals(now + 60_000L + DELETION_WINDOW_MILLIS, second.lockedUntilMillis)
    }

    @Test
    fun `quota is available again once the lock expires`() {
        // Given
        val locked = DeletionQuotaPolicy.record(DeletionCounters(), DELETION_LIMIT, now)

        // When
        val quota = DeletionQuotaPolicy.quotaAt(locked, now + DELETION_WINDOW_MILLIS)

        // Then
        assertEquals(DeletionQuota.Available(DELETION_LIMIT), quota)
    }

    @Test
    fun `recording after the lock expired starts a new window`() {
        // Given
        val locked = DeletionQuotaPolicy.record(DeletionCounters(), DELETION_LIMIT, now)
        val later = now + DELETION_WINDOW_MILLIS + 5_000L

        // When
        val counters = DeletionQuotaPolicy.record(locked, 3, later)

        // Then
        assertEquals(DeletionCounters(deleted = 3, windowStartMillis = later), counters)
    }

    @Test
    fun `a stale unlocked window resets the quota`() {
        // Given
        val counters = DeletionQuotaPolicy.record(DeletionCounters(), 10, now)

        // When
        val quota = DeletionQuotaPolicy.quotaAt(counters, now + DELETION_WINDOW_MILLIS)

        // Then
        assertEquals(DeletionQuota.Available(DELETION_LIMIT), quota)
    }
}
