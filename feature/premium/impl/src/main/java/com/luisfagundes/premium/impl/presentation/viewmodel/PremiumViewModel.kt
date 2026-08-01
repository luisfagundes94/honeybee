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
    initialState = PremiumUiState()
) {
    init {
        viewModelScope.launch {
            subscriptionRepository.state.collect { subscriptionState ->
                setState { currentState ->
                    val selectedOfferId = currentState.selectedOfferId
                        ?.takeIf { id -> subscriptionState.offers.any { offer -> offer.id == id } }
                        ?: subscriptionState.offers
                            .firstOrNull { offer -> offer.plan == SubscriptionPlan.YEARLY }
                            ?.id
                        ?: subscriptionState.offers.firstOrNull()?.id
                    currentState.copy(
                        subscriptionStatus = subscriptionState.subscriptionStatus,
                        offers = subscriptionState.offers,
                        selectedOfferId = selectedOfferId,
                        isPurchasePending = subscriptionState.isPurchasePending,
                        errorMessage = subscriptionState.errorMessage,
                    )
                }
            }
        }
    }

    override fun dispatchEvent(event: PremiumUiEvent) {
        when (event) {
            PremiumUiEvent.Load -> refresh()
            PremiumUiEvent.RestoreClick -> restorePurchases()
            PremiumUiEvent.BackClick -> sendEffect { PremiumUiEffect.NavigateBack }
            is PremiumUiEvent.OfferSelected -> setState {
                it.copy(selectedOfferId = event.offerId)
            }
            PremiumUiEvent.PurchaseClick -> launchPurchase()
            PremiumUiEvent.ManageSubscriptionClick -> sendEffect { PremiumUiEffect.OpenCustomerCenter }
            PremiumUiEvent.PaywallClick -> sendEffect { PremiumUiEffect.ShowPaywall }
        }
    }

    private fun refresh() = viewModelScope.launch { subscriptionRepository.refresh() }

    private fun restorePurchases() = viewModelScope.launch {
        subscriptionRepository.restorePurchases()
            .onSuccess {
                setState {
                    it.copy(errorMessage = null)
                }
            }
            .onFailure { error ->
                setState {
                    it.copy(errorMessage = error.message)
                }
            }
    }

    private fun launchPurchase() {
        val currentState = getCurrentState()
        val offer = currentState.offers.firstOrNull { it.id == currentState.selectedOfferId } ?: return
        sendEffect { PremiumUiEffect.LaunchPurchase(offer.id) }
    }
}
