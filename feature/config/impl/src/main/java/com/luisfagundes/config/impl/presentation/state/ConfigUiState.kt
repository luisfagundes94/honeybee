package com.luisfagundes.config.impl.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState

internal data class ConfigUiState(
    val isNotificationsEnabled: Boolean = true,
    val canShowSettingsBanner: Boolean = false,
    val isPrivacyOptionsRequired: Boolean = false,
    val settingsBannerAdUnitId: String = "",
) : UiState
