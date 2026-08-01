package com.luisfagundes.core.common.provider

import kotlinx.coroutines.flow.StateFlow

sealed interface SubscriptionStatus {
    data object Loading : SubscriptionStatus
    data object Free : SubscriptionStatus
    data object Premium : SubscriptionStatus
}

interface SubscriptionProvider {
    val status: StateFlow<SubscriptionStatus>

    suspend fun refresh()
}
