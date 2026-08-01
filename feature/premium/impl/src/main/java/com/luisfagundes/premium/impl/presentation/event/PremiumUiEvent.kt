package com.luisfagundes.premium.impl.presentation.event

import com.luisfagundes.core.common.presentation.arch.event.UiEvent

internal sealed interface PremiumUiEvent : UiEvent {
    data object Load : PremiumUiEvent
    data object BackClick : PremiumUiEvent
    data class OfferSelected(val offerId: String) : PremiumUiEvent
    data object PurchaseClick : PremiumUiEvent
    data object RestoreClick : PremiumUiEvent
    data object ManageSubscriptionClick : PremiumUiEvent
    data object PaywallClick : PremiumUiEvent
}
