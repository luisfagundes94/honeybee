package com.luisfagundes.premium.impl.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState
import com.luisfagundes.core.common.provider.SubscriptionStatus
import com.luisfagundes.premium.impl.domain.model.SubscriptionOffer

internal sealed interface PremiumUiState : UiState {
    val subscriptionStatus: SubscriptionStatus
    val offers: List<SubscriptionOffer>
    val selectedOfferId: String?
    val isPurchasePending: Boolean

    data class Content(
        override val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.Loading,
        override val offers: List<SubscriptionOffer> = emptyList(),
        override val selectedOfferId: String? = null,
        override val isPurchasePending: Boolean = false,
    ) : PremiumUiState
}
