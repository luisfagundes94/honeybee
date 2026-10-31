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
import kotlinx.coroutines.flow.catch
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
            ConfigUiEvent.PrivacyChoicesClick -> sendEffect { ConfigUiEffect.ShowPrivacyOptions }
            ConfigUiEvent.PrivacyPolicyClick -> sendEffect { ConfigUiEffect.OpenPrivacyPolicy }
        }
    }

    private fun observeNotificationsEnabled() = viewModelScope.launch {
        repository.notificationsEnabled()
            .catch { /* keep the last known toggle value */ }
            .collect { enabled ->
                setStateOf<ConfigUiState.Content> {
                    it.copy(isNotificationsEnabled = enabled)
                }
            }
    }

    private fun updateNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        // The stored preference drives the toggle, so a failed write leaves it unchanged.
        repository.setNotificationsEnabled(enabled)
    }

    private fun navigateToStatistics() {
        sendEffect { ConfigUiEffect.NavigateToStatistics }
    }

    private fun navigateToFeedback() {
        sendEffect { ConfigUiEffect.NavigateToFeedback }
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
