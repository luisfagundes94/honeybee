package com.luisfagundes.premium.impl.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState
import com.luisfagundes.core.common.provider.SubscriptionStatus
import com.luisfagundes.premium.impl.domain.model.SubscriptionOffer

internal data class PremiumUiState(
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.Loading,
    val offers: List<SubscriptionOffer> = emptyList(),
    val selectedOfferId: String? = null,
    val isPurchasePending: Boolean = false,
    val errorMessage: String? = null,
) : UiState
