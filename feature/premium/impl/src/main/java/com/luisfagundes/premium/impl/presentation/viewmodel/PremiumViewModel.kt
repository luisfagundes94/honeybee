package com.luisfagundes.premium.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import com.luisfagundes.premium.impl.domain.model.SubscriptionPlan
import com.luisfagundes.premium.impl.domain.repository.PremiumSubscriptionRepository
import com.luisfagundes.premium.impl.presentation.effect.PremiumUiEffect
import com.luisfagundes.premium.impl.presentation.event.PremiumUiEvent
import com.luisfagundes.premium.impl.presentation.state.PremiumUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PremiumViewModel @Inject constructor(
    private val subscriptionRepository: PremiumSubscriptionRepository,
) : ViewModel<PremiumUiState, PremiumUiEvent, PremiumUiEffect>(
    initialState = PremiumUiState.Content()
) {
    init {
        viewModelScope.launch {
            subscriptionRepository.status.collect { status ->
                setStateOf<PremiumUiState.Content> {
                    it.copy(subscriptionStatus = status)
                }
            }
        }
        viewModelScope.launch {
            subscriptionRepository.offers.collect { offers ->
                setStateOf<PremiumUiState.Content> {
                    val selected = it.selectedOfferId
                        ?.takeIf { id -> offers.any { offer -> offer.id == id } }
                        ?: offers.firstOrNull { offer -> offer.plan == SubscriptionPlan.ANNUAL }?.id
                        ?: offers.firstOrNull()?.id
                    it.copy(offers = offers, selectedOfferId = selected)
                }
            }
        }
        viewModelScope.launch {
            subscriptionRepository.isPurchasePending.collect { isPending ->
                setStateOf<PremiumUiState.Content> {
                    it.copy(isPurchasePending = isPending)
                }
            }
        }
    }

    override fun dispatchEvent(event: PremiumUiEvent) {
        when (event) {
            PremiumUiEvent.Load, PremiumUiEvent.RestoreClick -> refresh()
            PremiumUiEvent.BackClick -> sendEffect { PremiumUiEffect.NavigateBack }
            is PremiumUiEvent.OfferSelected -> setStateOf<PremiumUiState.Content> {
                it.copy(selectedOfferId = event.offerId)
            }
            PremiumUiEvent.PurchaseClick -> launchPurchase()
            PremiumUiEvent.ManageSubscriptionClick -> sendEffect { PremiumUiEffect.OpenSubscriptionManagement }
        }
    }

    private fun refresh() = viewModelScope.launch { subscriptionRepository.refresh() }

    private fun launchPurchase() {
        val currentState = getCurrentState()
        val offer = currentState.offers.firstOrNull { it.id == currentState.selectedOfferId } ?: return
        sendEffect { PremiumUiEffect.LaunchPurchase(offer.offerToken) }
    }
}
