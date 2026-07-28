package com.luisfagundes.premium.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import com.luisfagundes.premium.impl.data.PlayBillingSubscriptionProvider
import com.luisfagundes.premium.impl.domain.model.SubscriptionPlan
import com.luisfagundes.premium.impl.presentation.effect.PremiumUiEffect
import com.luisfagundes.premium.impl.presentation.event.PremiumUiEvent
import com.luisfagundes.premium.impl.presentation.state.PremiumUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PremiumViewModel @Inject constructor(
    private val subscriptionProvider: PlayBillingSubscriptionProvider,
) : ViewModel<PremiumUiState, PremiumUiEvent, PremiumUiEffect>(
    initialState = PremiumUiState()
) {
    init {
        viewModelScope.launch {
            subscriptionProvider.status.collect { status ->
                setState { it.copy(subscriptionStatus = status) }
            }
        }
        viewModelScope.launch {
            subscriptionProvider.offers.collect { offers ->
                setState {
                    val selected = it.selectedOfferId
                        ?.takeIf { id -> offers.any { offer -> offer.id == id } }
                        ?: offers.firstOrNull { offer -> offer.plan == SubscriptionPlan.ANNUAL }?.id
                        ?: offers.firstOrNull()?.id
                    it.copy(offers = offers, selectedOfferId = selected)
                }
            }
        }
        viewModelScope.launch {
            subscriptionProvider.isPurchasePending.collect { isPending ->
                setState { it.copy(isPurchasePending = isPending) }
            }
        }
    }

    override fun dispatchEvent(event: PremiumUiEvent) {
        when (event) {
            PremiumUiEvent.Load, PremiumUiEvent.RestoreClick -> refresh()
            PremiumUiEvent.BackClick -> sendEffect { PremiumUiEffect.NavigateBack }
            is PremiumUiEvent.OfferSelected -> setState { it.copy(selectedOfferId = event.offerId) }
            PremiumUiEvent.PurchaseClick -> launchPurchase()
            PremiumUiEvent.ManageSubscriptionClick -> sendEffect { PremiumUiEffect.OpenSubscriptionManagement }
        }
    }

    private fun refresh() = viewModelScope.launch { subscriptionProvider.refresh() }

    private fun launchPurchase() {
        val currentState = getCurrentState()
        val offer = currentState.offers.firstOrNull { it.id == currentState.selectedOfferId } ?: return
        sendEffect { PremiumUiEffect.LaunchPurchase(offer.offerToken) }
    }
}
