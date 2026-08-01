package com.luisfagundes.config.impl.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.luisfagundes.config.impl.domain.repository.ConfigRepository
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
    private val repository: ConfigRepository,
    private val adsCoordinator: AdsCoordinator,
    adsConfig: AdsConfig,
) : ViewModel<ConfigUiState, ConfigUiEvent, ConfigUiEffect>(
    initialState = ConfigUiState.Content()
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
        repository.notificationsEnabled().collect { enabled ->
            setStateOf<ConfigUiState.Content> {
                it.copy(isNotificationsEnabled = enabled)
            }
        }
    }

    private fun updateNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        val previousState = getCurrentState()
        repository.setNotificationsEnabled(enabled).fold(
            onSuccess = {},
            onFailure = {
                setState { previousState }
            }
        )
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
            setStateOf<ConfigUiState.Content> {
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
