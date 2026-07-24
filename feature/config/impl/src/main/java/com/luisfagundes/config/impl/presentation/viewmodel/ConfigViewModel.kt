package com.luisfagundes.config.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.config.impl.domain.usecase.ObserveNotificationsEnabledUseCase
import com.luisfagundes.config.impl.domain.usecase.SetNotificationsEnabledUseCase
import com.luisfagundes.config.impl.presentation.effect.ConfigUiEffect
import com.luisfagundes.config.impl.presentation.event.ConfigUiEvent
import com.luisfagundes.config.impl.presentation.state.ConfigUiState
import com.luisfagundes.core.ads.AdsConfig
import com.luisfagundes.core.ads.AdsCoordinator
import com.luisfagundes.core.common.presentation.arch.viewmodel.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ConfigViewModel @Inject constructor(
    private val observeNotificationsEnabledUseCase: ObserveNotificationsEnabledUseCase,
    private val setNotificationsEnabledUseCase: SetNotificationsEnabledUseCase,
    private val adsCoordinator: AdsCoordinator,
    adsConfig: AdsConfig,
) : ViewModel<ConfigUiState, ConfigUiEvent, ConfigUiEffect>(
    initialState = ConfigUiState()
) {
    init {
        observeNotificationsEnabled()
        observeAds(adsConfig)
    }

    override fun dispatchEvent(event: ConfigUiEvent) {
        when (event) {
            is ConfigUiEvent.NotificationsToggled -> updateNotificationsEnabled(event.enabled)
            ConfigUiEvent.StatisticsClick -> navigateToStatistics()
            ConfigUiEvent.FeedbackClick -> navigateToFeedback()
            ConfigUiEvent.PremiumClick -> navigateToPremium()
            ConfigUiEvent.PrivacyChoicesClick -> sendEffect { ConfigUiEffect.ShowPrivacyOptions }
        }
    }

    private fun observeNotificationsEnabled() = viewModelScope.launch {
        observeNotificationsEnabledUseCase().collect { enabled ->
            setState { it.copy(isNotificationsEnabled = enabled) }
        }
    }

    private fun updateNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        setNotificationsEnabledUseCase(enabled)
    }

    private fun navigateToStatistics() {
        sendEffect { ConfigUiEffect.NavigateToStatistics }
    }

    private fun navigateToFeedback() {
        sendEffect { ConfigUiEffect.NavigateToFeedback }
    }

    private fun navigateToPremium() {
        sendEffect { ConfigUiEffect.NavigateToPremium }
    }

    private fun observeAds(adsConfig: AdsConfig) = viewModelScope.launch {
        adsCoordinator.state.collect { adsState ->
            setState {
                it.copy(
                    canShowSettingsBanner = adsState.canShowAds,
                    isPrivacyOptionsRequired = adsState.isPrivacyOptionsRequired,
                    settingsBannerAdUnitId = if (adsState.canShowAds) {
                        adsConfig.settingsBannerAdUnitId
                    } else {
                        ""
                    },
                )
            }
        }
    }
}
