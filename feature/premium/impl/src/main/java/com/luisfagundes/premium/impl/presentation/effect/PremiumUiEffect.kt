package com.luisfagundes.premium.impl.presentation.effect

import com.luisfagundes.core.common.presentation.arch.effect.UiEffect

internal sealed interface PremiumUiEffect : UiEffect {
    data object NavigateBack : PremiumUiEffect
    data class LaunchPurchase(val productId: String) : PremiumUiEffect
    data object OpenCustomerCenter : PremiumUiEffect
    data object ShowPaywall : PremiumUiEffect
}
