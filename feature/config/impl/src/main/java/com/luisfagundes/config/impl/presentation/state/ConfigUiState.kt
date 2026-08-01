package com.luisfagundes.config.impl.presentation.state

import com.luisfagundes.core.common.presentation.arch.state.UiState

internal sealed interface ConfigUiState : UiState {
    val isNotificationsEnabled: Boolean
    val canShowSettingsBanner: Boolean
    val isPrivacyOptionsRequired: Boolean
    val settingsBannerAdUnitId: String

    data class Content(
        override val isNotificationsEnabled: Boolean = true,
        override val canShowSettingsBanner: Boolean = false,
        override val isPrivacyOptionsRequired: Boolean = false,
        override val settingsBannerAdUnitId: String = "",
    ) : ConfigUiState
}
